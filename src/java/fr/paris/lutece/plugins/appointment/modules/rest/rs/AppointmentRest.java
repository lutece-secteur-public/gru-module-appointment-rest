/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.appointment.modules.rest.rs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.AppointmentSlotsSearchPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.rs.filter.QueryParamValidator;
import fr.paris.lutece.plugins.appointment.modules.rest.rs.filter.ValidationErrorResponse;
import fr.paris.lutece.plugins.appointment.modules.rest.service.IAppointmentRestService;
import fr.paris.lutece.plugins.appointment.modules.rest.util.contsants.AppointmentRestConstants;
import fr.paris.lutece.plugins.appointment.service.AppointmentPlugin;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Path( RestConstants.BASE_PATH + AppointmentPlugin.PLUGIN_NAME )
public class AppointmentRest
{

    public static final String SOLR_EXCEPTION = "appointment-rest.solr.exception.message";
    public static final String JSON_EXCEPTION = "appointment-rest.json.exception.message";
    private static final String PROPERTY_HUB_AUTH_TOKEN = "appointment-rest.hub.auth.token";
    private static final String HEADER_HUB_AUTH_TOKEN = "X-Hub-Rdv-Auth-Token";
    private static final int DEFAULT_DOCUMENTS_NUMBER = 1;
    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    private static final ObjectMapper MAPPER = new ObjectMapper( ).registerModule( new JavaTimeModule( ) );

    @Inject
    IAppointmentRestService _appointmentRestService;

    /**
     * Get the free slots of the requested meeting points between two dates.
     *
     * @param request
     *            the request
     * @param appointementIds
     *            the meeting point ids
     * @param startDate
     *            the first day, yyyy-MM-dd
     * @param endDate
     *            the last day, yyyy-MM-dd
     * @param reason
     *            the reason of the appointment
     * @param documentsNumber
     *            the number of documents, one by default
     * @return the slots of each meeting point, 401 without the hub token, 422 on invalid parameters, 503 when the search
     *         engine cannot be reached
     */
    @GET
    @Path( AppointmentRestConstants.SLASH + AppointmentRestConstants.PATH_API + AppointmentRestConstants.SLASH + AppointmentRestConstants.PATH_AVAILABLE_SLOTS )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getAvailableTimeSlots( @Context HttpServletRequest request,
            @QueryParam( value = AppointmentRestConstants.JSON_TAG_MEETING_POINT_IDS ) List<String> appointementIds,
            @QueryParam( value = AppointmentRestConstants.JSON_TAG_START_DATE ) String startDate,
            @QueryParam( value = AppointmentRestConstants.JSON_TAG_END_DATE ) String endDate,
            @QueryParam( value = AppointmentRestConstants.JSON_TAG_REASON ) String reason,
            @QueryParam( value = AppointmentRestConstants.JSON_TAG_DOCUMENTS_NUMBER ) String documentsNumber )
    {
        if ( !isHubAuthorized( request ) )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).build( );
        }
        try
        {
            ValidationErrorResponse errors = QueryParamValidator.validate( request );
            if ( CollectionUtils.isNotEmpty( errors.getDetail( ) ) )
            {
                return json( STATUS_UNPROCESSABLE_ENTITY, errors );
            }
            AppointmentSlotsSearchPOJO search = new AppointmentSlotsSearchPOJO( appointementIds,
                    LocalDate.parse( startDate, AppointmentRestConstants.SEARCH_DATE_FORMATTER ),
                    LocalDate.parse( endDate, AppointmentRestConstants.SEARCH_DATE_FORMATTER ), reason,
                    Optional.ofNullable( documentsNumber ).map( Integer::valueOf ).orElse( DEFAULT_DOCUMENTS_NUMBER ) );
            return json( Response.Status.OK.getStatusCode( ), _appointmentRestService.getAvailableTimeSlots( search ) );
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( e.getMessage( ), e );
            return Response.status( Response.Status.SERVICE_UNAVAILABLE ).entity( AppPropertiesService.getProperty( SOLR_EXCEPTION ) ).build( );
        }
        catch( JsonProcessingException e )
        {
            AppLogService.error( e.getMessage( ), e );
            return Response.status( Response.Status.BAD_GATEWAY ).entity( AppPropertiesService.getProperty( JSON_EXCEPTION ) ).build( );
        }
    }

    /**
     * Get the meeting points managed by the site, one per appointment form indexed in the search engine.
     *
     * @param request
     *            the request
     * @return the meeting points, 401 without the hub token, 503 when the search engine cannot be reached
     */
    @GET
    @Path( AppointmentRestConstants.SLASH + AppointmentRestConstants.PATH_API + AppointmentRestConstants.SLASH
            + AppointmentRestConstants.PATH_MANAGED_MEETING_POINTS )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getManagedMeetingPoints( @Context HttpServletRequest request )
    {
        if ( !isHubAuthorized( request ) )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).build( );
        }
        try
        {
            return json( Response.Status.OK.getStatusCode( ), _appointmentRestService.getManagedMeetingPoints( ) );
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( e.getMessage( ), e );
            return Response.status( Response.Status.SERVICE_UNAVAILABLE ).entity( AppPropertiesService.getProperty( SOLR_EXCEPTION ) ).build( );
        }
        catch( JsonProcessingException e )
        {
            AppLogService.error( e.getMessage( ), e );
            return Response.status( Response.Status.BAD_GATEWAY ).entity( AppPropertiesService.getProperty( JSON_EXCEPTION ) ).build( );
        }
    }

    /**
     * Write an answer with Jackson, the serializer the Jackson annotations of the api objects are written for: the
     * container's own JSON binding ignores them and would change the field names the hub reads.
     *
     * @param nStatus
     *            the HTTP status
     * @param entity
     *            the object to write
     * @return the JSON answer
     * @throws JsonProcessingException
     *             if the object cannot be written
     */
    private static Response json( int nStatus, Object entity ) throws JsonProcessingException
    {
        return Response.status( nStatus ).type( MediaType.APPLICATION_JSON_TYPE ).entity( MAPPER.writeValueAsString( entity ) ).build( );
    }

    /**
     * Check the token of the ANTS hub: when the site configures one (appointment-rest.hub.auth.token), every call must
     * carry it in the X-Hub-Rdv-Auth-Token header; without a configured token the API stays open as before.
     *
     * @param request
     *            the request
     * @return true when the call may be served
     */
    private static boolean isHubAuthorized( HttpServletRequest request )
    {
        String strToken = AppPropertiesService.getProperty( PROPERTY_HUB_AUTH_TOKEN );
        if ( StringUtils.isBlank( strToken ) )
        {
            return true;
        }
        String strReceived = request.getHeader( HEADER_HUB_AUTH_TOKEN );
        return strReceived != null && MessageDigest.isEqual( strToken.getBytes( StandardCharsets.UTF_8 ), strReceived.getBytes( StandardCharsets.UTF_8 ) );
    }
}
