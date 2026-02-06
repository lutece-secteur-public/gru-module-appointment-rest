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
package fr.paris.lutece.plugins.appointment.modules.rest.rs.filter;

import fr.paris.lutece.plugins.appointment.modules.rest.util.contsants.AppointmentRestConstants;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class QueryParamValidator
{

    public static final String MEETING_IDS_REQUIRED = "appointment-rest.validation.required.meetingsids";
    public static final String START_DATE_REQUIRED = "appointment-rest.validation.required.startdate";
    public static final String START_DATE_INVALID = "appointment-rest.validation.invalid.startdate";
    public static final String END_DATE_REQUIRED = "appointment-rest.validation.required.enddate";
    public static final String END_DATE_INVALID = "appointment-rest.validation.invalid.enddate";
    public static final String REQUIRED = "Required";

    private QueryParamValidator()
    {
    }

    public static ValidationErrorResponse validate( HttpServletRequest request )
    {
        ValidationErrorResponse errors = new ValidationErrorResponse( );
        if ( request.getParameter( AppointmentRestConstants.JSON_TAG_MEETING_POINT_IDS ) == null )
        {
            errors.addDetail( new ValidationErrorResponse.Detail( AppointmentRestConstants.JSON_TAG_MEETING_POINT_IDS,
                    AppPropertiesService.getProperty( MEETING_IDS_REQUIRED, "Champs meeting_point_ids requis" ), REQUIRED) );
        }
        if ( request.getParameter( AppointmentRestConstants.JSON_TAG_START_DATE ) == null )
        {
            errors.addDetail( new ValidationErrorResponse.Detail( AppointmentRestConstants.JSON_TAG_START_DATE,
                    AppPropertiesService.getProperty( START_DATE_REQUIRED, "Champs start_date requis" ), REQUIRED) );
        }
        else
            if ( !isValideDate( request.getParameter( AppointmentRestConstants.JSON_TAG_START_DATE ) ) )
            {
                errors.addDetail( new ValidationErrorResponse.Detail( AppointmentRestConstants.JSON_TAG_START_DATE,
                        AppPropertiesService.getProperty( START_DATE_INVALID, "Format du champs start_date invalide" ), "Invalid" ) );
            }
        if ( request.getParameter( AppointmentRestConstants.JSON_TAG_END_DATE ) == null )
        {
            errors.addDetail( new ValidationErrorResponse.Detail( AppointmentRestConstants.JSON_TAG_END_DATE,
                    AppPropertiesService.getProperty( END_DATE_REQUIRED, "Champs end_date requis" ), REQUIRED) );
        }
        else
            if ( !isValideDate( request.getParameter( AppointmentRestConstants.JSON_TAG_END_DATE ) ) )
            {
                errors.addDetail( new ValidationErrorResponse.Detail( AppointmentRestConstants.JSON_TAG_END_DATE,
                        AppPropertiesService.getProperty( END_DATE_INVALID, "Format du champs end_date invalide" ), "Invalid" ) );
            }
        return errors;
    }

    public static boolean isValideDate( String dateStr )
    {
        try
        {
            LocalDate.parse( dateStr, AppointmentRestConstants.SEARCH_DATE_FORMATTER );
        }
        catch( DateTimeParseException e )
        {
            return false;
        }
        return true;
    }
}
