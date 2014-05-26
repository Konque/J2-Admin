/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jetspeed.portlets.security;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.JetspeedActions;
import org.apache.jetspeed.om.page.ContentPage;
import org.apache.jetspeed.portlet.HeaderPhaseSupportConstants;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.UserManager;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Jetspeed Usermamagement Portlet
 * 
 * @author <a href="mailto:joachim@wemove.com">Joachim Mueller</a>
 */
public class JetspeedUserManagement2Portlet extends GenericServletPortlet {
	static Logger log = LoggerFactory.getLogger(JetspeedUserManagement2Portlet.class);

	protected UserManager userManager;
	protected String yuiScriptPath = "/javascript/yui/build/yui/yui-min.js";

	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		PortletContext context = getPortletContext();

		userManager = (UserManager) context.getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);

		if (userManager == null) {
			throw new PortletException("Failed to find the User Manager Component on portlet initialization");
		}

		String param = config.getInitParameter("yuiScriptPath");

		if (param != null) {
			yuiScriptPath = param;
		}
	}

	@Override
	protected void doHeaders(RenderRequest request, RenderResponse response) {
		super.doHeaders(request, response);
		RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
		addJavaScript(response, rc.getRequest().getContextPath() + yuiScriptPath,
				HeaderPhaseSupportConstants.HEAD_ELEMENT_CONTRIBUTION_ELEMENT_ID_YUI_LIBRARY_INCLUDE);

		
		addJavaScript(response, request.getContextPath() + "/javascript/yui2/yui2-jetspeed.js", null);
		addStyleLink(response, request.getContextPath() + "/css/yui2/yui2-jetspeed.css", null);
		addStyleLink(response, request.getContextPath() + "/css/security-usermanager2.css", null);

	}

	@Override
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		boolean hasEditAccess = false;

		try {
			RequestContext requestContext = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
			ContentPage contentPage = requestContext.getPage();
			contentPage.checkAccess(JetspeedActions.EDIT);
			hasEditAccess = true;
		} catch (Exception ignore) {
		}

		request.setAttribute("editAccess", hasEditAccess ? Boolean.TRUE : Boolean.FALSE);

		super.doView(request, response);
	}

	@Override
	public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
			IOException {
		// nothing todo at the moment, everything is done by a REST service
	}

	private void addJavaScript(RenderResponse response, String scriptPath, String scriptId) {
		Element headElem = response.createElement("script");
		headElem.setAttribute("language", "javascript");
		if (scriptId != null) {
			headElem.setAttribute("id", scriptId);
		}
		headElem.setAttribute("src", scriptPath);
		headElem.setAttribute("type", "text/javascript");
		response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headElem);
	}

	private void addStyleLink(RenderResponse response, String cssPath, String cssId) {
		Element headElem = response.createElement("link");
		headElem.setAttribute("rel", "stylesheet");
		if (cssId != null) {
			headElem.setAttribute("id", cssId);
		}
		headElem.setAttribute("href", cssPath);
		headElem.setAttribute("type", "text/css");
		response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headElem);
	}

}
