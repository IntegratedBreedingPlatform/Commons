
package org.generationcp.commons.context;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.util.ContextUtil;
import org.springframework.web.util.WebUtils;

public class ContextFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NOOP
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		response.setHeader("x-frame-options", "SAMEORIGIN");

		if (!ContextUtil.isStaticResourceRequest(request.getRequestURI())) {
			ContextInfo requestContextInfo = ContextUtil.getContextInfoFromRequest(request);

			if (requestContextInfo.getSelectedProjectId() != null && requestContextInfo.getLoggedInUserId() != null) {
				WebUtils.setSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO, requestContextInfo);
				String contextPath = request.getContextPath();

				Cookie loggedInUserCookie = new Cookie(ContextConstants.PARAM_LOGGED_IN_USER_ID, requestContextInfo.getLoggedInUserId().toString());
				Cookie selectedProjectIdCookie = new Cookie(ContextConstants.PARAM_SELECTED_PROJECT_ID, requestContextInfo.getSelectedProjectId()
						.toString());
				Cookie authTokenCookie = new Cookie(ContextConstants.PARAM_AUTH_TOKEN, requestContextInfo.getAuthToken());

				loggedInUserCookie.setPath(contextPath);
				selectedProjectIdCookie.setPath(contextPath);
				authTokenCookie.setPath(contextPath);

				response.addCookie(loggedInUserCookie);
				response.addCookie(selectedProjectIdCookie);
				response.addCookie(authTokenCookie);
			}

			else {
				ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

				if (contextInfo == null) {
					// this happens when session attribute gets lost due to session.invalidate() calls when navigating within application.
					// restore session attribure from cookies
					Cookie userIdCookie = WebUtils.getCookie(request, ContextConstants.PARAM_LOGGED_IN_USER_ID);
					Cookie selectedProjectIdCookie = WebUtils.getCookie(request, ContextConstants.PARAM_SELECTED_PROJECT_ID);
					Cookie authTokenCookie = WebUtils.getCookie(request, ContextConstants.PARAM_AUTH_TOKEN);
					if (userIdCookie != null && selectedProjectIdCookie != null) {
						ContextUtil.setContextInfo(request, Integer.valueOf(userIdCookie.getValue()),
								Long.valueOf(selectedProjectIdCookie.getValue()), authTokenCookie.getValue());
					}
				}
			}
		}

		chain.doFilter(request, response);
	}



	@Override
	public void destroy() {
		// NOOP
	}

}
