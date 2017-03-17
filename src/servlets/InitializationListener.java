package servlets;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

public class InitializationListener implements ServletContextListener {    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        CacheManager singletonManager = CacheManager.create();
        // name, elements, overflowtodisk, eternal, timeToLiveSeconds, timeToIdleSeconds
        Cache memoryOnlyCache = new Cache("dbCache", 100, false, true, 86400,86400);
        singletonManager.addCache(memoryOnlyCache);
        Cache cache = singletonManager.getCache("dbCache");       
        ctx.setAttribute("dbCache", cache );           
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}