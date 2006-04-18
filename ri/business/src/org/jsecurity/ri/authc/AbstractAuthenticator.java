/*
 * Copyright (C) 2005 Jeremy Haile, Les Hazlewood
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the
 *
 * Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330
 * Boston, MA 02111-1307
 * USA
 *
 * Or, you may view it online at
 * http://www.opensource.org/licenses/lgpl-license.php
 */

package org.jsecurity.ri.authc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.Authenticator;
import org.jsecurity.authc.event.AuthenticationEvent;
import org.jsecurity.authc.module.AuthenticationInfo;
import org.jsecurity.authz.AuthorizationContext;
import org.jsecurity.ri.authc.event.AuthenticationEventFactory;
import org.jsecurity.ri.authc.event.AuthenticationEventSender;
import org.jsecurity.ri.authc.event.SimpleAuthenticationEventFactory;
import org.jsecurity.ri.authc.bind.AuthorizationContextBinder;
import org.jsecurity.ri.authc.bind.ThreadLocalAuthorizationContextBinder;
import org.jsecurity.ri.authz.AuthorizationContextFactory;
import org.jsecurity.ri.authz.support.SimpleAuthorizationContextFactory;

/**
 * Superclass for {@link Authenticator} implementations that performs the common work of wrapping a
 * returned {@link AuthorizationContext} using an {@link AuthorizationContextFactory} and binding
 * the context using an {@link org.jsecurity.ri.authc.bind.AuthorizationContextBinder}.  Subclasses should implement the {@link
 * #doAuthenticate(org.jsecurity.authc.AuthenticationToken)} method.
 *
 * @since 0.1
 * @author Jeremy Haile
 * @author Les Hazlewood
 */
public abstract class AbstractAuthenticator implements Authenticator {

    /*--------------------------------------------
    |             C O N S T A N T S             |
    ============================================*/

    /*--------------------------------------------
    |    I N S T A N C E   V A R I A B L E S    |
    ============================================*/
    /**
     * Commons logger.
     */
    protected Log log = LogFactory.getLog( getClass() );
    /**
     * Alias for the 'log' protected class attribute for subclass authors that may prefer one over
     * the other.
     */
    protected Log logger = log;

    /**
     * The factory used to wrap authorization context after authentication.
     */
    private AuthorizationContextFactory authContextFactory = new SimpleAuthorizationContextFactory();

    /**
     * The binder used to bind the authorization context so that it is accessible on subsequent
     * requests.
     */
    private AuthorizationContextBinder authzCtxBinder = new ThreadLocalAuthorizationContextBinder();

    private AuthenticationEventFactory authcEventFactory = new SimpleAuthenticationEventFactory();

    private AuthenticationEventSender authcEventSender = null;

    /*--------------------------------------------
    |         C O N S T R U C T O R S           |
    ============================================*/
    public AbstractAuthenticator(){}

    /*--------------------------------------------
    |  A C C E S S O R S / M O D I F I E R S    |
    ============================================*/
    protected AuthorizationContextFactory getAuthorizationContextFactory() {
        return authContextFactory;
    }


    public void setAuthorizationContextFactory( AuthorizationContextFactory authContextFactory ) {
        this.authContextFactory = authContextFactory;
    }


    public AuthorizationContextBinder getAuthorizationContextBinder() {
        return authzCtxBinder;
    }


    public void setAuthorizationContextBinder( AuthorizationContextBinder authContextBinder ) {
        this.authzCtxBinder = authContextBinder;
    }

    public AuthenticationEventFactory getAuthenticationEventFactory() {
        return authcEventFactory;
    }

    public void setAuthenticationEventFactory( AuthenticationEventFactory factory ) {
        this.authcEventFactory = factory;
    }

    public AuthenticationEventSender getAuthenticationEventSender() {
        return authcEventSender;
    }

    public void setAuthenticationEventSender( AuthenticationEventSender authcEventSender ) {
        this.authcEventSender = authcEventSender;
    }

    /*-------------------------------------------
     |               M E T H O D S               |
     ============================================*/

    protected AuthenticationEvent createFailureEvent( AuthenticationToken token,
                                                      AuthenticationException ae ) {
        AuthenticationEventFactory factory = getAuthenticationEventFactory();
        return factory.createFailureEvent( token, ae );
    }

    protected AuthenticationEvent createSuccessEvent( AuthenticationToken token,
                                                      AuthenticationInfo info ) {
        AuthenticationEventFactory factory = getAuthenticationEventFactory();
        return factory.createSuccessEvent( token, info );
    }

    protected void sendFailureEvent( AuthenticationToken token, AuthenticationException ae ) {
        AuthenticationEventSender sender = getAuthenticationEventSender();
        //only incur event creation overhead if the event can actually be sent:
        if ( sender != null ) {
            AuthenticationEvent event = createFailureEvent( token, ae );
            if ( event != null ) {
                send( event );
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug( "No AuthenticationEvent instance returned from " +
                               "'createFailureEvent' method call.  No failed authentication " +
                               "event will be sent." );
                }
            }
        } else {
            if ( log.isTraceEnabled() ) {
                log.trace( "No AuthenticationEventSender configured.  No failure event will " +
                           "be sent" );
            }
        }

    }

    protected void sendSuccessEvent( AuthenticationToken token, AuthenticationInfo info ) {
        AuthenticationEventSender sender = getAuthenticationEventSender();
        //only incur event creation overhead if the event can actually be sent:
        if ( sender != null ) {
            AuthenticationEvent event = createSuccessEvent( token, info );
            if ( event != null ) {
                send( event );
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug( "No AuthenticationEvent instance returned from " +
                            "'createSuccessEvent' method call.  No successful authentication " +
                            "event will be sent." );
                }
            }
        } else {
            if ( log.isTraceEnabled() ) {
                log.trace( "No AuthenticationEventSender configured.  No success event will " +
                           "be sent" );
            }
        }
    }

    protected void send( AuthenticationEvent event ) throws IllegalArgumentException {
        if ( event == null ) {
            throw new IllegalArgumentException( "AuthenticationEvent argument cannot be null" );
        }
        AuthenticationEventSender sender = getAuthenticationEventSender();
        if ( sender != null ) {
            try {
                sender.send( event );
            } catch ( Throwable t ) {
                if ( log.isWarnEnabled() ) {
                    log.warn( "Unable to send AuthenticationEvent [" + event + "]", t );
                }
            }
        } else {
            if ( log.isTraceEnabled() ) {
                log.trace( "No AuthenticationEventSender configured.  Event [" + event + "] will " +
                        "not be sent." );
            }
        }
    }

    protected AuthorizationContext createAuthorizationContext( AuthenticationInfo info ) {
        return getAuthorizationContextFactory().createAuthorizationContext( info );
    }

    protected void bind( AuthorizationContext authzCtx ) {
        getAuthorizationContextBinder().bindAuthorizationContext( authzCtx );
    }

    private void assertCreation( AuthorizationContext authzCtx ) throws IllegalStateException {
        if ( authzCtx == null ) {
            String msg = "Programming or configuration error - No AuthorizationContext was created after successful " +
                    "authentication.  Verify that you have either configured the " + getClass().getName() +
                    " instance with a proper " + AuthorizationContextFactory.class.getName() + " (easier) or " +
                    "that you have overridden the " + AbstractAuthenticator.class.getName() +
                    ".createAuthorizationContext( AuthenticationInfo info ) method.";
            throw new IllegalStateException( msg );
        }
    }

    public final AuthorizationContext authenticate( AuthenticationToken token )
            throws AuthenticationException {

        if ( log.isTraceEnabled() ) {
            log.trace( "Authentication request received for token [" + token + "]" );
        }

        AuthenticationInfo info;
        try {
            info = doAuthenticate( token );
            if ( info == null ) {
                throw new AuthenticationException( "Authentication token of type [" +
                    token.getClass() + "] could not be processed for authentication.  Check that " +
                    "the Authenticator is configured correctly." );
            }
        } catch ( AuthenticationException e ) {
            if ( log.isInfoEnabled() ) {
                log.info( "Authentication failed for token submission [" + token + "]" );
            }
            sendFailureEvent( token, e );
            throw e;
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Authentication successful.  Returned authentication info: [" + info + "]" );
        }

        AuthorizationContext authzCtx = createAuthorizationContext( info );

        assertCreation( authzCtx );

        bind( authzCtx );

        sendSuccessEvent( token, info );

        return authzCtx;
    }

    /**
     * Template design pattern hook for subclasses to implement specific authentication behavior.
     *
     * <p>Common behavior for most all common authentication attempts is encapsulated in the 
     * {@link #authenticate} method and that method invokes this one for custom behavior.
     *
     * <p><b>N.B.</b> Subclasses <em>should</em> throw some kind of
     * <tt>AuthenticationException</tt> if there is a problem during
     * authentication instead of returning <tt>null</tt>.  A <tt>null</tt> return value indicates
     * a configuration or programming error, since <tt>AuthenticationException</tt>s should
     * indicate any expected problem.
     *
     * @param token the authentication token encapsulating the user's login information.
     * @return an <tt>AuthenticationInfo</tt> object encapsulating the user's account information
     * important to JSecurity.  <tt>null</tt> should <em>not</em> be returned.
     * @throws AuthenticationException if there is a problem logging in the user.
     */
    protected abstract AuthenticationInfo doAuthenticate( AuthenticationToken token )
            throws AuthenticationException;
}