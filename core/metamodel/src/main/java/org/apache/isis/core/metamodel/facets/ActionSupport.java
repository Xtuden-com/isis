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
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils.MethodAndPpmConstructor;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ActionSupport {

    @Value @Builder
    public static class ActionSupportingMethodSearchRequest {

        public static enum ReturnType {
            TEXT,
            BOOLEAN, 
        }

        @NonNull FacetFactory.ProcessMethodContext processMethodContext;
        @NonNull Can<String> methodNames;
        @NonNull EnumSet<SearchAlgorithm> searchAlgorithms; 
        @NonNull ReturnType returnType;

        Class<?> additionalParamType;

        @Getter(lazy = true)
        Class<?>[] paramTypes = getProcessMethodContext().getMethod().getParameterTypes();

        Can<String> getSupporingMethodNameCandidates() {
            return methodNames;
        }
    }

    @FunctionalInterface
    public static interface SearchFunction {
        ActionSupportingMethodSearchResult search(
                ActionSupportingMethodSearchRequest searchRequest);
    }

    @RequiredArgsConstructor
    public static enum SearchAlgorithm
    implements SearchFunction {
        PPM(ActionSupport::findActionSupportingMethodWithPPMArg),
        ALL_PARAM_TYPES(ActionSupport::findActionSupportingMethodWithAllParamTypes),
        ;
        private final SearchFunction searchFunction;
        public ActionSupportingMethodSearchResult search(
                final ActionSupportingMethodSearchRequest searchRequest) {
            return searchFunction.search(searchRequest);
        }
    }

    @Value(staticConstructor = "of")
    public static class ActionSupportingMethodSearchResult {
        Method supportingMethod;
        Class<?> returnType;
        Optional<Constructor<?>> ppmFactory;
    }

    public static void findActionSupportingMethods(
            final ActionSupportingMethodSearchRequest searchRequest,
            final Consumer<ActionSupportingMethodSearchResult> onMethodFound) {
        
        for (val searchAlgorithm : searchRequest.searchAlgorithms) { 

            val searchResult = searchAlgorithm.search(searchRequest); 
            
            if(log.isDebugEnabled()) {
                log.debug("search algorithm={} {}",
                        searchAlgorithm.name(),
                        searchResult != null ? "FOUND " : "",
                        toString(searchRequest));
            }

            if (searchResult != null) {
                onMethodFound.accept(searchResult);
            }
        }
        
    }

    // -- SEARCH ALGORITHMS

    private final static ActionSupportingMethodSearchResult findActionSupportingMethodWithPPMArg(
            final ActionSupportingMethodSearchRequest searchRequest) {
        
        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getMethodNames();
                
        val additionalParamTypes = Can.ofNullable(searchRequest.getAdditionalParamType());
        
        final MethodAndPpmConstructor supportingMethodAndPpmConstructor;
        
        switch(searchRequest.getReturnType()) {
        case BOOLEAN:
            supportingMethodAndPpmConstructor = MethodFinder2
                .findMethodWithPPMArg_returningBoolean(type, methodNames, paramTypes, additionalParamTypes);
            break;
        case TEXT:
            supportingMethodAndPpmConstructor = MethodFinder2
                .findMethodWithPPMArg_returningText(type, methodNames, paramTypes, additionalParamTypes);
            break;
        default:
            supportingMethodAndPpmConstructor = null;
        }
        
        if(log.isDebugEnabled()) {
            
            log.debug(". signature (<any>, {}) {}", 
                    toString(additionalParamTypes.toArray(_Constants.emptyClasses)),
                    supportingMethodAndPpmConstructor != null 
                        ? "found -> " + supportingMethodAndPpmConstructor.getSupportingMethod() 
                        : "");
        }
        
        if(supportingMethodAndPpmConstructor != null) {
            val searchResult = ActionSupportingMethodSearchResult
                    .of( 
                            supportingMethodAndPpmConstructor.getSupportingMethod(), 
                            supportingMethodAndPpmConstructor.getSupportingMethod().getReturnType(),
                            Optional.of(supportingMethodAndPpmConstructor.getPpmFactory()));
            return searchResult;
        }
        
        return null;
    }

    private final static ActionSupportingMethodSearchResult findActionSupportingMethodWithAllParamTypes(
            final ActionSupportingMethodSearchRequest searchRequest) {
        
        val processMethodContext = searchRequest.getProcessMethodContext();
        val type = processMethodContext.getCls();
        val paramTypes = searchRequest.getParamTypes();
        val methodNames = searchRequest.getMethodNames();
        
        val additionalParamType = searchRequest.getAdditionalParamType();
        val additionalParamCount = additionalParamType!=null ? 1 : 0;
        
        final int paramsConsideredCount = paramTypes.length + additionalParamCount; 
        if(paramsConsideredCount>=0) {
        
            val paramTypesToLookFor = concat(paramTypes, paramsConsideredCount, additionalParamType);
            
            final Method supportingMethod;
            
            switch(searchRequest.getReturnType()) {
            case BOOLEAN:
                supportingMethod = MethodFinder2
                    .findMethod_returningBoolean(type, methodNames, paramTypesToLookFor);
                break;
            case TEXT:
                supportingMethod = MethodFinder2
                    .findMethod_returningText(type, methodNames, paramTypesToLookFor);
                break;
            default:
                supportingMethod = null;
            }
            
            if(log.isDebugEnabled()) {
                log.debug(". signature ({}) {}", 
                        toString(paramTypesToLookFor),
                        supportingMethod != null ? "found -> " + supportingMethod : "");
            }
            
            if(supportingMethod != null) {
                val searchResult = ActionSupportingMethodSearchResult
                        .of(supportingMethod, supportingMethod.getReturnType(), Optional.empty());
                return searchResult;
            }
        }
        
        return null;
    }
    
    // -- PARAM UTIL
    
    private static Class<?>[] concat(
            final Class<?>[] paramTypes,
            final int paramsConsidered,
            @Nullable final Class<?> additionalParamType) {

        if(paramsConsidered>paramTypes.length) {
            val msg = String.format("paramsConsidered %d exceeds size of paramTypes %d", 
                    paramsConsidered, paramTypes.length);
            throw new IllegalArgumentException(msg);
        }
        
        val paramTypesConsidered = paramsConsidered<paramTypes.length
                ? Arrays.copyOf(paramTypes, paramsConsidered)
                        : paramTypes;
                
        val withAdditional = additionalParamType!=null
                ? _Arrays.combine(paramTypesConsidered, additionalParamType)
                        : paramTypesConsidered;
                
        return withAdditional;
    }
    
    // -- DEBUG LOGGING
    
    private static String toString(
            ActionSupportingMethodSearchRequest searchRequest) {
        
        return String.format("%s.%s(%s) : %s",
                searchRequest.getProcessMethodContext().getCls().getSimpleName(),
                searchRequest.getSupporingMethodNameCandidates(),
                toString(searchRequest.getParamTypes()),
                searchRequest.getReturnType().name()
                );
    }
    
    private static String toString(Class<?>[] types) {
        return _NullSafe.stream(types)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(","));
    }

}