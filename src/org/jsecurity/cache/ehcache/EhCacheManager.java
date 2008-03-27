/*
 * Copyright 2005-2008 Jeremy Haile, Les Hazlewood
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jsecurity.cache.ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsecurity.cache.Cache;
import org.jsecurity.cache.CacheException;
import org.jsecurity.cache.CacheManager;
import org.jsecurity.session.mgt.eis.CachingSessionDAO;
import org.jsecurity.util.Destroyable;
import org.jsecurity.util.Initializable;

import java.io.InputStream;

/**
 * <p>JSecurity <code>CacheManager</code> implementation utilizing the Ehcache framework for all cache functionality.</p>
 *
 * <p>This implementation requires EhCache 1.2 and above. Make sure EhCache 1.1 or earlier
 * is not in the classpath or it will not work.</p>
 *
 * <p>See http://ehcache.sf.net for documentation on EhCache</p>
 *
 * @since 0.2
 * @author Jeremy Haile
 * @author Les Hazlewood
 */
public class EhCacheManager implements CacheManager, Initializable, Destroyable {

    public static final String DEFAULT_ACTIVE_SESSIONS_CACHE_NAME = CachingSessionDAO.ACTIVE_SESSION_CACHE_NAME;
    public static final int DEFAULT_ACTIVE_SESSIONS_CACHE_MAX_ELEM_IN_MEM = 20000;
    public static final int DEFAULT_ACTIVE_SESSIONS_DISK_EXPIRY_THREAD_INTERVAL_SECONDS = 600;

    /**
     * Commons-logging logger
     */
    protected final transient Log log = LogFactory.getLog( getClass() );

    /**
     * The EhCache cache manager used by this implementation to create caches.
     */
    protected net.sf.ehcache.CacheManager manager;
    private boolean cacheManagerImplicitlyCreated = false;
    /**
     * Classpath file location - without a leading slash, it is relative to the current class.
     */
    private String cacheManagerConfigFile = "ehcache.xml";

    public net.sf.ehcache.CacheManager getCacheManager() {
        return manager;
    }

    public void setCacheManager( net.sf.ehcache.CacheManager manager ) {
        this.manager = manager;
    }

    public String getCacheManagerConfigFile() {
        return this.cacheManagerConfigFile;
    }

    public void setCacheManagerConfigFile( String classpathLocation ) {
        this.cacheManagerConfigFile = classpathLocation;
    }

    protected InputStream getCacheManagerConfigFileInputStream() {
        String classpathLocation = getCacheManagerConfigFile();
        return getClass().getResourceAsStream( classpathLocation );
    }

    /**
     * Loads an existing EhCache from the cache manager, or starts a new cache if one is not found.
     *
     * @param name the name of the cache to load/create.
     */
    public final Cache getCache( String name ) throws CacheException {

        if ( log.isTraceEnabled() ) {
            log.trace( "Loading a new EhCache cache named [" + name + "]" );
        }

        try {
            net.sf.ehcache.Cache cache = getCacheManager().getCache( name );
            if ( cache == null ) {
                if ( log.isInfoEnabled() ) {
                    log.info( "Could not find a specific ehcache configuration for cache named [" + name + "]; using defaults." );
                }
                if ( name.equals( DEFAULT_ACTIVE_SESSIONS_CACHE_NAME ) ) {
                    if ( log.isInfoEnabled() ) {
                        log.info( "Creating " + DEFAULT_ACTIVE_SESSIONS_CACHE_NAME + " cache with default JSecurity " +
                            "session cache settings." );
                    }
                    cache = buildDefaultActiveSessionsCache();
                    manager.addCache( cache );
                } else {
                    manager.addCache( name );
                }

                cache = manager.getCache( name );
                
                if ( log.isInfoEnabled() ) {
                    log.info( "Started EHCache named [" + name + "]" );
                }
            } else {
                if ( log.isInfoEnabled() ) {
                    log.info("Using preconfigured EHCache named [" + cache.getName() + "]" );
                }
            }
            return new EhCache( cache );
        } catch ( net.sf.ehcache.CacheException e ) {
            throw new CacheException( e );
        }
    }

    private net.sf.ehcache.Cache buildDefaultActiveSessionsCache() throws CacheException {
        return new net.sf.ehcache.Cache( DEFAULT_ACTIVE_SESSIONS_CACHE_NAME,
            DEFAULT_ACTIVE_SESSIONS_CACHE_MAX_ELEM_IN_MEM,
            true,
            true,
            0,
            0,
            true,
            DEFAULT_ACTIVE_SESSIONS_DISK_EXPIRY_THREAD_INTERVAL_SECONDS );
    }

    /**
     * Initializes this instance.
     * <p/>
     * <p>If a {@link #setCacheManager CacheManager} has been
     * explicitly set (e.g. via Dependency Injection or programatically) prior to calling this
     * method, this method does nothing.
     * <p>However, if no <tt>CacheManager</tt> has been set, the default Ehcache singleton will be initialized, where
     * Ehcache will look for an <tt>ehcache.xml</tt> file at the root of the classpath.  If one is not found,
     * Ehcache will use its own failsafe configuration file.
     * <p/>
     * <p>Because JSecurity cannot use the failsafe defaults (failsafe expunges cached objects after 2 minutes,
     * something not desireable for JSecurity sessions), this class manages an internal default configuration for
     * this case.</p>
     *
     * @throws org.jsecurity.cache.CacheException
     *          if there are any CacheExceptions thrown by EhCache.
     * @see net.sf.ehcache.CacheManager#create
     */
    public final void init() throws CacheException {
        try {
            net.sf.ehcache.CacheManager cacheMgr = getCacheManager();
            if ( cacheMgr == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "cacheManager property not set.  Constructing CacheManager instance... " );
                }
                //using the CacheManager constructor, the resulting instance is _not_ a VM singleton
                //(as would be the case by calling CacheManager.getInstance().  We do not use the getInstance here
                //because we need to know if we need to destroy the CacheManager instance - using the static call,
                //we don't know which component is responsible for shutting it down.  By using a single EhCacheManager,
                //it will always know to shut down the instance if it was responsible for creating it.
                cacheMgr = new net.sf.ehcache.CacheManager( getCacheManagerConfigFileInputStream() );
                cacheManagerImplicitlyCreated = true;
                setCacheManager( cacheMgr );
            }
        } catch ( Exception e ) {
            throw new CacheException( e );
        }
    }

    public void destroy() {
        if ( cacheManagerImplicitlyCreated ) {
            try {
                net.sf.ehcache.CacheManager cacheMgr = getCacheManager();
                cacheMgr.shutdown();
            } catch ( Exception e ) {
                if ( log.isWarnEnabled() ) {
                    log.warn( "Unable to cleanly shutdown implicitly created CacheManager instance.  " +
                        "Ignoring (shutting down)..." );
                }
            }
            cacheManagerImplicitlyCreated = false;
        }
    }
}