/*
 * Copyright (C) 2005 Les Hazlewood
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

package org.jsecurity.ri.authz.module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.authz.AuthorizedAction;
import org.jsecurity.authz.method.MethodInvocation;
import org.jsecurity.authz.module.AuthorizationModule;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Abstract class providing common functionality across modules that process metadata
 * (annotations).  Primarily provides automatic support for the
 * {@link org.jsecurity.authz.module.AuthorizationModule#supports supports} method.  This allows support for any
 * arbitrary number of annotations - simply create a subclass of this one and add an instance of
 * that subclass to the {@link org.jsecurity.ri.authz.module.ModularAuthorizer ModularAuthorizer}'s
 * set of {@link org.jsecurity.ri.authz.module.ModularAuthorizer#setAuthorizationModules authorizationModule}s.
 *
 * @see org.jsecurity.ri.authz.module.ModularAuthorizer#setAuthorizationModules
 *
 * @since 0.1
 * @author Les Hazlewood
 */
public abstract class AnnotationAuthorizationModule implements AuthorizationModule {

    protected transient final Log log = LogFactory.getLog( getClass() );

    Class<? extends Annotation> annotationClass;

    public AnnotationAuthorizationModule(){}

    public void init() {
        if ( annotationClass == null ) {
            String msg = "annotationClass property must be set";
            throw new IllegalStateException( msg );
        }
    }

    public void setAnnotationClass( Class<? extends Annotation> annotationClass ) {
        this.annotationClass = annotationClass;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return this.annotationClass;
    }

    public boolean supports( AuthorizedAction action ) {
        if ( action != null && (action instanceof MethodInvocation) ) {
            return supports( ((MethodInvocation)action).getMethod() );
        }

        return false;
    }

    protected boolean supports( Method m ) {
        return ( m != null && ( m.getAnnotation( getAnnotationClass() ) != null ) );
    }
}