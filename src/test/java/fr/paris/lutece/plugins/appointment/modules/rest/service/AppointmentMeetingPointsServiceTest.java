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

import fr.paris.lutece.plugins.appointment.modules.rest.pojo.MeetingPointPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.SolrMeetingPointPOJO;
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class AppointmentMeetingPointsServiceTest extends LuteceTestCase
{
    @Inject
    private AppointmentMeetingPointsService _appointmentMeetingPointsService;

    @Test
    public void testTransform( )
    {
        SolrMeetingPointPOJO pojo1 = new SolrMeetingPointPOJO( );
        pojo1.setTitle( "Pojo1" );
        pojo1.setUid( "uid1" );
        pojo1.setAddressText( "adresse1 12345 ville1" );
        pojo1.setGeoloc( "11,22" );

        SolrMeetingPointPOJO pojo2 = new SolrMeetingPointPOJO( );
        pojo2.setTitle( "Pojo2" );
        pojo2.setUid( "uid2" );
        pojo2.setAddressText( "adresse complete" );
        pojo2.setGeoloc( "22,33" );

        List<SolrMeetingPointPOJO> solrMeetings = Arrays.asList( pojo1, pojo2 );

        List<MeetingPointPOJO> manegedPoints = _appointmentMeetingPointsService.transform( solrMeetings );
        assertEquals( 2, solrMeetings.size( ) );
        assertEquals( "11", manegedPoints.get( 0 ).getLatitude( ) );
        assertEquals( "22", manegedPoints.get( 0 ).getLongitude( ) );
        assertEquals( "ville1", manegedPoints.get( 0 ).getCityName( ) );
        assertEquals( "12345", manegedPoints.get( 0 ).getZipCode( ) );
        assertEquals( "adresse1", manegedPoints.get( 0 ).getPublicAdress( ) );

        assertEquals( "22", manegedPoints.get( 1 ).getLatitude( ) );
        assertEquals( "33", manegedPoints.get( 1 ).getLongitude( ) );
        assertNull( manegedPoints.get( 1 ).getCityName( ) );
        assertNull( manegedPoints.get( 1 ).getZipCode( ) );
        assertEquals( "adresse complete", manegedPoints.get( 1 ).getPublicAdress( ) );
    }

}
