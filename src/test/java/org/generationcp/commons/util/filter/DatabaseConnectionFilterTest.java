
package org.generationcp.commons.util.filter;

import org.generationcp.commons.util.ResourceFinder;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.pojos.workbench.Project;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/20/2015 Time: 3:31 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class DatabaseConnectionFilterTest {

	public static final String DUMMY_PROJECT_DATABASE_NAME = "projectDbName";

	@Mock
	private FilterConfig config;

	@Mock
	private ServletContext context;

	@Mock
	private ServletRequest servletRequest;

	@Mock
	private ServletResponse response;

	@InjectMocks
	private final DatabaseConnectionFilter dut = Mockito.spy(new DatabaseConnectionFilter());

	@Before
	public void setUp() throws Exception {
		Mockito.when(this.config.getServletContext()).thenReturn(this.context);

	}

	@Test(expected = ServletException.class)
	public void testFilterInitializationIOExceptionOccured() throws IOException, ServletException {
		Mockito.doThrow(IOException.class).when(this.dut).getConfigFileInputStream();

		this.dut.init(this.config);
	}

	@Test
	public void testFilterInitialization() throws IOException, ServletException {
		Mockito.doReturn(ResourceFinder.locateFile("test.properties").openStream()).when(this.dut).getConfigFileInputStream();

		this.dut.init(this.config);

		Assert.assertNotNull(this.dut.getDbHost());
		Assert.assertNotNull(this.dut.getDbPassword());
		Assert.assertNotNull(this.dut.getDbUsername());
		Assert.assertNotNull(this.dut.getDbPort());
	}

	@Test
	public void testRetrieveCurrentProjectSessionFactoryNothingInMap() throws IOException {
		this.dut.setSessionFactoryMap(new HashMap<Long, SessionFactory>());
		SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
		Project project = Mockito.mock(Project.class);
		Mockito.when(project.getProjectId()).thenReturn((long) 1);
		Mockito.when(project.getDatabaseName()).thenReturn(DatabaseConnectionFilterTest.DUMMY_PROJECT_DATABASE_NAME);
		Mockito.doReturn(sessionFactory).when(this.dut)
		.openSessionFactory(Matchers.any(DatabaseConnectionParameters.class), Matchers.any(String[].class));

		SessionFactory retrievedFactory = this.dut.retrieveCurrentProjectSessionFactory(project, new String[] {});

		Assert.assertEquals(sessionFactory, retrievedFactory);

		ArgumentCaptor<DatabaseConnectionParameters> argumentCaptor = ArgumentCaptor.forClass(DatabaseConnectionParameters.class);
		Mockito.verify(this.dut).openSessionFactory(argumentCaptor.capture(), Matchers.any(String[].class));
		Assert.assertEquals(DatabaseConnectionFilterTest.DUMMY_PROJECT_DATABASE_NAME, argumentCaptor.getValue().getDbName());

		Assert.assertEquals(sessionFactory, this.dut.getSessionFactoryMap().get(project.getProjectId()));

	}

	@Test
	public void testRetrieveCurrentProjectSessionFactoryAlreadyInMap() throws IOException {
		Map<Long, SessionFactory> sessionFactoryMap = new HashMap<>();
		this.dut.setSessionFactoryMap(sessionFactoryMap);
		SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
		Project project = Mockito.mock(Project.class);
		Mockito.when(project.getProjectId()).thenReturn((long) 1);

		sessionFactoryMap.put(project.getProjectId(), sessionFactory);

		Assert.assertEquals(sessionFactory, this.dut.retrieveCurrentProjectSessionFactory(project, null));
		Mockito.verify(this.dut, Mockito.never()).openSessionFactory(Matchers.any(DatabaseConnectionParameters.class),
				Matchers.any(String[].class));
	}

	@Test
	public void testDoFilter() throws MiddlewareQueryException, ServletException, IOException {
		ProgramService programService = Mockito.mock(ProgramService.class);
		Project project = Mockito.mock(Project.class);
		Mockito.when(project.getProjectId()).thenReturn((long) 1);
		Mockito.doReturn(programService).when(this.dut).constructProgramService();
		Mockito.doReturn(project).when(this.dut).getCurrentProject(programService, this.servletRequest);
		SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);

		Map<Long, SessionFactory> sessionFactoryMap = new HashMap<>();
		this.dut.setSessionFactoryMap(sessionFactoryMap);
		sessionFactoryMap.put(project.getProjectId(), sessionFactory);

		this.dut.doFilter(this.servletRequest, this.response, Mockito.mock(FilterChain.class));
		Mockito.verify(this.servletRequest).setAttribute(Matchers.eq(DatabaseConnectionFilter.ATTR_MANAGER_FACTORY),
				Matchers.any(ManagerFactory.class));
	}

	@Test
	public void testDestroy() {
		SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
		Mockito.when(sessionFactory.isClosed()).thenReturn(false);
		Map<Long, SessionFactory> sessionFactoryMap = new HashMap<>();
		this.dut.setSessionFactoryMap(sessionFactoryMap);
		sessionFactoryMap.put((long) 1, sessionFactory);

		this.dut.destroy();
		Mockito.verify(sessionFactory).close();
	}

	@Test
	public void testDestroyAlreadyClosed() {
		SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
		Mockito.when(sessionFactory.isClosed()).thenReturn(true);
		Map<Long, SessionFactory> sessionFactoryMap = new HashMap<>();
		this.dut.setSessionFactoryMap(sessionFactoryMap);
		sessionFactoryMap.put((long) 1, sessionFactory);

		this.dut.destroy();
		Mockito.verify(sessionFactory, Mockito.never()).close();
	}

}
