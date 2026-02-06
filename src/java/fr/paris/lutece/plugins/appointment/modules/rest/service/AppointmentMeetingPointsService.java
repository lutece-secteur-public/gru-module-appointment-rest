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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.appointment.modules.rest.business.providers.IAppointmentDataProvider;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.MeetingPointPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.SolrMeetingPointPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.SolrResponseMeetingPointPOJO;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class AppointmentMeetingPointsService
{

    private static final String PROPERTY_WEBSITE_URL = "appointment-rest.website.url";

    @Inject
    private IAppointmentDataProvider _dataProvider;

    private String _strWebsiteURL;
    private static final Pattern ZIP_CITY_PATTERN = Pattern.compile( "(.*)(\\d{5})\\s+(.+)" );

    @PostConstruct
    public void init( )
    {
        AppLogService.info( "DatatProvider loaded : {}", _dataProvider.getName( ) );
        _strWebsiteURL = AppPropertiesService.getProperty( PROPERTY_WEBSITE_URL, MeetingPointPOJO.DEFAULT_WEBSITE_URL_RDV );
    }

    public List<MeetingPointPOJO> getManagedMeetingPoints( )
    {
        List<MeetingPointPOJO> manegedPoints;

        try
        {
            String response = null;
            response = _dataProvider.getManagedMeetingPoints( );

            ObjectMapper objectMapper = new ObjectMapper( );
            SolrResponseMeetingPointPOJO solrResponse = null;

            solrResponse = objectMapper.readValue( response, SolrResponseMeetingPointPOJO.class );

            List<SolrMeetingPointPOJO> solrMeetings = new ArrayList<>( );

            for ( SolrResponseMeetingPointPOJO.Group group : solrResponse.getGrouped( ).getGroupedByUidForm( ).getGroups( ) )
            {
                if ( group.getGroupValue( ) != null )
                {
                    solrMeetings.addAll( group.getDocList( ).getDocs( ) );
                }
            }

            manegedPoints = transform( solrMeetings );

            return manegedPoints;
        }
        catch( IOException | HttpAccessException e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw new AppException( e.getMessage( ), e );
        }
    }

    public List<MeetingPointPOJO> transform( List<SolrMeetingPointPOJO> solrMeetings )
    {
        List<MeetingPointPOJO> meetingPoints = new ArrayList<>( );

        for ( SolrMeetingPointPOJO solrMeeting : solrMeetings )
        {
            MeetingPointPOJO meeting = new MeetingPointPOJO( );
            meeting.setId( solrMeeting.getUid( ) );
            if ( solrMeeting.getGeoloc( ) != null && solrMeeting.getGeoloc( ).contains( "," ) )
            {
                String[] geoloc = solrMeeting.getGeoloc( ).split( "," );
                meeting.setLatitude( geoloc[0].trim( ) );
                meeting.setLongitude( geoloc[1].trim( ) );
            }
            if ( solrMeeting.getAddressText( ) != null )
            {
                Matcher matcher = ZIP_CITY_PATTERN.matcher( solrMeeting.getAddressText( ) );
                if ( matcher.find( ) )
                {
                    String publicAddress = matcher.group(1).trim();

                    while (publicAddress.endsWith(","))
                    {
                        publicAddress = publicAddress.substring(0, publicAddress.length() - 1).trim();
                    }
                    meeting.setPublicAdress( publicAddress );
                    meeting.setZipCode( matcher.group( 2 ) );
                    meeting.setCityName( matcher.group( 3 ).trim( ) );
                }
                else
                {
                    meeting.setPublicAdress( solrMeeting.getAddressText( ) );
                }
            }

            meeting.setName( solrMeeting.getTitle( ) );
            meeting.setWebsite( _strWebsiteURL );
            meetingPoints.add( meeting );
            meeting.setCityLogo( solrMeeting.getIconUrl( ) );
        }

        return meetingPoints;
    }

}
