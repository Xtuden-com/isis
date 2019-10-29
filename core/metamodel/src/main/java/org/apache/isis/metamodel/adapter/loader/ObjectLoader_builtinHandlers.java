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
package org.apache.isis.metamodel.adapter.loader;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.loader.ObjectLoader.Handler;
import org.apache.isis.metamodel.adapter.loader.ObjectLoader.ObjectLoadRequest;
import org.apache.isis.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.Data;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
final class ObjectLoader_builtinHandlers {

    // -- NULL GUARD
    
    @Data
    public static class GuardAgainstNull implements Handler {
        
        private MetaModelContext metaModelContext;
        
        @Override
        public boolean isHandling(ObjectLoadRequest objectLoadRequest) {
            
            if(objectLoadRequest==null) {
                return true;
            }
            
            val spec = objectLoadRequest.getObjectSpecification();
            if(spec == null) {
                // eg "NONEXISTENT:123"
                return true;
            }

            // we don't guard against the identifier being null, because, this is ok 
            // for services and values
            return false;
        }

        @Override
        public ManagedObject loadObject(ObjectLoadRequest objectLoadRequest) {
            return null; // yes null
        }

    }
    
    // -- MANAGED BEANS

    @Data
    public static class LoadService implements Handler {
        
        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(ObjectLoadRequest objectLoadRequest) {
            
            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isManagedBean();
        }

        @Override
        public ManagedObject loadObject(ObjectLoadRequest objectLoadRequest) {
            
            val spec = objectLoadRequest.getObjectSpecification();
            val beanName = spec.getSpecId().asString();
            
            val servicePojo = metaModelContext.getServiceRegistry()
                .lookupRegisteredBeanById(beanName)
                .map(ManagedBeanAdapter::getInstance)
                .flatMap(Can::getFirst)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "loader: %s loading beanName %s", 
                        this.getClass().getName(), beanName));
            
            return ManagedObject.of(spec, servicePojo);
        }

    }
    
    // -- VALUES
    
    @Data
    public static class LoadValue implements Handler {

        private MetaModelContext metaModelContext;
        
        @Override
        public boolean isHandling(ObjectLoadRequest objectLoadRequest) {
            
            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isValue();
        }

        @Override
        public ManagedObject loadObject(ObjectLoadRequest objectLoadRequest) {
            
            // cannot load a value
            
            val spec = objectLoadRequest.getObjectSpecification();
            throw _Exceptions.illegalArgument(
                    "cannot load a value, loader: %s loading ObjectSpecification %s", 
                        this.getClass().getName(), spec);
        }

    }

    // -- VIEW MODELS
    
    @Data
    public static class LoadViewModel implements Handler {
        
        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(ObjectLoadRequest objectLoadRequest) {
            
            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isViewModel();
        }

        @Override
        public ManagedObject loadObject(ObjectLoadRequest objectLoadRequest) {
            
            val spec = objectLoadRequest.getObjectSpecification();
            val viewModelFacet = spec.getFacet(ViewModelFacet.class);
            if(viewModelFacet == null) {
                throw _Exceptions.illegalArgument(
                        "ObjectSpecification is missing a ViewModelFacet: %s", spec);
            }
            
            val memento = objectLoadRequest.getObjectIdentifier();
            final Object viewModelPojo;
            if(viewModelFacet.getRecreationMechanism().isInitializes()) {
                viewModelPojo = this.instantiateAndInjectServices(spec);
                viewModelFacet.initialize(viewModelPojo, memento);
            } else {
                viewModelPojo = viewModelFacet.instantiate(spec.getCorrespondingClass(), memento);
            }
            
            return ManagedObject.of(spec, viewModelPojo);
        }
        
        private Object instantiateAndInjectServices(ObjectSpecification spec) {

            val type = spec.getCorrespondingClass();
            if (type.isArray()) {
                return Array.newInstance(type.getComponentType(), 0);
            }

            if (Modifier.isAbstract(type.getModifiers())) {
                throw _Exceptions.illegalArgument("Cannot create an instance of an abstract class '%s', "
                        + "loader: %s loading ObjectSpecification %s", 
                        type, this.getClass().getName(), spec);
            }

            final Object newInstance;
            try {
                newInstance = type.newInstance();
            } catch (final IllegalAccessException | InstantiationException e) {
                throw _Exceptions.illegalArgument("Failed to create instance of type '%s', "
                        + "loader: %s loading ObjectSpecification %s", 
                        type, this.getClass().getName(), spec);
            }

            metaModelContext.getServiceInjector().injectServicesInto(newInstance);
            return newInstance;
        }

    }

    // -- ENTITIES
    
    @Data
    public static class LoadEntity implements Handler {
        
        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(ObjectLoadRequest objectLoadRequest) {
            
            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isEntity();
        }

        @Override
        public ManagedObject loadObject(ObjectLoadRequest objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                throw _Exceptions.illegalArgument(
                        "ObjectSpecification is missing an EntityFacet: %s", spec);
            }
            
            val identifier = objectLoadRequest.getObjectIdentifier();
            val entityPojo = entityFacet.fetchByIdentifier(spec, identifier);
            
            metaModelContext.getServiceInjector().injectServicesInto(entityPojo);
            
            return ManagedObject.of(spec, entityPojo);
        }

    }
    
    // -- UNKNOWN LOAD REQUEST
    
    @Data
    public static class LoadOther implements Handler {
        
        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(ObjectLoadRequest objectLoadRequest) {
            return true; // the last handler in the chain
        }

        @Override
        public ManagedObject loadObject(ObjectLoadRequest objectLoadRequest) {

            // unknown object load request
            
            throw _Exceptions.illegalArgument(
                    "unknown object load request, loader: %s loading ObjectSpecification %s", 
                        this.getClass().getName(), objectLoadRequest.getObjectSpecification());

        }

    }

}