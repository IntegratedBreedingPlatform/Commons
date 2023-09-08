
package org.generationcp.commons.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class SecurityUtil {

	/**
	 * While role prefix is configurable and can be set to empty, having a prefix is recommended. See JavaDoc for
	 * org.springframework.security.access.vote.RoleVoter
	 */
	public static final String ROLE_PREFIX = "ROLE_";

	private SecurityUtil() {
		// Utility class
	}

	public static String getLoggedInUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			return authentication.getName();
		}
		return null;
	}

	public static Collection<? extends GrantedAuthority> getLoggedInUserAuthorities() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			return authentication.getAuthorities();
		}
		return Collections.emptyList();
	}

	public static Collection<? extends GrantedAuthority> getAuthorities(final List<PermissionDto> permissionDtoList) {
		final List<GrantedAuthority> authorities = new ArrayList<>();
		if (permissionDtoList != null) {
			for (final PermissionDto permissionDto : permissionDtoList) {
				authorities.add(new SimpleGrantedAuthority(SecurityUtil.ROLE_PREFIX + permissionDto.getName()));
			}
		}
		return authorities;
	}

	public static boolean hasAnyAuthority(final List<String> permissions) {
		final Collection<? extends GrantedAuthority> authorities = SecurityUtil.getLoggedInUserAuthorities();
		return authorities.stream().anyMatch(o -> permissions.contains(o.getAuthority()));
	}

	public static String getEncodedToken() {
		return Base64.encodeBase64URLSafeString(SecurityUtil.getLoggedInUserName().getBytes());
	}
}
