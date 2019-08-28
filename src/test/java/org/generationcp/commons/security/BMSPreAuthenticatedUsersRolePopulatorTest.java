
package org.generationcp.commons.security;

import org.apache.commons.codec.binary.Base64;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.transaction.PlatformTransactionManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class BMSPreAuthenticatedUsersRolePopulatorTest {

	private static final String TEST_USER = "testUser";
	// FIXME IBP-2843
	private static final String AUTH_TOKEN = Base64.encodeBase64URLSafeString(BMSPreAuthenticatedUsersRolePopulatorTest.TEST_USER
			.getBytes());
	private static final String PERMISSION_NAME = "ADMIN";

	private static final int USER_ID = 1;
	private static final long SELECTED_PROJECT_ID = 1L;
	private static final String CROP_NAME = "maize";
	private static final Integer PROGRAM_ID = 1;

	@Mock
	private UserService userService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private PermissionService permissionService;

	@Mock
	@SuppressWarnings("unused")
	private PlatformTransactionManager transactionManager;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private BMSPreAuthenticatedUsersRolePopulator rolesPopulator;

	@Before
	public void setup() {
		Mockito.when(this.request.getParameter(ContextConstants.PARAM_LOGGED_IN_USER_ID)).thenReturn(String.valueOf(USER_ID));
		Mockito.when(this.request.getParameter(ContextConstants.PARAM_SELECTED_PROJECT_ID)).thenReturn(String.valueOf(SELECTED_PROJECT_ID));
		Mockito.when(this.request.getParameter(ContextConstants.PARAM_AUTH_TOKEN)).thenReturn(AUTH_TOKEN);
	}

	@Test
	public void testBuildDetails() {
		try {
			final List<WorkbenchUser> matchingUsers = new ArrayList<>();
			final WorkbenchUser testUserWorkbench = new WorkbenchUser();
			testUserWorkbench.setName(BMSPreAuthenticatedUsersRolePopulatorTest.TEST_USER);
			testUserWorkbench.setUserid(USER_ID);
			testUserWorkbench.setPassword("password");
			matchingUsers.add(testUserWorkbench);
			Mockito.when(this.userService.getUserByName(BMSPreAuthenticatedUsersRolePopulatorTest.TEST_USER, 0, 1, Operation.EQUAL))
					.thenReturn(matchingUsers);

			final Project project = new Project();
			project.setProjectId(SELECTED_PROJECT_ID);
			final CropType cropType = new CropType(CROP_NAME);
			project.setCropType(cropType);
			Mockito.when(this.workbenchDataManager.getProjectById(SELECTED_PROJECT_ID)).thenReturn(project);

			final List<PermissionDto> permissions = new ArrayList<>();
			final PermissionDto permission = new PermissionDto();
			permission.setName(PERMISSION_NAME);
			permissions.add(permission);
			Mockito.when(this.permissionService.getPermissions(USER_ID, CROP_NAME, PROGRAM_ID)).thenReturn(permissions);

			final PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails roleDetails =
					(PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails) this.rolesPopulator.buildDetails(this.request);

			Assert.assertEquals(permissions.size(), roleDetails.getGrantedAuthorities().size());
			Assert.assertEquals(SecurityUtil.ROLE_PREFIX + permission.getName(), roleDetails.getGrantedAuthorities().get(0)
					.getAuthority());

		} catch (MiddlewareQueryException e) {
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test(expected = AuthenticationServiceException.class)
	public void testLoadUserDataAccessError() throws MiddlewareQueryException {
		Mockito.when(this.userService.getUserByName(BMSPreAuthenticatedUsersRolePopulatorTest.TEST_USER, 0, 1, Operation.EQUAL))
				.thenThrow(new MiddlewareQueryException("Boom!"));
		this.rolesPopulator.buildDetails(this.request);
	}

}
