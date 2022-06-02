/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.commons.hibernate;

import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.pojos.workbench.Project;
import org.hibernate.SessionFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;

public class DynamicManagerFactoryProvider extends ManagerFactoryBase implements ManagerFactoryProvider, HttpRequestAware {

	public DynamicManagerFactoryProvider() {
	}

	public DynamicManagerFactoryProvider(ProgramService programService) {
		this.programService = programService;
	}

	private HibernateSessionPerRequestProvider sessionProvider;

	private final static ThreadLocal<HttpServletRequest> CURRENT_REQUEST = new ThreadLocal<HttpServletRequest>();

	private ProgramService programService;

	public synchronized ManagerFactory createInstance() throws MiddlewareQueryException {

		String databaseName = null;
		Project project = ContextUtil.getProjectInContext(this.programService, DynamicManagerFactoryProvider.CURRENT_REQUEST.get());
		SessionFactory sessionFactory = this.sessionFactoryCache.get(project.getProjectId());

		if (sessionFactory != null) {
			this.projectAccessList.remove(project.getProjectId());
		}

		if (sessionFactory == null || sessionFactory.isClosed()) {
			databaseName = project.getDatabaseName();

			// close any excess cached session factory
			this.closeExcessSessionFactory();

			DatabaseConnectionParameters params =
					new DatabaseConnectionParameters(this.dbHost, String.valueOf(this.dbPort), databaseName, this.dbUsername,
							this.dbPassword);
			try {
				sessionFactory = SessionFactoryUtil.openSessionFactory(params);
				this.sessionFactoryCache.put(project.getProjectId(), sessionFactory);
			} catch (FileNotFoundException e) {
				throw new ConfigException("Cannot create a SessionFactory for " + project, e);
			}
		} else {
			databaseName = project.getDatabaseName();
		}

		// add this session factory to the head of the access list
		this.projectAccessList.add(0, project.getProjectId());

		if (this.sessionProvider == null && sessionFactory != null) {
			this.sessionProvider = new HibernateSessionPerRequestProvider(sessionFactory);
		} else {
			this.sessionProvider.setSessionFactory(sessionFactory);
		}

		// create a ManagerFactory and set the HibernateSessionProviders
		// we don't need to set the SessionFactories here
		// since we want to a Session Per Request
		ManagerFactory factory = new ManagerFactory();
		factory.setSessionProvider(this.sessionProvider);

		return factory;
	}

	@Override
	public void onRequestStarted(HttpServletRequest request, HttpServletResponse response) {
		DynamicManagerFactoryProvider.CURRENT_REQUEST.set(request);
	}

	@Override
	public void onRequestEnded(HttpServletRequest request, HttpServletResponse response) {
		DynamicManagerFactoryProvider.CURRENT_REQUEST.remove();
	}

	@Override
	public ManagerFactory getManagerFactoryForProject(Project project) {
		return null;
	}

	@Override
	public void close() {
		if (this.sessionProvider != null) {
			this.sessionProvider.close();
		}
	}

}
