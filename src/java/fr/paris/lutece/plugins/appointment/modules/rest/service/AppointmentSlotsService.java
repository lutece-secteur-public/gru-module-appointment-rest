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
package fr.paris.lutece.plugins.appointment.modules.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.appointment.modules.rest.business.providers.IAppointmentDataProvider;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.AppointmentSlotsSearchPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.InfoSlot;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.SolrAppointmentSlotPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.util.contsants.AppointmentRestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.url.UrlItem;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import fr.paris.lutece.portal.service.util.AppException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class AppointmentSlotsService
{

    public static final String LUTECE_BASE_URL = "appointment-rest.lutece.base.url";

    @Inject
    private IAppointmentDataProvider _dataProvider;

    private String _baseUrl;

    @PostConstruct
    public void init( )
    {
        AppLogService.info( "DatatProvider loaded : {}", _dataProvider.getName( ) );
        _baseUrl = AppPropertiesService.getProperty( LUTECE_BASE_URL );
    }

    public Map<String, List<InfoSlot>> getAvailableTimeSlotsAsList( AppointmentSlotsSearchPOJO search )
    {
        String response = null;
        try
        {
            response = _dataProvider.getAvailableTimeSlot( search.getAppointmentIds( ), search.getStartDate( ), search.getEndDate( ),
                    search.getDocumentNumber( ) );

            ObjectMapper mapper = new ObjectMapper( );
            TypeReference<List<SolrAppointmentSlotPOJO>> typeReference = new TypeReference<List<SolrAppointmentSlotPOJO>>( )
            {
            };
            List<SolrAppointmentSlotPOJO> solrResponse = mapper.readValue( response, typeReference );

            return solrResponse.stream( ).collect( Collectors.groupingBy( SolrAppointmentSlotPOJO::getUidFormString, Collectors
                    .mapping( a -> new InfoSlot( buildDate( a.getUrl( ) ), buildUrl( a.getUrl( ), search.getDocumentNumber( ) ) ), Collectors.toList( ) ) ) );
        }
        catch (HttpAccessException | JsonProcessingException e)
        {
            AppLogService.error( e.getMessage( ), e );
            throw new AppException( e.getMessage( ), e );
        }
    }

    private static LocalDateTime buildDate( String strUrl )
    {
        return LocalDateTime.parse( getParamFromUrl( strUrl ).get( AppointmentRestConstants.PARAMETER_STARTING_DATE ),
                AppointmentRestConstants.SOLR_RESPONSE_DATE_FORMATTER );
    }

    public String buildUrl( String strUrl, Integer nbPlaceToTake )
    {
        Map<String, String> mapParamaters = getParamFromUrl( strUrl );

        UrlItem url = new UrlItem( _baseUrl /* + AppointmentRestConstants.XPAGE_APPOINTMENT_ANTS */ );
        url.addParameter( AppointmentRestConstants.PARAMETER_VIEW, AppointmentRestConstants.VIEW_APPOINTMENT_ANTS );
        url.addParameter( AppointmentRestConstants.PARAMETER_ID_FORM, mapParamaters.get( AppointmentRestConstants.PARAMETER_ID_FORM ) );
        url.addParameter( AppointmentRestConstants.PARAMETER_STARTING_DATE, mapParamaters.get( AppointmentRestConstants.PARAMETER_STARTING_DATE ) );
        url.addParameter( AppointmentRestConstants.PARAMETER_NB_PLACES_TO_TAKE, nbPlaceToTake );
        return url.getUrl( );
    }

    private static Map<String, String> getParamFromUrl( String strUrl )
    {
        List<NameValuePair> params;
        try
        {

            params = URLEncodedUtils.parse( new URI( strUrl ), StandardCharsets.UTF_8 );
        }
        catch( URISyntaxException e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw new AppException( e.getMessage( ), e );
        }
        return params.stream( ).collect( Collectors.toMap( NameValuePair::getName, NameValuePair::getValue ) );
    }

}
