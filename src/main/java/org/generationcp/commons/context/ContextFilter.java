
package org.generationcp.commons.context;

import org.generationcp.commons.util.ContextUtil;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContextFilter implements Filter {

	static final String HTTPS = "https";

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// NOOP
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException,
			ServletException {

		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse response = (HttpServletResponse) servletResponse;
		response.setHeader("x-frame-options", "SAMEORIGIN");
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
		response.setHeader("Feature-Policy", "self");

		if (!ContextUtil.isStaticResourceRequest(request.getRequestURI())) {
			final ContextInfo requestContextInfo = ContextUtil.getContextInfoFromRequest(request);

			if (requestContextInfo.getSelectedProjectId() != null && requestContextInfo.getLoggedInUserId() != null) {
				WebUtils.setSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO, requestContextInfo);
				final String contextPath = request.getContextPath();
				final String httpScheme = WorkbenchAppPathResolver.getScheme(request);
				final boolean isHttps = HTTPS.equalsIgnoreCase(httpScheme);
				final Cookie loggedInUserCookie = new Cookie(ContextConstants.PARAM_LOGGED_IN_USER_ID, requestContextInfo.getLoggedInUserId().toString());
				final Cookie selectedProjectIdCookie = new Cookie(ContextConstants.PARAM_SELECTED_PROJECT_ID, requestContextInfo.getSelectedProjectId()
						.toString());
				final Cookie authTokenCookie = new Cookie(ContextConstants.PARAM_AUTH_TOKEN, requestContextInfo.getAuthToken());


				this.configureAndAddCookie(loggedInUserCookie, contextPath, isHttps, response);
				this.configureAndAddCookie(selectedProjectIdCookie, contextPath, isHttps, response);
				this.configureAndAddCookie(authTokenCookie, contextPath, isHttps, response);
			}

			else {
				final ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

				if (contextInfo == null) {
					// this happens when session attribute gets lost due to session.invalidate() calls when navigating within application.
					// restore session attribure from cookies
					final Cookie userIdCookie = WebUtils.getCookie(request, ContextConstants.PARAM_LOGGED_IN_USER_ID);
					final Cookie selectedProjectIdCookie = WebUtils.getCookie(request, ContextConstants.PARAM_SELECTED_PROJECT_ID);
					final Cookie authTokenCookie = WebUtils.getCookie(request, ContextConstants.PARAM_AUTH_TOKEN);
					if (userIdCookie != null && selectedProjectIdCookie != null) {
						ContextUtil.setContextInfo(request, Integer.valueOf(userIdCookie.getValue()),
								Long.valueOf(selectedProjectIdCookie.getValue()), authTokenCookie.getValue());
					}
				}
			}
		}

		chain.doFilter(request, response);
	}

	private void configureAndAddCookie(final Cookie cookie, final String contextPath, final boolean isHttps, final HttpServletResponse response) {
		cookie.setPath(contextPath);
		cookie.setSecure(isHttps);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	@Override
	public void destroy() {
		// NOOP
	}

}
