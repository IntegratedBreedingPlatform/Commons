/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.commons.hibernate.util;

import org.generationcp.commons.hibernate.HttpRequestAware;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public abstract class HttpRequestAwareUtil {

	public static void onRequestStart(ApplicationContext applicationContext, HttpServletRequest request, HttpServletResponse response) {
		// notify all HttpRequestAware beans that an HTTP request has been started
		Map<String, HttpRequestAware> beans = applicationContext.getBeansOfType(HttpRequestAware.class, false, false);
		for (String beanName : beans.keySet()) {
			HttpRequestAware bean = applicationContext.getBean(beanName, HttpRequestAware.class);
			bean.onRequestStarted(request, response);
		}
	}

	public static void onRequestEnd(ApplicationContext applicationContext, HttpServletRequest request, HttpServletResponse response) {
		// notify all HttpRequestAware beans that an HTTP request has been ended
		Map<String, HttpRequestAware> beans = applicationContext.getBeansOfType(HttpRequestAware.class, false, false);
		for (String beanName : beans.keySet()) {
			HttpRequestAware bean = applicationContext.getBean(beanName, HttpRequestAware.class);
			bean.onRequestEnded(request, response);
		}
	}
}
