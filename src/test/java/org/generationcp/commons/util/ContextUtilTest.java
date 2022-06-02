
package org.generationcp.commons.util;

import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ContextUtilTest {

	private static Project testProject;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpSession session;
	@Mock
	private UserService userService;
	@Mock
	private ProgramService programService;

	@BeforeClass
	public static void setupOnce() {
		ContextUtilTest.testProject = new Project();
		ContextUtilTest.testProject.setProjectId(1L);
		ContextUtilTest.testProject.setProjectName("Rice Breeding Programme");
	}

	@Before
	public void setUpEach() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.request.getParameter(ContextConstants.PARAM_LOGGED_IN_USER_ID)).thenReturn("1");
		Mockito.when(this.request.getParameter(ContextConstants.PARAM_SELECTED_PROJECT_ID)).thenReturn("1");
	}

	@Test
	public void testGetProjectInContextResolvesFromSessionContext() throws MiddlewareQueryException {
		Mockito.when(this.session.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(new ContextInfo(1, 2L));
		Mockito.when(this.request.getSession(Matchers.anyBoolean())).thenReturn(this.session);
		Mockito.when(this.programService.getProjectById(2L)).thenReturn(ContextUtilTest.testProject);

		Assert.assertNotNull(ContextUtil.getProjectInContext(this.programService, this.request));
		Mockito.verify(this.programService).getProjectById(Matchers.anyLong());
		Mockito.verify(this.programService, Mockito.never()).getLastOpenedProjectAnyUser();
	}

	@Test
	public void testGetProjectInContextFallsBackToOldMethod() throws MiddlewareQueryException {
		Mockito.when(this.session.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(null);
		Mockito.when(this.request.getSession(Matchers.anyBoolean())).thenReturn(this.session);
		Mockito.when(this.programService.getLastOpenedProjectAnyUser()).thenReturn(ContextUtilTest.testProject);

		Assert.assertNotNull(ContextUtil.getProjectInContext(this.programService, this.request));
		Mockito.verify(this.programService).getLastOpenedProjectAnyUser();
		Mockito.verify(this.programService, Mockito.never()).getProjectById(Matchers.anyLong());
	}

	@Test(expected = MiddlewareQueryException.class)
	public void testExceptionIsThrownWhenProjectCannotBeResolved() throws MiddlewareQueryException {

		Mockito.when(this.session.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(null);
		Mockito.when(this.request.getSession(Matchers.anyBoolean())).thenReturn(this.session);
		Mockito.when(this.programService.getLastOpenedProjectAnyUser()).thenReturn(null);

		ContextUtil.getProjectInContext(this.programService, this.request);
	}

	@Test
	public void testCurrentWorkbenchUserIdResolvesFromSessionContext() throws MiddlewareQueryException {
		ContextInfo contextInfo = new ContextInfo(1, 1L);
		Mockito.when(this.session.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(contextInfo);
		Mockito.when(this.request.getSession(Matchers.anyBoolean())).thenReturn(this.session);

		Assert.assertEquals(contextInfo.getLoggedInUserId(), ContextUtil.getCurrentWorkbenchUserId(this.request));
	}


	@Test(expected = MiddlewareQueryException.class)
	public void testExceptionIsThrownWhenWorkbenchUserCannotBeResolved() throws MiddlewareQueryException {
		Mockito.when(this.session.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(null);
		Mockito.when(this.request.getSession(Matchers.anyBoolean())).thenReturn(this.session);

		ContextUtil.getCurrentWorkbenchUserId(this.request);
	}

	@Test
	public void testIsStaticResourceRequest() {

		Assert.assertFalse(ContextUtil.isStaticResourceRequest("/App/NonStaticResource"));

		Assert.assertTrue(ContextUtil.isStaticResourceRequest("/App/static/app.whatever"));

		Assert.assertTrue(ContextUtil.isStaticResourceRequest("/App/js/app.js"));
		Assert.assertTrue(ContextUtil.isStaticResourceRequest("/App/css/app.css"));
		Assert.assertTrue(ContextUtil.isStaticResourceRequest("/App/img/app.png"));
		Assert.assertTrue(ContextUtil.isStaticResourceRequest("/App/img/app.gif"));
		Assert.assertTrue(ContextUtil.isStaticResourceRequest("/App/img/app.jpg"));
		Assert.assertTrue(ContextUtil.isStaticResourceRequest("/App/font/app.woff"));
	}

	@Test
	public void testGetContextInfoFromRequest() throws Exception {
		Mockito.when(this.session.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(
				new ContextInfo(1, 1L));
		Mockito.when(this.request.getSession(Matchers.anyBoolean())).thenReturn(this.session);
		Mockito.when(this.programService.getProjectById(1L)).thenReturn(ContextUtilTest.testProject);

		Assert.assertNotNull(ContextUtil.getContextInfoFromRequest(this.request));
		Assert.assertEquals(Integer.valueOf(1), ContextUtil.getContextInfoFromRequest(this.request).getLoggedInUserId());
		Assert.assertEquals(Long.valueOf(1L), ContextUtil.getContextInfoFromRequest(this.request).getSelectedProjectId());
	}

	@Test
	public void testGetCurrentWorkbenchUsername() throws Exception {
		Mockito.when(this.session.getAttribute(ContextConstants.SESSION_ATTR_CONTEXT_INFO)).thenReturn(
				new ContextInfo(1, 1L));
		Mockito.when(this.request.getSession(Matchers.anyBoolean())).thenReturn(this.session);
		Mockito.when(this.programService.getProjectById(1L)).thenReturn(ContextUtilTest.testProject);
	}
}
