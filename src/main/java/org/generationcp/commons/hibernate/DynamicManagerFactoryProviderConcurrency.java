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
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.DatasourceUtilities;
import org.generationcp.middleware.hibernate.HibernateSessionPerThreadProvider;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.pojos.workbench.Project;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

public class DynamicManagerFactoryProviderConcurrency extends ManagerFactoryBase implements ManagerFactoryProvider {

	private final static Logger LOG = LoggerFactory.getLogger(DynamicManagerFactoryProviderConcurrency.class);

	protected Map<Long, String> cropNameCache = new HashMap<Long, String>();

	public DynamicManagerFactoryProviderConcurrency() {

	}

	public DynamicManagerFactoryProviderConcurrency(final ProgramService programService) {
		this.programService = programService;
	}

	private ProgramService programService;

	@Autowired
	private ApplicationContext applicationContext;

	public synchronized ManagerFactory createInstance() throws MiddlewareQueryException {

		final Project project = this.getCropProject();

		final String databaseName = project.getDatabaseName();

		final SessionFactory applicableCropSessionFactory =
				(SessionFactory) this.applicationContext.getBean(DatasourceUtilities.computeSessionFactoryName(databaseName));

		final ManagerFactory factory = new ManagerFactory();
		factory.setSessionProvider(new HibernateSessionPerThreadProvider(applicableCropSessionFactory));
		factory.setCropName(project.getCropType().getCropName());

		ContextHolder.setCurrentCrop(project.getCropType().getCropName());
		ContextHolder.setCurrentProgram(project.getUniqueID());
		ContextHolder.setLoggedInUserId(this.getCurrentWorkbenchUserId());

		factory.setPedigreeProfile(this.pedigreeProfile);
		return factory;

	}

	private Project getCropProject() throws MiddlewareQueryException {
		return ContextUtil.getProjectInContext(this.programService,
				((ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
				.getRequest());
	}

	private Integer getCurrentWorkbenchUserId() {
		return ContextUtil.getCurrentWorkbenchUserId(((ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
			.getRequest());
	}

	@Override
	public ManagerFactory getManagerFactoryForProject(final Project project) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
