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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import fr.paris.lutece.plugins.appointment.modules.rest.pojo.AppointmentSlotsSearchPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.InfoSlot;
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.inject.Inject;

/**
 * A search engine document without form id, without URL or with a URL without starting date must not turn the whole
 * answer into an HTTP 500: it is skipped, the valid slots are returned.
 */
public class AppointmentSlotsServiceInvalidDocTest extends LuteceTestCase
{
    private static final String VALID_URL = "http://localhost/site/jsp/site/Portal.jsp?page=appointment&view=getAppointmentForm&id_form=7&starting_date_time=2030-01-07T09:00";
    private static final String DOCS = "{\"response\":{\"docs\":["
            + "{\"uid_form_string\":\"mp1\",\"url\":\"" + VALID_URL + "\"},"
            + "{},"
            + "{\"uid_form_string\":\"mp1\"},"
            + "{\"uid_form_string\":\"mp2\",\"url\":\"http://localhost/site/jsp/site/Portal.jsp?page=appointment&id_form=8\"},"
            + "{\"uid_form_string\":\"mp2\",\"url\":\"not a uri at all\"}"
            + "]}}";
    private static HttpServer _server;

    // the search engine stub (system property, above the Lutece configuration) before the Lutece test context starts
    static
    {
        try
        {
            _server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
            _server.createContext( "/solr/select", exchange -> {
                exchange.getRequestBody( ).readAllBytes( );
                byte [ ] body = DOCS.getBytes( StandardCharsets.UTF_8 );
                exchange.getResponseHeaders( ).add( "Content-Type", "application/json" );
                exchange.getResponseHeaders( ).add( "Connection", "close" );
                exchange.sendResponseHeaders( 200, body.length );
                try ( OutputStream out = exchange.getResponseBody( ) )
                {
                    out.write( body );
                }
            } );
            _server.start( );
            System.setProperty( "appointment-rest.solr.base_url", "http://127.0.0.1:" + _server.getAddress( ).getPort( ) + "/solr" );
        }
        catch( IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    @Inject
    private AppointmentSlotsService _service;

    @AfterAll
    static void stop( )
    {
        _server.stop( 0 );
    }

    @Test
    public void testInvalidDocumentsAreSkipped( ) throws Exception
    {
        AppointmentSlotsSearchPOJO search = new AppointmentSlotsSearchPOJO( List.of( "mp1", "mp2" ), LocalDate.of( 2030, 1, 7 ), LocalDate.of( 2030, 1, 8 ),
                null, 1 );
        Map<String, List<InfoSlot>> slots = _service.getAvailableTimeSlotsAsList( search );

        assertEquals( 1, slots.size( ), "only the meeting point with a valid slot: " + slots );
        assertEquals( 1, slots.get( "mp1" ).size( ) );
        assertEquals( LocalDate.of( 2030, 1, 7 ).atTime( 9, 0 ), slots.get( "mp1" ).get( 0 ).getDateTime( ) );
    }
}
