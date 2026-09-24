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

import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse
{
    private List<Detail> _detail = new ArrayList<>( );

    public List<Detail> getDetail( )
    {
        return _detail;
    }

    public void setDetail( List<Detail> detail )
    {
        this._detail = detail;
    }

    public void addDetail( Detail d )
    {
        _detail.add( d );
    }

    public static class Detail
    {
        private List<Object> _loc = new ArrayList<>( );
        private String _msg;
        private String _type;

        public Detail( List<Object> loc, String msg, String type )
        {
            this._loc = loc;
            this._msg = msg;
            this._type = type;
        }

        public Detail( Object loc, String msg, String type )
        {
            this._loc.add( loc );
            this._msg = msg;
            this._type = type;
        }

        public List<Object> getLoc( )
        {
            return _loc;
        }

        public void setLoc( List<Object> loc )
        {
            this._loc = loc;
        }

        public String getMsg( )
        {
            return _msg;
        }

        public void setMsg( String msg )
        {
            this._msg = msg;
        }

        public String getType( )
        {
            return _type;
        }

        public void setType( String type )
        {
            this._type = type;
        }
    }
}
