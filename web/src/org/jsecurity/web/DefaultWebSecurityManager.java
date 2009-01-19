/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jsecurity.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.mgt.DefaultSecurityManager;
import org.jsecurity.realm.Realm;
import org.jsecurity.session.mgt.SessionManager;
import org.jsecurity.util.LifecycleUtils;
import org.jsecurity.web.session.DefaultWebSessionManager;
import org.jsecurity.web.session.ServletContainerSessionManager;
import org.jsecurity.web.session.WebSessionManager;

import java.util.Collection;

/**
 * SecurityManager implementation that should be used in web-based applications or any application that requires
 * HTTP connectivity (SOAP, http remoting, etc).
 *
 * @author Les Hazlewood
 * @since 0.2
 */
public class DefaultWebSecurityManager extends DefaultSecurityManager {

    //TODO - complete JavaDoc

    private static final Log log = LogFactory.getLog(DefaultWebSecurityManager.class);

    public static final String HTTP_SESSION_MODE = "http";
    public static final String JSECURITY_SESSION_MODE = "jsecurity";

    /**
     * The key that is used to store subject principals in the session.
     */
    public static final String PRINCIPALS_SESSION_KEY = DefaultWebSecurityManager.class.getName() + "_PRINCIPALS_SESSION_KEY";

    /**
     * The key that is used to store whether or not the user is authenticated in the session.
     */
    public static final String AUTHENTICATED_SESSION_KEY = DefaultWebSecurityManager.class.getName() + "_AUTHENTICATED_SESSION_KEY";

    private String sessionMode = HTTP_SESSION_MODE; //default

    public DefaultWebSecurityManager() {
        WebSessionManager sm = new DefaultWebSessionManager();
        applySessionManager(sm);
        setRememberMeManager(new WebRememberMeManager());
        setSubjectFactory(new WebSubjectFactory(this, sm));
    }

    public DefaultWebSecurityManager(Realm singleRealm) {
        this();
        setRealm(singleRealm);
    }

    public DefaultWebSecurityManager(Collection<Realm> realms) {
        this();
        setRealms(realms);
    }

    /**
     * Sets the path used to store the remember me cookie.  This determines which paths
     * are able to view the remember me cookie.
     *
     * @param rememberMeCookiePath the path to use for the remember me cookie.
     */
    public void setRememberMeCookiePath(String rememberMeCookiePath) {
        ((WebRememberMeManager) getRememberMeManager()).setCookiePath(rememberMeCookiePath);
    }

    /**
     * Sets the maximum age allowed for the remember me cookie.  This basically sets how long
     * a user will be remembered by the "remember me" feature.  Used when calling
     * {@link javax.servlet.http.Cookie#setMaxAge(int) maxAge}.  Please see that JavaDoc for the semantics on the
     * repercussions of negative, zero, and positive values for the maxAge.
     *
     * @param rememberMeMaxAge the maximum age for the remember me cookie.
     */
    public void setRememberMeCookieMaxAge(Integer rememberMeMaxAge) {
        ((WebRememberMeManager) getRememberMeManager()).setCookieMaxAge(rememberMeMaxAge);
    }

    private DefaultWebSessionManager getSessionManagerForCookieAttributes() {
        SessionManager sessionManager = getSessionManager();
        if (!(sessionManager instanceof DefaultWebSessionManager)) {
            String msg = "The convenience passthrough methods for setting session id cookie attributes " +
                    "are only available when the underlying SessionManager implementation is " +
                    DefaultWebSessionManager.class.getName() + ", which is enabled by default when the " +
                    "sessionMode is 'jsecurity'.";
            throw new IllegalStateException(msg);
        }
        return (DefaultWebSessionManager) sessionManager;
    }

    public void setSessionIdCookieName(String name) {
        getSessionManagerForCookieAttributes().setSessionIdCookieName(name);
    }

    public void setSessionIdCookiePath(String path) {
        getSessionManagerForCookieAttributes().setSessionIdCookiePath(path);
    }

    public void setSessionIdCookieMaxAge(int maxAge) {
        getSessionManagerForCookieAttributes().setSessionIdCookieMaxAge(maxAge);
    }

    public void setSessionIdCookieSecure(boolean secure) {
        getSessionManagerForCookieAttributes().setSessionIdCookieSecure(secure);
    }

    public String getSessionMode() {
        return sessionMode;
    }

    public void setSessionMode(String sessionMode) {
        String mode = sessionMode;
        if (mode == null) {
            throw new IllegalArgumentException("sessionMode argument cannot be null.");
        }
        mode = sessionMode.toLowerCase();
        if (!HTTP_SESSION_MODE.equals(mode) && !JSECURITY_SESSION_MODE.equals(mode)) {
            String msg = "Invalid sessionMode [" + sessionMode + "].  Allowed values are " +
                    "public static final String constants in the " + getClass().getName() + " class: '"
                    + HTTP_SESSION_MODE + "' or '" + JSECURITY_SESSION_MODE + "', with '" +
                    HTTP_SESSION_MODE + "' being the default.";
            throw new IllegalArgumentException(msg);
        }
        boolean recreate = this.sessionMode == null || !this.sessionMode.equals(sessionMode);
        this.sessionMode = sessionMode;
        if (recreate) {
            LifecycleUtils.destroy(getSessionManager());
            SessionManager sessionManager = newSessionManagerInstance();
            applySessionManager(sessionManager);
            setSubjectFactory(new WebSubjectFactory(this, (WebSessionManager) sessionManager));
        }
    }

    public boolean isHttpSessionMode() {
        return this.sessionMode == null || this.sessionMode.equals(HTTP_SESSION_MODE);
    }

    protected SessionManager newSessionManagerInstance() {
        if (isHttpSessionMode()) {
            if (log.isInfoEnabled()) {
                log.info(HTTP_SESSION_MODE + " mode - enabling ServletContainerSessionManager (Http Sessions)");
            }
            return new ServletContainerSessionManager();
        } else {
            if (log.isInfoEnabled()) {
                log.info(JSECURITY_SESSION_MODE + " mode - enabling WebSessionManager (JSecurity heterogenous sessions)");
            }
            return new DefaultWebSessionManager();
        }
    }
}