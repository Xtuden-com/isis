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
package demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class DomainObjectPublishingDisabledJdoEntities {

    public Optional<DomainObjectPublishingDisabledJdo> find(final String value) {
        return repositoryService.firstMatch(DomainObjectPublishingDisabledJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<DomainObjectPublishingDisabledJdo> all() {
        return repositoryService.allInstances(DomainObjectPublishingDisabledJdo.class);
    }

    public Optional<DomainObjectPublishingDisabledJdo> first() {
        return all().stream().findFirst();
    }

    public DomainObjectPublishingDisabledJdo create(String newValue) {
        return repositoryService.persistAndFlush(new DomainObjectPublishingDisabledJdo(newValue));
    }

    public void remove(DomainObjectPublishingDisabledJdo disabledJdo) {
        repositoryService.removeAndFlush(disabledJdo);
    }

    @Inject
    RepositoryService repositoryService;

}
