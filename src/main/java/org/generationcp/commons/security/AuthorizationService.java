package org.generationcp.commons.security;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorizationService {

	@Autowired
	private UserService userService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ContextUtil contextUtil;

	public AuthorizationService() {
	}

	public boolean hasAnyAuthority(final List<String> permissions) {
		final Project project = this.contextUtil.getProjectInContext();
		final WorkbenchUser user = this.userService.getUserWithAuthorities(this.contextUtil.getCurrentWorkbenchUser().getName(), project.getCropType().getCropName(),
			this.contextUtil.getCurrentProgramUUID());
		final List<String> userPermissions = user.getPermissions().stream().map(PermissionDto::getName).collect(Collectors.toList());
		return CollectionUtils.containsAny(userPermissions, permissions);
	}

	public Boolean isSuperAdminUser() {
		final WorkbenchUser workbenchUser = this.userService.getUserByUsername(SecurityUtil.getLoggedInUserName());
		if (workbenchUser != null) {
			return workbenchUser.isSuperAdmin();
		}
		return false;
	}


	// Workaround to reload authorities per program
	public void reloadAuthorities(final Project project) {
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		final String cropName = project.getCropType().getCropName();
		final Integer programId = project.getProjectId().intValue();

		final WorkbenchUser currentUser = contextUtil.getCurrentWorkbenchUser();

		new TransactionTemplate(this.transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				final List<GrantedAuthority> authorities = new ArrayList<>( //
						SecurityUtil.getAuthorities(permissionService.getPermissions( //
								currentUser.getUserid(), //
								cropName, //
								programId)));

				final Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
				SecurityContextHolder.getContext().setAuthentication(newAuth);
			}
		});
	}
}
