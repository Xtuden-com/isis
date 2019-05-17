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

package org.apache.isis.core.security.authentication.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.manager.RegistrationDetails;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

public class AuthenticationManagerStandard implements AuthenticationManager {

    private final Map<String, String> userByValidationCode = _Maps.newHashMap();

    private final List<Authenticator> authenticators = _Lists.newArrayList();

    private RandomCodeGenerator randomCodeGenerator;
    

    // //////////////////////////////////////////////////////////
    // init
    // //////////////////////////////////////////////////////////

    /**
     * Will default the {@link #setRandomCodeGenerator(RandomCodeGenerator)
     * RandomCodeGenerator}, but {@link Authenticator}(s) must have been
     * {@link #addAuthenticator(Authenticator) added}.
     * @param deploymentCategory
     */
    @Override
    public final void init() {
        defaultRandomCodeGeneratorIfNecessary();
        addDefaultAuthenticators();
        if (authenticators.size() == 0) {
            throw new IsisException("No authenticators specified");
        }
        for (final Authenticator authenticator : authenticators) {
            authenticator.init();
        }
    }

    private void defaultRandomCodeGeneratorIfNecessary() {
        if (randomCodeGenerator == null) {
            randomCodeGenerator = new RandomCodeGenerator10Chars();
        }
    }

    /**
     * optional hook method
     */
    protected void addDefaultAuthenticators() {
    }

    @Override
    public void shutdown() {
        for (final Authenticator authenticator : authenticators) {
            authenticator.shutdown();
        }
    }

    // //////////////////////////////////////////////////////////
    // Session Management (including authenticate)
    // //////////////////////////////////////////////////////////

    
    @Override
    public synchronized final AuthenticationSession authenticate(final AuthenticationRequest request) {
        if (request == null) {
            return null;
        }

        final Collection<Authenticator> compatibleAuthenticators = 
                _Lists.filter(authenticators, AuthenticatorFuncs.compatibleWith(request));
        if (compatibleAuthenticators.size() == 0) {
            throw new NoAuthenticatorException("No authenticator available for processing " + request.getClass().getName());
        }
        for (final Authenticator authenticator : compatibleAuthenticators) {
            final AuthenticationSession authSession = authenticator.authenticate(request, getUnusedRandomCode());
            if (authSession != null) {
                userByValidationCode.put(authSession.getValidationCode(), authSession.getUserName());
                return authSession;
            }
        }
        return null;
    }

    private String getUnusedRandomCode() {
        String code;
        do {
            code = randomCodeGenerator.generateRandomCode();
        } while (userByValidationCode.containsKey(code));

        return code;
    }

    
    @Override
    public final boolean isSessionValid(final AuthenticationSession session) {
        final String userName = userByValidationCode.get(session.getValidationCode());
        return session.hasUserNameOf(userName);
    }

    
    @Override
    public void closeSession(final AuthenticationSession session) {
        List<Authenticator> authenticators = getAuthenticators();
        for (Authenticator authenticator : authenticators) {
            authenticator.logout(session);
        }
        userByValidationCode.remove(session.getValidationCode());
    }

    // //////////////////////////////////////////////////////////
    // Authenticators
    // //////////////////////////////////////////////////////////

    
    public final void addAuthenticator(final Authenticator authenticator) {
        authenticators.add(authenticator);
    }

    
    public void addAuthenticatorToStart(final Authenticator authenticator) {
        authenticators.add(0, authenticator);
    }

    
    public List<Authenticator> getAuthenticators() {
        return Collections.unmodifiableList(authenticators);
    }


    
    @Override
    public boolean register(final RegistrationDetails registrationDetails) {
        for (final Registrar registrar : getRegistrars()) {
            if (registrar.canRegister(registrationDetails.getClass())) {
                return registrar.register(registrationDetails);
            }
        }
        return false;
    }

    
    @Override
    public boolean supportsRegistration(final Class<? extends RegistrationDetails> registrationDetailsClass) {
        for (final Registrar registrar : getRegistrars()) {
            if (registrar.canRegister(registrationDetailsClass)) {
                return true;
            }
        }
        return false;
    }

    
    public List<Registrar> getRegistrars() {
        return asAuthenticators(getAuthenticators());
    }

    private static List<Registrar> asAuthenticators(final List<Authenticator> authenticators2) {
        return stream(authenticators2)
                .map(Registrar.AS_REGISTRAR_ELSE_NULL)
                .filter(Registrar.NON_NULL)
                .collect(Collectors.toList());
    }

    // //////////////////////////////////////////////////////////
    // RandomCodeGenerator
    // //////////////////////////////////////////////////////////


    /**
     * For injection; will {@link #defaultRandomCodeGeneratorIfNecessary()
     * default} otherwise.
     */
    public void setRandomCodeGenerator(final RandomCodeGenerator randomCodeGenerator) {
        assert randomCodeGenerator != null;
        this.randomCodeGenerator = randomCodeGenerator;
    }

    // //////////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////////

    private final static ToString<AuthenticationManagerStandard> toString =
            ToString.<AuthenticationManagerStandard>toString("class", obj->obj.getClass().getSimpleName())
            .thenToString("authenticators", obj->""+obj.authenticators.size())
            .thenToString("users", obj->""+obj.userByValidationCode.size());
    
    @Override
    public String toString() {
        return toString.toString(this);
    }

    // //////////////////////////////////////////////////////////
    // Injected (constructor)
    // //////////////////////////////////////////////////////////

    protected IsisConfiguration getConfiguration() {
        return _Config.getConfiguration();
    }

}