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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.appointment.modules.rest.pojo.AppointmentSlotsSearchPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.InfoSlot;
import fr.paris.lutece.plugins.appointment.modules.rest.pojo.MeetingPointPOJO;
import fr.paris.lutece.plugins.appointment.modules.rest.service.IAppointmentRestService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Covers the answers of the appointment REST resource when the search engine cannot be reached.
 */
public class AppointmentRestTest extends LuteceTestCase
{
    private static final ObjectMapper MAPPER = new ObjectMapper( );

    /**
     * Build a resource whose service cannot reach the search engine.
     *
     * @return the resource
     */
    private static AppointmentRest unreachableSearchEngine( )
    {
        AppointmentRest rest = new AppointmentRest( );
        rest._appointmentRestService = new IAppointmentRestService( )
        {
            @Override
            public Map<String, List<InfoSlot>> getAvailableTimeSlots( AppointmentSlotsSearchPOJO search ) throws HttpAccessException
            {
                throw new HttpAccessException( "unreachable", null );
            }

            @Override
            public List<MeetingPointPOJO> getManagedMeetingPoints( ) throws HttpAccessException
            {
                throw new HttpAccessException( "unreachable", null );
            }
        };
        return rest;
    }

    /**
     * Check that an answer is a JSON error carrying a message in its detail field.
     *
     * @param response
     *            the answer
     * @throws Exception
     *             if the body is not JSON
     */
    private static void assertJsonError( Response response ) throws Exception
    {
        assertEquals( Response.Status.SERVICE_UNAVAILABLE.getStatusCode( ), response.getStatus( ) );
        assertEquals( MediaType.APPLICATION_JSON_TYPE, response.getMediaType( ) );
        JsonNode body = MAPPER.readTree( (String) response.getEntity( ) );
        assertTrue( body.path( "detail" ).isTextual( ) );
        assertFalse( body.path( "detail" ).asText( ).isEmpty( ) );
    }

    /**
     * The meeting points answer a JSON error when the search engine cannot be reached.
     *
     * @throws Exception
     *             if the body is not JSON
     */
    @Test
    public void testManagedMeetingPointsUnavailableAnswersJson( ) throws Exception
    {
        assertJsonError( unreachableSearchEngine( ).getManagedMeetingPoints( new MockHttpServletRequest( ) ) );
    }

    /**
     * The available slots answer a JSON error when the search engine cannot be reached.
     *
     * @throws Exception
     *             if the body is not JSON
     */
    @Test
    public void testAvailableTimeSlotsUnavailableAnswersJson( ) throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "meeting_point_ids", "9001" );
        request.addParameter( "start_date", "2026-10-01" );
        request.addParameter( "end_date", "2026-10-15" );
        assertJsonError( unreachableSearchEngine( ).getAvailableTimeSlots( request, List.of( "9001" ), "2026-10-01", "2026-10-15", null, null ) );
    }

    /**
     * A date that does not exist in the calendar is answered with the JSON validation error of the module.
     *
     * @throws Exception
     *             if the body is not JSON
     */
    @Test
    public void testImpossibleDateAnswersValidationError( ) throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "meeting_point_ids", "9001" );
        request.addParameter( "start_date", "2026-02-30" );
        request.addParameter( "end_date", "2026-03-15" );
        Response response = unreachableSearchEngine( ).getAvailableTimeSlots( request, List.of( "9001" ), "2026-02-30", "2026-03-15", null, null );
        assertEquals( 422, response.getStatus( ) );
        assertEquals( MediaType.APPLICATION_JSON_TYPE, response.getMediaType( ) );
        JsonNode detail = MAPPER.readTree( (String) response.getEntity( ) ).path( "detail" );
        assertEquals( 1, detail.size( ) );
        assertEquals( "start_date", detail.get( 0 ).path( "loc" ).get( 0 ).asText( ) );
        assertEquals( "Invalid", detail.get( 0 ).path( "type" ).asText( ) );
    }
}
