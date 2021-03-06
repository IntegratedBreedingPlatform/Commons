
package org.generationcp.commons.security;

import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class BMSPreAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

	private static final Logger LOG = LoggerFactory.getLogger(BMSPreAuthenticationFilter.class);

	@Resource
	UserService userService;

	@Autowired
	PlatformTransactionManager transactionManager;

	@Override
	protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);

		return transactionTemplate.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(final TransactionStatus status) {
				try {
					return ContextUtil.getCurrentWorkbenchUsername(BMSPreAuthenticationFilter.this.userService, request);
				} catch (final MiddlewareQueryException e) {
					BMSPreAuthenticationFilter.LOG.error(e.getMessage(), e);
				}
				// TODO Auto-generated method stub
				return null;
			}
		});
	}

	@Override
	protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
		return "";
	}

}
