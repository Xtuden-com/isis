/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.applib.spec;

/**
 * An implementation of the <i>Specification</i> pattern, as described in Eric
 * Evans' <i>Domain Driven Design</i>, p224.
 */
// tag::refguide[]
public interface Specification {

    // end::refguide[]
    /**
     * If <tt>null</tt> then satisfied, otherwise is reason why the
     * specification is not satisfied.
     */
    // tag::refguide[]
    String satisfies(Object obj);

}
// end::refguide[]