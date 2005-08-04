/*
 * Copyright (C) 2005 Les A. Hazlewood
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
package org.jsecurity.authz;

import java.io.Serializable;
import java.security.Permission;
import java.security.Principal;
import java.util.List;
import java.util.Set;

/**
 * Provides all access control behavior for an authenticated user.  An <tt>AuthorizationContext</tt>
 * can only be acquired upon a successful login, as access control behavior must be associated
 * with a known identity.
 *
 * @author Les Hazlewood
 */
public interface AuthorizationContext {

    Principal getPrincipal();

    boolean hasRole( Serializable roleIdentifier );

    boolean[] hasRoles( List<Serializable> roleIdentifiers );

    boolean hasAllRoles( Set<Serializable> roleIdentifiers );

    boolean hasPermission( Permission permission );

    boolean[] hasPermissions( List<Permission> permissions );

    boolean hasAllPermissions( Set<Permission> permissions );

    void checkPermission( Permission permission ) throws AuthorizationException;

    void checkPermissions( Set<Permission> permissions ) throws AuthorizationException;

    Object getAttribute( Object key );

    void setAttribute( Object key, Object value );

    Object removeAttribute( Object key );
}