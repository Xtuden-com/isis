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
package demoapp.dom.annotDomain.DomainObject.publishing;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled.DomainObjectPublishingDisabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdoEntities;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled.DomainObjectPublishingEnabledMetaAnnotOverriddenJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled.DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    describedAs = "Deletes one publishing enabled entity and one publishing disabled entity"
)
public class DomainObjectPublishingVm_delete {

    private final DomainObjectPublishingVm domainObjectPublishingVm;
    public DomainObjectPublishingVm_delete(DomainObjectPublishingVm domainObjectPublishingVm) {
        this.domainObjectPublishingVm = domainObjectPublishingVm;
    }

    @MemberOrder(sequence = "3.0")
    public DomainObjectPublishingVm act(
            @Nullable DomainObjectPublishingEnabledJdo enabledJdo
            , @Nullable DomainObjectPublishingDisabledJdo disabledJdo
            , @Nullable DomainObjectPublishingEnabledMetaAnnotatedJdo metaAnnotatedJdo
            , @Nullable DomainObjectPublishingEnabledMetaAnnotOverriddenJdo metaAnnotOverriddenJdo
            ) {
        if(enabledJdo != null) {
            publishingEnabledJdoEntities.remove(enabledJdo);
        }
        if(disabledJdo != null) {
            publishingDisabledJdoEntities.remove(disabledJdo);
        }
        if(metaAnnotatedJdo != null) {
            publishingEnabledMetaAnnotatedJdoEntities.remove(metaAnnotatedJdo);
        }
        if(metaAnnotOverriddenJdo != null) {
            publishingEnabledMetaAnnotOverriddenJdoEntities.remove(metaAnnotOverriddenJdo);
        }
        return domainObjectPublishingVm;
    }
    public DomainObjectPublishingEnabledJdo default0Act() {
        return publishingEnabledJdoEntities.first().get();
    }
    public DomainObjectPublishingDisabledJdo default1Act() {
        return publishingDisabledJdoEntities.first().get();
    }
    public DomainObjectPublishingEnabledMetaAnnotatedJdo default2Act() {
        return publishingEnabledMetaAnnotatedJdoEntities.first().get();
    }
    public DomainObjectPublishingEnabledMetaAnnotOverriddenJdo default3Act() {
        return publishingEnabledMetaAnnotOverriddenJdoEntities.first().get();
    }
    public String disableAct() {
        if(!publishingEnabledJdoEntities.first().isPresent()) { return "No EnabledJdo to delete"; }
        if(!publishingDisabledJdoEntities.first().isPresent()) { return "No DisabledJdo to delete"; }
        if(!publishingEnabledMetaAnnotatedJdoEntities.first().isPresent()) { return "No MetaAnnotated to delete"; }
        if(!publishingEnabledMetaAnnotOverriddenJdoEntities.first().isPresent()) { return "No MetaAnnotated But Overridden to delete"; }
        return null;
    }

    @Inject DomainObjectPublishingEnabledJdoEntities publishingEnabledJdoEntities;
    @Inject DomainObjectPublishingDisabledJdoEntities publishingDisabledJdoEntities;
    @Inject DomainObjectPublishingEnabledMetaAnnotatedJdoEntities publishingEnabledMetaAnnotatedJdoEntities;
    @Inject DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities publishingEnabledMetaAnnotOverriddenJdoEntities;
}
//end::class[]
