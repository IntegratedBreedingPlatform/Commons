
package org.generationcp.commons.util.filter;

import org.generationcp.commons.util.ContextUtil;
import org.generationcp.commons.util.ResourceFinder;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.api.program.ProgramServiceImpl;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.HibernateSessionPerThreadProvider;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.support.servlet.MiddlewareServletContextListener;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class DatabaseConnectionFilter implements Filter {

	private final static Logger LOG = LoggerFactory.getLogger(DatabaseConnectionFilter.class);

	public static final String ATTR_MANAGER_FACTORY = "managerFactory";
	public static final String PARAM_MIDDLEWARE_RESOURCE_FILES = "middleware_additional_resources";

	private FilterConfig filterConfig;
	private String dbHost;
	private String dbPort;
	private String dbUsername;
	private String dbPassword;
	private Map<Long, SessionFactory> sessionFactoryMap;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;

		Properties props = new Properties();
		try {
			props.load(this.getConfigFileInputStream());
			this.dbHost = props.getProperty("workbench.host");
			this.dbPort = props.getProperty("workbench.port");
			this.dbUsername = props.getProperty("workbench.username");
			this.dbPassword = props.getProperty("workbench.password");
		} catch (IOException e) {
			throw new ServletException(e);
		}

		this.sessionFactoryMap = new HashMap<>();
	}

	protected InputStream getConfigFileInputStream() throws IOException {
		String databasePropertyFile =
				this.filterConfig.getServletContext().getInitParameter(MiddlewareServletContextListener.PARAM_DATABASE_PROPERTY_FILE);
		return ResourceFinder.locateFile(databasePropertyFile).openStream();
	}

	protected ProgramService constructProgramService() {
		ServletContext context = this.filterConfig.getServletContext();
		SessionFactory workbenchSessionFactory =
				(SessionFactory) context.getAttribute(MiddlewareServletContextListener.ATTR_WORKBENCH_SESSION_FACTORY);

		HibernateSessionProvider sessionProviderForWorkbench = new HibernateSessionPerRequestProvider(workbenchSessionFactory);

		return new ProgramServiceImpl(sessionProviderForWorkbench);
	}

	protected SessionFactory retrieveCurrentProjectSessionFactory(Project project, String[] additionalResourceFiles) throws IOException {

		SessionFactory sessionFactory = this.sessionFactoryMap.get(project.getProjectId());
		if (sessionFactory == null) {
			String databaseName = project.getDatabaseName();
			DatabaseConnectionParameters params =
					new DatabaseConnectionParameters(this.dbHost, this.dbPort, databaseName, this.dbUsername, this.dbPassword);

			sessionFactory = this.openSessionFactory(params, additionalResourceFiles);
			this.sessionFactoryMap.put(project.getProjectId(), sessionFactory);
		}

		return sessionFactory;
	}

	// wrapper method around static call to ContextUtil to make it simpler to test
	protected Project getCurrentProject(ProgramService programService, ServletRequest servletRequest)
			throws MiddlewareQueryException {
		return ContextUtil.getProjectInContext(programService, (HttpServletRequest) servletRequest);
	}

	// wrapper method around static call to ContextUtil to make it simpler to test
	protected SessionFactory openSessionFactory(DatabaseConnectionParameters params, String[] additionalResourceFiles)
			throws FileNotFoundException {
		return SessionFactoryUtil.openSessionFactory(null, params, additionalResourceFiles);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
			ServletException {

		ProgramService programService = this.constructProgramService();

		ManagerFactory factory = null;

		try {
			Project project = this.getCurrentProject(programService, servletRequest);

			String paramResourceFile =
					this.filterConfig.getServletContext().getInitParameter(DatabaseConnectionFilter.PARAM_MIDDLEWARE_RESOURCE_FILES);

			String[] additionalResourceFiles = null;
			if (paramResourceFile != null && !paramResourceFile.isEmpty()) {
				additionalResourceFiles = paramResourceFile.split(",");
			}

			SessionFactory sessionFactory = this.retrieveCurrentProjectSessionFactory(project, additionalResourceFiles);

			assert sessionFactory != null;

			HibernateSessionPerThreadProvider sessionProvider = new HibernateSessionPerThreadProvider(sessionFactory);

			// create a ManagerFactory and set the HibernateSessionProviders
			// we don't need to set the SessionFactories here
			// since we want to a Session Per Request
			factory = new ManagerFactory();
			factory.setSessionProvider(sessionProvider);

			servletRequest.setAttribute(DatabaseConnectionFilter.ATTR_MANAGER_FACTORY, factory);

			filterChain.doFilter(servletRequest, servletResponse);

		} catch (MiddlewareQueryException e) {
			DatabaseConnectionFilter.LOG.error(e.getMessage(), e);
		} finally {
			if (factory != null) {
				factory.close();
			}
		}

	}

	@Override
	public void destroy() {
		for (SessionFactory sessionFactory : this.sessionFactoryMap.values()) {
			if (!sessionFactory.isClosed()) {
				sessionFactory.close();
			}
		}
	}

	public FilterConfig getFilterConfig() {
		return this.filterConfig;
	}

	public String getDbHost() {
		return this.dbHost;
	}

	public String getDbPort() {
		return this.dbPort;
	}

	public String getDbUsername() {
		return this.dbUsername;
	}

	public String getDbPassword() {
		return this.dbPassword;
	}

	public Map<Long, SessionFactory> getSessionFactoryMap() {
		return this.sessionFactoryMap;
	}

	public void setSessionFactoryMap(Map<Long, SessionFactory> sessionFactoryMap) {
		this.sessionFactoryMap = sessionFactoryMap;
	}
}
