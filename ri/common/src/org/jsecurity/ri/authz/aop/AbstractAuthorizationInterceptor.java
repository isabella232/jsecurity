package org.jsecurity.ri.authz.aop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.authz.AuthorizationContext;
import org.jsecurity.authz.AuthorizedAction;
import org.jsecurity.authz.Authorizer;
import org.jsecurity.authz.UnauthorizedException;
import org.jsecurity.context.SecurityContext;

/**
 * This class is an abstraction of AOP method interceptor behavior specific to JSecurity that
 * leaves AOP implementation specifics to be handled by subclass implementations.  Shared behavior
 * is defined in this class.
 *
 * <p>Different frameworks represent Method Invocations (MI) in different ways, so this class
 * aggregates as much JSecurity interceptor behavior as possible, leaving framework MI details to
 * subclasses via template methods.
 *
 * @since 0.2
 * @author Les Hazlewood
 */
public abstract class AbstractAuthorizationInterceptor {

    protected transient final Log log = LogFactory.getLog( getClass() );

    private Authorizer authorizer;

    public AbstractAuthorizationInterceptor(){}

    public void setAuthorizer( Authorizer authorizer ) {
        this.authorizer = authorizer;
    }

    public void init() throws Exception {
        if ( this.authorizer == null ) {
            String msg = "authorizer property must be set";
            throw new IllegalStateException( msg );
        }
    }

    protected Object invoke( final Object implSpecificMethodInvocation ) throws Throwable {

        AuthorizationContext authzCtx = SecurityContext.getAuthorizationContext();

        if ( authzCtx != null ) {
            AuthorizedAction action = createAuthzAction( implSpecificMethodInvocation );
            //will throw an exception if not authorized to execute the action:
            this.authorizer.checkAuthorization( authzCtx, action );
        } else {
            String msg = "No AuthorizationContext available via " +
                         SecurityContext.class.getName() + ".getAuthorizationContext() " +
                         "(User not authenticated?).  Authorization failed.";
            throw new UnauthorizedException( msg );
        }

        //authzCtx was found, and it determined the AOP invocation chain should proceed:
        return continueInvocation( implSpecificMethodInvocation );
    }

    protected abstract AuthorizedAction createAuthzAction( Object implSpecificMethodInvocation );

    protected abstract Object continueInvocation( Object implSpecificMethodInvocation ) throws Throwable;
}