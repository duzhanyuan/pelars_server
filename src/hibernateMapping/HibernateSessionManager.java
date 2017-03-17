package hibernateMapping;

import java.util.HashSet;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * 
 * @author Giacomo Dabisias, Emanuele Ruffaldi
 *
 */
public class HibernateSessionManager {

	public static HashSet<String> mapped_classes;

	private static final SessionFactory sessionFactory;

	static{
		try{
			Configuration configuration = new Configuration();
			configuration.configure("/hibernateMapping/hibernate.cfg.xml");
			StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();

			serviceRegistryBuilder.applySettings(configuration.getProperties());

			ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);

		}catch (Throwable ex){
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}

	public static Session getSession(){
		Session s = sessionFactory.openSession();
		//database made consistent after each commit
		s.setFlushMode(FlushMode.COMMIT);
		return s;
	}
}