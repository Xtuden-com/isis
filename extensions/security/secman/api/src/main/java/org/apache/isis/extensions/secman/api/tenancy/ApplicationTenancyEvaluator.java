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
package org.apache.isis.extensions.secman.api.tenancy;

import org.apache.isis.extensions.secman.api.user.ApplicationUser;

/**
 * Optional SPI interface to be implemented by a domain service, providing an alternative mechanism for evaluating the
 * application tenancy of the object being interacted with (the "what") and optionally also the tenancy of the user
 * making the call (the "who").
 *
 * @see #handles(Class)
 */
public interface ApplicationTenancyEvaluator {

    /**
     * Whether this evaluator can determine the tenancy of the specified domain entity (such as <tt>ToDoItem</tt>)
     * being interacted with (the "what").
     *
     * <p>
     *     This method is also called to determine if the evaluator is also able to determine the tenancy of the
     *     security module's own {@link ApplicationUser}, ie the "who" is
     *     doing the interacting.  If the evaluator does not handle the class, then the fallback behaviour is
     *     to invoke {@link ApplicationUser#getAtPath()}} on the {@link ApplicationUser} and use the
     *     path from that.
     * </p>
     */
    boolean handles(Class<?> cls);

    String hides(Object domainObject, ApplicationUser applicationUser);

    String disables(Object domainObject, ApplicationUser applicationUser);


}
