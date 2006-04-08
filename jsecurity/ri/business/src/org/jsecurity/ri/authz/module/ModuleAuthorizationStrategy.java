/*
 * Copyright (C) 2005 Jeremy Haile
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

import org.jsecurity.authz.AuthorizationContext;
import org.jsecurity.authz.AuthorizedAction;
import org.jsecurity.authz.module.AuthorizationModule;
import org.jsecurity.authz.module.AuthorizationVote;

import java.util.Map;


/**
 * Strategy that determines whether or not a user is authorized based on the
 * votes of all {@link org.jsecurity.authz.module.AuthorizationModule}s provided by
 * the {@link ModuleAuthorizer}.
 *
 * @since 0.1
 * @author Jeremy Haile
 */
public interface ModuleAuthorizationStrategy {

    /**
     * Determines if a user is authorized to perform the given action based
     * on a set of {@link org.jsecurity.authz.module.AuthorizationVote}s that were returned from a
     * set of {@link org.jsecurity.authz.module.AuthorizationModule}s.
     *
     * @param context the context of the user being authorized.
     * @param action the action that the user is requesting authorization for.
     * @param votes the votes returned by {@link org.jsecurity.authz.module.AuthorizationModule}s.
     * @return true if the user should be authorized based on the votes, or
     * false otherwise.
     */
    boolean isAuthorized( AuthorizationContext context,
                          AuthorizedAction action,
                          Map<AuthorizationModule, AuthorizationVote> votes );
    
}
