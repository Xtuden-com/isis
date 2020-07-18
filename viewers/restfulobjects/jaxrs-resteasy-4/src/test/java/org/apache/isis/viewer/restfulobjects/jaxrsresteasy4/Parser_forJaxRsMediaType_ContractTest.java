/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//TODO ISIS-2374 refactor copy
package org.apache.isis.viewer.restfulobjects.jaxrsresteasy4;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.util.MediaTypes;
import org.apache.isis.viewer.restfulobjects.applib.util.Parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

abstract class Parser_forJaxRsMediaType_ContractTest {

    @Test
    public void forJaxRsMediaType() {
        final Parser<MediaType> parser = Parser.forJaxRsMediaType();

        for (final javax.ws.rs.core.MediaType v : new javax.ws.rs.core.MediaType[] {
                javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML_TYPE,
                javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE,
                javax.ws.rs.core.MediaType.APPLICATION_XHTML_XML_TYPE,
                MediaTypes.parse(RestfulMediaType.APPLICATION_JSON_OBJECT)
        }) {
            final String asString = parser.asString(v);
            final javax.ws.rs.core.MediaType valueOf = parser.valueOf(asString);
            assertThat(v, is(equalTo(valueOf)));
        }
    }

}