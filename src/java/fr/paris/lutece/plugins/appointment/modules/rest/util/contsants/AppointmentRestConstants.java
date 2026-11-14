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
package fr.paris.lutece.plugins.appointment.modules.rest.util.contsants;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class AppointmentRestConstants
{
    /** The Constant SLASH. */
    public static final String SLASH = "/";

    /** The Constant PATH_API. */
    public static final String PATH_API = "api";

    /** The Constant PATH_AVAILABLE_SLOTS. */
    public static final String PATH_AVAILABLE_SLOTS = "availableTimeSlots";
    public static final String PATH_MANAGED_MEETING_POINTS = "getManagedMeetingPoints";

    public static final String XPAGE_APPOINTMENT_ANTS = "page=appointmentants";
    public static final String PARAMETER_VIEW = "view";
    public static final String VIEW_APPOINTMENT_ANTS = "predemandeForm";
    public static final String PARAMETER_ID_FORM = "id_form";
    public static final String PARAMETER_STARTING_DATE = "starting_date_time";
    public static final String PARAMETER_NB_PLACES_TO_TAKE = "nbPlacesToTake";

    public static final String JSON_TAG_MEETING_POINT_IDS = "meeting_point_ids";
    public static final String JSON_TAG_START_DATE = "start_date";
    public static final String JSON_TAG_END_DATE = "end_date";
    public static final String JSON_TAG_REASON = "reason";
    public static final String JSON_TAG_DOCUMENTS_NUMBER = "documents_number";

    public static final String SOLR_QUERY_SELECT = "/select";
    public static final String SOLR_QUERY_Q = "?q=";
    public static final String SOLR_QUERY_Q_VALUE = "*:*";
    public static final String SOLR_QUERY_FIELD = "&fl=";
    public static final String SOLR_QUERY_FILTER_QUERY = "&fq=";
    public static final String SOLR_QUERY_FILTER_ROWS = "&rows=";
    public static final String SOLR_QUERY_GROUP = "&group=true";
    public static final String SOLR_QUERY_GROUP_FIELD = "&group.field=";
    public static final String SOLR_QUERY_COMMA = ",";
    public static final String SOLR_QUERY_COLON = ":";
    public static final String SOLR_QUERY_TO = " TO ";
    public static final String SOLR_QUERY_LB = "[";
    public static final String SOLR_QUERY_RB = "]";
    public static final String SOLR_QUERY_RP = ")";
    public static final String SOLR_QUERY_LP = "(";
    public static final String SOLR_QUERY_STAR = "*";
    public static final String SOLR_QUERY_TRUE = "true";
    public static final String SOLR_QUERY_NOW = "NOW";

    public static final DateTimeFormatter SEARCH_DATE_FORMATTER = DateTimeFormatter.ofPattern( "uuuu-MM-dd" ).withResolverStyle( ResolverStyle.STRICT );
    public static final DateTimeFormatter SOLR_DATE_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss.SS'Z'" );
    public static final DateTimeFormatter SOLR_RESPONSE_DATE_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm" );

    private AppointmentRestConstants()
    {
    }
}
