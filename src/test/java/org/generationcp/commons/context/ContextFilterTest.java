package org.generationcp.commons.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@RunWith(MockitoJUnitRunner.class)
public class ContextFilterTest {

	@Mock
	HttpServletRequest httpServletRequest;

	@Mock
	HttpServletResponse httpServletResponse;

	@Mock
	HttpSession httpSession;

	@Mock
	FilterChain filterChain;

	ContextFilter contextFilter;

	@Before
	public void setup(){
		this.contextFilter = new ContextFilter();
	}

	@Test
	public void testDoFilterWithContextInfoInRequestParams() throws Exception{
		final String contextPath = "/contextPath";
		Mockito.when(this.httpServletRequest.getRequestURI()).thenReturn(contextPath);

		Mockito.when(this.httpServletRequest.getParameter(ContextConstants.PARAM_SELECTED_PROJECT_ID)).thenReturn("1");
		Mockito.when(this.httpServletRequest.getParameter(ContextConstants.PARAM_LOGGED_IN_USER_ID)).thenReturn("1");
		Mockito.when(this.httpServletRequest.getSession()).thenReturn(this.httpSession);
		Mockito.when(this.httpServletRequest.getContextPath()).thenReturn(contextPath);
		Mockito.when(this.httpServletRequest.getScheme()).thenReturn("https");

		this.contextFilter.doFilter(this.httpServletRequest, this.httpServletResponse, this.filterChain);

		final ContextInfo expectedContextInfo = new ContextInfo(1, 1L);

		final Cookie expectedUserIdCookie = this.getCookie(ContextConstants.PARAM_LOGGED_IN_USER_ID, "1", contextPath, true);
		final Cookie expectedProjectIdCookie = this.getCookie(ContextConstants.PARAM_SELECTED_PROJECT_ID, "1", contextPath, true);

		Mockito.verify(this.httpSession).setAttribute(Matchers.eq(ContextConstants.SESSION_ATTR_CONTEXT_INFO), Matchers.refEq(expectedContextInfo));
		Mockito.verify(this.httpServletResponse).addCookie(Matchers.refEq(expectedUserIdCookie));
		Mockito.verify(this.httpServletResponse).addCookie(Matchers.refEq(expectedProjectIdCookie));
		Mockito.verify(this.filterChain).doFilter(Matchers.refEq(this.httpServletRequest),Matchers.refEq(this.httpServletResponse));

	}

	@Test
	public void testDoFilterWithContextInfoNotInRequestParamsAndNotInSessionAttributeButInCookie() throws Exception{
		final String contextPath = "/contextPath";
		Mockito.when(this.httpServletRequest.getRequestURI()).thenReturn(contextPath);
		Mockito.when(this.httpServletRequest.getSession()).thenReturn(this.httpSession);

		final Cookie[] requestCookies = new Cookie[3];
		requestCookies[0] = this.getCookie(ContextConstants.PARAM_LOGGED_IN_USER_ID, "11", contextPath, false);
		requestCookies[1] = this.getCookie(ContextConstants.PARAM_SELECTED_PROJECT_ID, "12", contextPath, false);
		Mockito.when(this.httpServletRequest.getCookies()).thenReturn(requestCookies);

		this.contextFilter.doFilter(this.httpServletRequest, this.httpServletResponse, this.filterChain);

		final ContextInfo expectedContextInfo = new ContextInfo(11, 12L);
		Mockito.verify(this.httpSession).setAttribute(Matchers.eq(ContextConstants.SESSION_ATTR_CONTEXT_INFO), Matchers.refEq(expectedContextInfo));
		Mockito.verify(this.filterChain).doFilter(Matchers.refEq(this.httpServletRequest),Matchers.refEq(this.httpServletResponse));

	}

	@Test
	public void testDoFilterWithContextInfoNotInRequestParamsAndNotInSessionAttributeAndNotInCookie() throws Exception{
		final String contextPath = "/contextPath";
		Mockito.when(this.httpServletRequest.getRequestURI()).thenReturn(contextPath);

		final Cookie[] requestCookies = null;
		Mockito.when(this.httpServletRequest.getCookies()).thenReturn(requestCookies);

		this.contextFilter.doFilter(this.httpServletRequest, this.httpServletResponse, this.filterChain);

		Mockito.verify(this.httpSession, Mockito.never()).setAttribute(Matchers.eq(ContextConstants.SESSION_ATTR_CONTEXT_INFO), Matchers.anyObject());
		Mockito.verify(this.filterChain).doFilter(Matchers.refEq(this.httpServletRequest),Matchers.refEq(this.httpServletResponse));

	}

	@Test
	public void testDoFilterWithContextInfoNotInRequestParamButInSessionAttribute() throws Exception{
		final String contextPath = "/contextPath";
		Mockito.when(this.httpServletRequest.getRequestURI()).thenReturn(contextPath);

		final ContextInfo sessionContextInfo = new ContextInfo(11, 12L);

		this.contextFilter.doFilter(this.httpServletRequest, this.httpServletResponse, this.filterChain);
		Mockito.verify(this.httpSession, Mockito.never()).setAttribute(Matchers.eq(ContextConstants.SESSION_ATTR_CONTEXT_INFO), Matchers.anyObject());
		Mockito.verify(this.filterChain).doFilter(Matchers.refEq(this.httpServletRequest),Matchers.refEq(this.httpServletResponse));

	}

	@Test
	public void testDoFilterWithStaticResource() throws Exception{
		final String contextPath = "/static";
		Mockito.when(this.httpServletRequest.getRequestURI()).thenReturn(contextPath);

		this.contextFilter.doFilter(this.httpServletRequest, this.httpServletResponse, this.filterChain);
		Mockito.verify(this.filterChain).doFilter(Matchers.refEq(this.httpServletRequest),Matchers.refEq(this.httpServletResponse));
	}


	private Cookie getCookie(final String cookieName, final String cookieValue, final String cookiePath, final boolean isSecure){
		final Cookie cookie = new Cookie(cookieName , cookieValue);
		cookie.setPath(cookiePath);
		cookie.setHttpOnly(true);
		cookie.setSecure(isSecure);

		return cookie;
	}

}
