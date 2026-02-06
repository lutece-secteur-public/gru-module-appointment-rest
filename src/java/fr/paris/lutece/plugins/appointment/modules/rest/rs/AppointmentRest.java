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
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.AppointmentSlotsSearchPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.InfoSlot;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.MeetingPointPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.rs.filter.QueryParamValidator;
import fr.paris.lutece.plugins.appointment.modules.rest.rs.filter.ValidationErrorResponse;
import fr.paris.lutece.plugins.appointment.modules.rest.service.IAppointmentRestService;
import fr.paris.lutece.plugins.appointment.modules.rest.util.contsants.AppointmentRestConstants;
import fr.paris.lutece.plugins.appointment.service.AppointmentPlugin;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import org.apache.commons.collections.CollectionUtils;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@Path( RestConstants.BASE_PATH + AppointmentPlugin.PLUGIN_NAME )
public class AppointmentRest
{

    public static final String SOLR_EXCEPTION = "appointment-rest.solr.exception.message";
    public static final String JSON_EXCEPTION = "appointment-rest.json.exception.message";

    @Inject
    IAppointmentRestService _appointmentRestService;

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
        ValidationErrorResponse errors = QueryParamValidator.validate( request );
        if ( CollectionUtils.isNotEmpty( errors.getDetail( ) ) )
        {
            return Response.status( 422 ).entity( errors ).build( );
        }
        AppointmentSlotsSearchPOJO search = new AppointmentSlotsSearchPOJO( appointementIds,
                LocalDate.parse( startDate, AppointmentRestConstants.SEARCH_DATE_FORMATTER ),
                LocalDate.parse( endDate, AppointmentRestConstants.SEARCH_DATE_FORMATTER ), reason,
                Optional.ofNullable( documentsNumber ).map( Integer::valueOf ).orElse( null ) );
        Map<String, List<InfoSlot>> availableTimeSlots;
        try
        {
            availableTimeSlots = _appointmentRestService.getAvailableTimeSlots( search );
        }
        catch( HttpAccessException e )
        {
            throw new WebApplicationException(
                    Response.status( Response.Status.SERVICE_UNAVAILABLE ).entity( AppPropertiesService.getProperty( SOLR_EXCEPTION ) ).build( ) );
        }
        catch( JsonProcessingException e )
        {
            throw new WebApplicationException(
                    Response.status( Response.Status.BAD_REQUEST ).entity( AppPropertiesService.getProperty( JSON_EXCEPTION ) ).build( ) );
        }
        return Response.ok( availableTimeSlots ).build( );
    }

    @GET
    @Path( AppointmentRestConstants.SLASH + AppointmentRestConstants.PATH_API + AppointmentRestConstants.SLASH
            + AppointmentRestConstants.PATH_MANAGED_MEETING_POINTS )
    @Produces( MediaType.APPLICATION_JSON )
    public List<MeetingPointPOJO> getManagedMeetingPoints( )
    {
        List<MeetingPointPOJO> managedMeetingPoints;
        try
        {
            managedMeetingPoints = _appointmentRestService.getManagedMeetingPoints( );
        }
        catch( HttpAccessException e )
        {
            throw new WebApplicationException(
                    Response.status( Response.Status.SERVICE_UNAVAILABLE ).entity( AppPropertiesService.getProperty( SOLR_EXCEPTION ) ).build( ) );
        }
        catch( JsonProcessingException e )
        {
            throw new WebApplicationException(
                    Response.status( Response.Status.BAD_REQUEST ).entity( AppPropertiesService.getProperty( JSON_EXCEPTION ) ).build( ) );
        }
        return managedMeetingPoints;
    }
}
