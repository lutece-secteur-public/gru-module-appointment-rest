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
package fr.paris.lutece.plugins.appointment.modules.rest.business.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.SolrAppointmentSlotPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.SolrMeetingPointPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.util.contsants.AppointmentRestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.signrequest.BasicAuthorizationAuthenticator;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class SolrProvider implements IAppointmentDataProvider
{

    // Constants
    private static final String PROVIDER_NAME = "solr.provider";
    private static final String PROPERTY_ENCODE_URI_ENCODING = "search.encode.uri.encoding";
    private static final String DEFAULT_URI_ENCODING = "ISO-8859-1";
    private static final String PROPERTY_SOLR_BASE_URL = "appointment-rest.solr.base_url";
    private static final String PROPERTY_SOLR_ROWS = "appointment-rest.solr.rows";
    private static final String PROPERTY_SOLR_USERNAME = "appointment-rest.solr.username";
    private static final String PROPERTY_SOLR_PASSWORD = "appointment-rest.solr.password";

    private static SolrProvider _instance;
    private static String _strBaseUrl;
    private static String _strRows;
    private static BasicAuthorizationAuthenticator _authenticator;

    @Override
    public String getName( )
    {
        return PROVIDER_NAME;
    }

    public static synchronized SolrProvider getInstance( )
    {
        if ( _instance == null )
        {
            _instance = new SolrProvider( );
            _instance.init( );
        }

        return _instance;
    }

    private synchronized void init( )
    {
        if ( _strBaseUrl == null )
        {
            _strBaseUrl = AppPropertiesService.getProperty( PROPERTY_SOLR_BASE_URL );
        }
        _strRows = AppPropertiesService.getProperty( PROPERTY_SOLR_ROWS, "10000" );
        _authenticator = new BasicAuthorizationAuthenticator( AppPropertiesService.getProperty( PROPERTY_SOLR_USERNAME ),
                AppPropertiesService.getProperty( PROPERTY_SOLR_PASSWORD ) );
    }

    @Override
    public String getAvailableTimeSlot( List<String> appointmentIds, LocalDate startDate, LocalDate endDate, Integer documentNumber )
            throws HttpAccessException, JsonProcessingException
    {
        HttpAccess httpAccess = new HttpAccess( );

        StringBuilder query = generateAvailableTimeSlotSolrQuery( appointmentIds, startDate, endDate, documentNumber );

        AppLogService.debug( "Solr query getAvailableTimeSlot : {}", query );

        String strUrl = _strBaseUrl + query;

        String response = httpAccess.doGet( strUrl, _authenticator, null );
        ObjectMapper mapper = new ObjectMapper( );
        JsonNode jsonNode = mapper.readTree( response );
        return jsonNode.get( "response" ).get( "docs" ).toString( );
    }

    private static StringBuilder generateAvailableTimeSlotSolrQuery( List<String> appointmentIds, LocalDate startDate, LocalDate endDate,
            Integer documentNumber )
    {
        String strStartDate = startDate.atStartOfDay( ).format( AppointmentRestConstants.SOLR_DATE_FORMATTER );

        if ( LocalDate.now( ).isEqual( startDate ) )
        {
            strStartDate = AppointmentRestConstants.SOLR_QUERY_NOW;
        }

        StringBuilder query = new StringBuilder( );
        query.append( AppointmentRestConstants.SOLR_QUERY_SELECT + AppointmentRestConstants.SOLR_QUERY_Q )
                .append( encoder( AppointmentRestConstants.SOLR_QUERY_Q_VALUE ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FIELD );
        query.append( encoder(
                SolrAppointmentSlotPOJO.SOLR_FIELD_UID + AppointmentRestConstants.SOLR_QUERY_COMMA + SolrAppointmentSlotPOJO.SOLR_FIELD_DATE + AppointmentRestConstants.SOLR_QUERY_COMMA + SolrAppointmentSlotPOJO.SOLR_FIELD_URL ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append( SolrAppointmentSlotPOJO.SOLR_FIELD_DATE ).append( encoder( AppointmentRestConstants.SOLR_QUERY_COLON ) );
        query.append( encoder( AppointmentRestConstants.SOLR_QUERY_LB ) ).append( strStartDate ).append( encoder( AppointmentRestConstants.SOLR_QUERY_TO ) )
                .append( LocalTime.MAX.atDate( endDate ).format( AppointmentRestConstants.SOLR_DATE_FORMATTER ) )
                .append( encoder( AppointmentRestConstants.SOLR_QUERY_RB ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append( SolrAppointmentSlotPOJO.SOLR_FIELD_UID ).append( encoder( AppointmentRestConstants.SOLR_QUERY_COLON ) )
                .append( AppointmentRestConstants.SOLR_QUERY_LP );
        query.append( encoder( appointmentIds.stream( ).collect( Collectors.joining( StringUtils.SPACE ) ) ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_RP );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append(
                encoder( SolrAppointmentSlotPOJO.SOLR_FIELD_DAY_OPEN + AppointmentRestConstants.SOLR_QUERY_COLON + AppointmentRestConstants.SOLR_QUERY_TRUE ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append(
                encoder( SolrAppointmentSlotPOJO.SOLR_FIELD_ENABLED + AppointmentRestConstants.SOLR_QUERY_COLON + AppointmentRestConstants.SOLR_QUERY_TRUE ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append( encoder(
                SolrAppointmentSlotPOJO.SOLR_FIELD_APPOINTMENT_ACTIVE + AppointmentRestConstants.SOLR_QUERY_COLON + AppointmentRestConstants.SOLR_QUERY_TRUE ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append( SolrAppointmentSlotPOJO.SOLR_FIELD_NB_CONSECUTIVES_SLOTS ).append( encoder( AppointmentRestConstants.SOLR_QUERY_COLON ) );
        query.append( encoder( AppointmentRestConstants.SOLR_QUERY_LB ) ).append( documentNumber ).append( encoder( AppointmentRestConstants.SOLR_QUERY_TO ) )
                .append( AppointmentRestConstants.SOLR_QUERY_STAR ).append( encoder( AppointmentRestConstants.SOLR_QUERY_RB ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append( SolrAppointmentSlotPOJO.SOLR_FIELD_MAX_CONSECUTIVES_SLOTS ).append( encoder( AppointmentRestConstants.SOLR_QUERY_COLON ) );
        query.append( encoder( AppointmentRestConstants.SOLR_QUERY_LB ) ).append( documentNumber ).append( encoder( AppointmentRestConstants.SOLR_QUERY_TO ) )
                .append( AppointmentRestConstants.SOLR_QUERY_STAR ).append( encoder( AppointmentRestConstants.SOLR_QUERY_RB ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_ROWS + _strRows );
        return query;
    }

    @Override
    public String getManagedMeetingPoints( ) throws HttpAccessException
    {
        HttpAccess httpAccess = new HttpAccess( );

        StringBuilder query = generateManagedMeetingPoints( );

        AppLogService.debug( "Solr query getManagedMeetingPoints : {}", query );

        String strUrl = _strBaseUrl + query;

        return httpAccess.doGet( strUrl, _authenticator, null );
    }

    private static StringBuilder generateManagedMeetingPoints( )
    {
        StringBuilder query = new StringBuilder( );
        query.append( AppointmentRestConstants.SOLR_QUERY_SELECT + AppointmentRestConstants.SOLR_QUERY_Q )
                .append( encoder( AppointmentRestConstants.SOLR_QUERY_Q_VALUE ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FIELD );
        query.append( encoder(
                SolrMeetingPointPOJO.SOLR_FIELD_UID + AppointmentRestConstants.SOLR_QUERY_COMMA + SolrMeetingPointPOJO.SOLR_FIELD_TITLE + AppointmentRestConstants.SOLR_QUERY_COMMA + SolrMeetingPointPOJO.SOLR_FIELD_ADDRESS + AppointmentRestConstants.SOLR_QUERY_COMMA + SolrMeetingPointPOJO.SOLR_FIELD_GEOLOC + AppointmentRestConstants.SOLR_QUERY_COMMA + SolrMeetingPointPOJO.SOLR_FIELD_ICON_URL ) );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_QUERY );
        query.append( SolrMeetingPointPOJO.SOLR_FIELD_TYPE ).append( encoder( AppointmentRestConstants.SOLR_QUERY_COLON ) )
                .append( SolrMeetingPointPOJO.SOLR_FIELD_TYPE_APPOINTMENT );
        query.append( AppointmentRestConstants.SOLR_QUERY_GROUP );
        query.append( AppointmentRestConstants.SOLR_QUERY_GROUP_FIELD + SolrMeetingPointPOJO.SOLR_FIELD_UID );
        query.append( AppointmentRestConstants.SOLR_QUERY_FILTER_ROWS + _strRows );
        return query;
    }

    public static String encoder( String strSource )
    {
        String strEncoded = StringUtils.EMPTY;

        try
        {
            strEncoded = URLEncoder.encode( strSource, getEncoding( ) );
        }
        catch( UnsupportedEncodingException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }

        return strEncoded;
    }

    public static String getEncoding( )
    {
        return AppPropertiesService.getProperty( PROPERTY_ENCODE_URI_ENCODING, DEFAULT_URI_ENCODING );
    }
}
