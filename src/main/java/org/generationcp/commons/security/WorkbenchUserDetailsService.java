
package org.generationcp.commons.security;

import com.google.common.base.Optional;
import org.apache.commons.lang.StringEscapeUtils;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

public class WorkbenchUserDetailsService implements UserDetailsService {

	private UserService userService;

	@Autowired
	private ProgramService programService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private HttpServletRequest request;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			// username must be converted from html-encode to utf-8 string to support chinese/utf-8 languages
			username = StringEscapeUtils.unescapeHtml(username);

			final List<WorkbenchUser> matchingUsers = this.userService.getUserByName(username, 0, 1, Operation.EQUAL);
			if (matchingUsers != null && !matchingUsers.isEmpty()) {
				final WorkbenchUser workbenchUser = matchingUsers.get(0);

				final Optional<Project> project = ContextUtil.getProject(this.programService, this.request);

				String cropName = null;
				Integer programId = null;
				if (project.isPresent()) {
					cropName = project.get().getCropType().getCropName();
					programId = project.get().getProjectId().intValue();
				}

				final Collection<? extends GrantedAuthority> authorities =
					SecurityUtil.getAuthorities(this.permissionService.getPermissions( //
						workbenchUser.getUserid(), //
						cropName, //
						programId));
				// FIXME Populate flags for accountNonExpired, credentialsNonExpired, accountNonLocked properly, all true for now.
				return new User(workbenchUser.getName(), workbenchUser.getPassword(), authorities);
			}
			throw new UsernameNotFoundException("Invalid username/password.");
		} catch (final MiddlewareQueryException e) {
			throw new AuthenticationServiceException("Data access error while authenticaing user against Workbench.", e);
		}
	}

	public void setUserService(final UserService userService) {
		this.userService = userService;
	}
}
