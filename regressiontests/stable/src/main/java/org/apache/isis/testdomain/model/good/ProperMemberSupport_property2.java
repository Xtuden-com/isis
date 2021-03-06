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
package org.apache.isis.testdomain.model.good;

import java.util.Set;

import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.RequiredArgsConstructor;

@Mixin @RequiredArgsConstructor
public class ProperMemberSupport_property2 {
    
    private final ProperMemberSupport holder;

    //@Action(semantics=SAFE)   // <-- inferred (required)
    //@ActionLayout(contributed=ASSOCIATION)  // <-- inferred (required)
    @Property
    @PropertyLayout(named= "foo", describedAs = "bar")
    public String $$() {
        return holder.toString();
    }
    
    // -- PROPERLY DECLARED SUPPORTING METHODS 
    
    @Model
    public Set<String> autoComplete$$(@MinLength(3) String search) {
        return null;
    }
    
    @Model
    public Set<String> choices$$() {
        return null;
    }
    
    @Model
    public String default$$() {
        return "";
    }

    @Model
    public String disable$$() {
        return null;
    }
    
    @Model
    public boolean hide$$() {
        return false;
    }

    
}
