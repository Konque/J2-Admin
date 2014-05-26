<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

--%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="javax.portlet.RenderRequest"%>
<%@page import="javax.portlet.RenderResponse"%>
<%@page import="org.apache.jetspeed.decoration.Theme" %>
<%@page import="org.apache.jetspeed.portlets.spaces.SpaceBean"%>
<%@page import="org.apache.jetspeed.om.folder.Folder" %>
<%@page import="org.apache.jetspeed.om.page.Page"%>
<%@page import="org.apache.jetspeed.om.page.Link"%>
<%@page import="org.apache.jetspeed.portalsite.Menu"%>
<%@page import="org.apache.jetspeed.portalsite.MenuElement"%>
<%@page import="org.apache.jetspeed.portalsite.MenuOption"%>
<%@page import="org.apache.jetspeed.CommonPortletServices"%>
<%@page import="org.apache.jetspeed.request.RequestContext"%>
<%@page import="org.apache.jetspeed.portalsite.PortalSiteRequestContext"%>
<%@page import="org.apache.jetspeed.portlets.spaces.PageNavigator"%>

<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<%!
private void printMenuItem(MenuElement element,
                           int depth,
                           int maxDepth,
                           javax.servlet.jsp.JspWriter out, 
                           RenderRequest renderRequest, 
                           RenderResponse renderResponse,
                           RequestContext rc,
                           PortalSiteRequestContext psrc,
                           PageNavigator pageNavigator) throws java.io.IOException
{
    String type = element.getElementType();
    String nodeType = element.getManagedNode().getType();
    boolean editable = element.isEditable();
    String path = element.getManagedNode().getPath();
    String title = element.getTitle(renderRequest.getLocale());
    
    if (MenuElement.MENU_ELEMENT_TYPE.equals(type))
    {
        String url = ((Menu) element).getUrl();
        String styleClass = "closed"; 
        if (((Menu) element).isSelected(psrc))
        {
            styleClass = "active expanded";
        }
        out.write("<li type='" + type + "' nodeType='" + nodeType + "' editable='" + editable + "' path='" + path + "' class='" + styleClass + "'>\n");
        out.write("<a href='" + pageNavigator.getAbsoluteUrl(url, renderResponse, rc) + "'>" + title + "</a>\n");
        
        if (depth < maxDepth)
        {
	        List<MenuElement> childElements = (List<MenuElement>) ((Menu) element).getElements();
	        
	        if (childElements != null && !childElements.isEmpty())
	        {
	            out.write("<ul>\n");
	            
	            for (MenuElement child : childElements)
	            {
	                printMenuItem(child, depth + 1, maxDepth, out, renderRequest, renderResponse, rc, psrc, pageNavigator);
	            }
	            
	            out.write("</ul>\n");
	        }
        }
        
        out.write("</li>");
    }
    else if (MenuElement.OPTION_ELEMENT_TYPE.equals(type))
    {
        String url = ((MenuOption) element).getUrl();
        String styleClass = "leaf"; 
        if (((MenuOption) element).isSelected(psrc))
        {
            styleClass = "active leaf";
        }
        out.write("<li type='" + type + "' nodeType='" + nodeType + "' editable='" + editable + "' path='" + path +"' class='" + styleClass + "'>");
        out.write("<a href='" + pageNavigator.getAbsoluteUrl(url, renderResponse, rc) + "'>" + title + "</a>");
        out.write("</li>\n");
    }
}
%>

<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.spaces.resources.SpacesResources" />

<%
RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
PortalSiteRequestContext psrc = (PortalSiteRequestContext) rc.getAttribute("org.apache.jetspeed.portalsite.PortalSiteRequestContext");
String contextPath = rc.getRequest().getContextPath();
String basePath = rc.getPortalURL().getBasePath();
SpaceBean space = (SpaceBean) renderRequest.getAttribute("space");
Locale locale = renderRequest.getLocale();
PageNavigator pageNavigator = (PageNavigator) renderRequest.getAttribute("PageNavigator");
List<MenuElement> menuElements = (List<MenuElement>) renderRequest.getAttribute("spaceMenuElements");
List<MenuElement> linkElements = (List<MenuElement>) renderRequest.getAttribute("spaceLinkElements");
Theme theme = (Theme) rc.getRequest().getAttribute("org.apache.jetspeed.theme");
%>

<form id='jsPages' method="POST" action='<portlet:actionURL/>'>
  <div class="portlet-section-body">
    <div class="portlet-section-text">
      <h3 id="<portlet:namespace/>spaceMenuItem" editable="${pageEditable}" nodeType="folder" path="${space.path}" style="margin-right: 1em">
        <a href="<%=basePath%>${space.path}"><fmt:message key="spaces.pages.label.space.title"><fmt:param value="${space.title}"/></fmt:message></a>
      </h3>
      <ul id="<portlet:namespace/>pageNavMenu" class="navmenu">
<%
for (MenuElement element : menuElements)
{
    printMenuItem(element, 0, 6, out, renderRequest, renderResponse, rc, psrc, pageNavigator);
}
%>
      </ul>
    </div>
  </div>
<%
if (!linkElements.isEmpty())
{
%>
  <div class="portlet-section-body">
    <div class="portlet-section-text">
      <ul id="<portlet:namespace/>pageNavLinks" class="navmenu">
<%
for (MenuElement element : linkElements)
{
    printMenuItem(element, 0, 6, out, renderRequest, renderResponse, rc, psrc, pageNavigator);
}
%>
      </ul>
    </div>
  </div>
<%
}
%>
  
  <c:if test="${pageEditable}">
    <table style="border-collapse: collapse; width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
      <tr>
        <th class="portlet-section-header"></th>
      </tr>
      <tr>
        <td class="portlet-section-body">
          <select name="type" class="portlet-form-input-field">
            <c:forEach var="nodeType" items="${manageableNodeTypes}">
              <option value="${nodeType}"><fmt:message key="spaces.pages.label.nodeType.${nodeType}"/></option>
            </c:forEach>
          </select>
          <input type="text" name="name" class="portlet-form-input-field" size="8" value=""/><input type="submit" value="<fmt:message key='spaces.pages.label.add'/>" />
        </td>
      </tr>
      <tr>
        <td class="portlet-section-body">
          <fmt:message key="spaces.pages.label.template"/>:
          <select name="templatePage" class="portlet-form-field">
            <c:forEach items="${templatePages}" var="templatePage">
              <option value="${templatePage.path}">${templatePage.title}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <th class="portlet-section-header"></th>
      </tr>
    </table>
  </c:if>
</form>

<p>&nbsp;</p>

<span class="yui-skin-sam">
  <span id="<portlet:namespace/>nodePopupMenu" class="yui-menu yui-menu-horizontal yui-splitbuttonnav" 
        style="position: absolute; display: none">
    <span class="yui-menu-content" style="border: none">
      <ul>
        <li style="border: none">
          <span class="yui-menu-label" style="width: 2em; border-left: none; border-top: none; border-right: none; border-bottom: none">
            <a href="#"></a>
            <a href="#<portlet:namespace/>menuOptions" class="yui-menu-toggle" style="border: 1px solid #CCCCCC;">&nbsp;</a>
          </span>
          <div id="<portlet:namespace/>menuOptions" class="yui-menu">
            <div class="yui-menu-content">
              <ul id="<portlet:namespace/>nodePopupMenuItemList">
                <li class="yui-menuitem"><a id="<portlet:namespace/>nodePopupMenu_rename" class="yui-menuitem-content" href="#"><fmt:message key='spaces.pages.label.rename'/></a></li>
                <li class="yui-menuitem"><a id="<portlet:namespace/>nodePopupMenu_move" class="yui-menuitem-content" href="#"><fmt:message key='spaces.pages.label.move'/></a></li>
                <li class="yui-menuitem"><a id="<portlet:namespace/>nodePopupMenu_copy" class="yui-menuitem-content" href="#"><fmt:message key='spaces.pages.label.copy'/></a></li>
                <li class="yui-menuitem"><a id="<portlet:namespace/>nodePopupMenu_delete" class="yui-menuitem-content" href="#"><fmt:message key='spaces.pages.label.delete'/></a></li>
                <li class="yui-menuitem"><a id="<portlet:namespace/>nodePopupMenu_documentOrdering" class="yui-menuitem-content" href="#"><fmt:message key='spaces.pages.label.documentOrdering'/></a></li>
                <li class="yui-menuitem"><a id="<portlet:namespace/>nodePopupMenu_changeUrl" class="yui-menuitem-content" href="#"><fmt:message key='spaces.pages.label.changeUrl'/></a></li>
              </ul>
            </div>
          </div>
        </li>
      </ul>
    </span>
  </span>
</span>

<div style="display: none">
  <div id="<portlet:namespace/>folderModalHeader" align="center">
    <h2 id="<portlet:namespace/>folderModalHeader"><fmt:message key='spaces.pages.message.confirm.choosefolder'/></h2>
  </div>
  <div id="<portlet:namespace/>folderModalBody">
    <div style="WIDTH: 100%; HEIGHT: 200px; OVERFLOW: auto">
      <form id="<portlet:namespace/>targetFolderForm">
        <ul class="navmenu">
          <li path='${space.path}' class='expanded'>
            <input type='radio' name='targetFolder' value='${space.path}'/>
            <a href='#'>${space.title}</a>
            <ul id="<portlet:namespace/>targetSubFolders" class="navmenu">
            </ul>
          </li>
        </ul>
      </form>
    </div>
  </div> 
  <div id="<portlet:namespace/>folderModalFooter" align="center">
    <form>
      <input id="<portlet:namespace/>folderModalOK" type="button" value="<fmt:message key='spaces.pages.label.ok'/>" />
      <input id="<portlet:namespace/>folderModalCancel" type="button" value="<fmt:message key='spaces.pages.label.cancel'/>" />
    </form>
  </div>
</div>

<div style="display: none">
  <div id="<portlet:namespace/>documentOrderingModalHeader" align="center">
    <h2 id="<portlet:namespace/>documentOrderingModalHeader"><fmt:message key='spaces.pages.label.documentOrdering'/></h2>
  </div>
  <div id="<portlet:namespace/>documentOrderingModalBody">
    <div style="WIDTH: 100%; HEIGHT: 200px; OVERFLOW: auto">
      <ul class="navmenu" id="<portlet:namespace/>documentOrderingItems">
      </ul>
    </div>
  </div> 
  <div id="<portlet:namespace/>documentOrderingModalFooter" align="center">
    <form>
      <input id="<portlet:namespace/>documentOrderingModalOK" type="button" value="<fmt:message key='spaces.pages.label.ok'/>" />
      <input id="<portlet:namespace/>documentOrderingModalCancel" type="button" value="<fmt:message key='spaces.pages.label.cancel'/>" />
    </form>
  </div>
</div>

<script language="javascript">
YUI().use('jetui-portal', 'io', 'json', 'node', 'plugin', 'event-mouseenter', 'node-focusmanager', 'node-menunav', function(Y) {
    
    var onMenuItemClick = function(e) {
        var a = e.target;
        var menuitem = a.get("parentNode");
        var radio = menuitem.one("INPUT");
        if (radio) {
            radio.set("checked", "true");
        }
        var submenu = menuitem.one("UL");
        if (submenu) {
            if (submenu.getStyle("display") == "none") {
                menuitem.replaceClass("closed", "expanded");
                submenu.setStyle("display", "");
            } else {
                menuitem.replaceClass("expanded", "closed");
                submenu.setStyle("display", "none");
            }
	        
	        e.halt();
        }
    };
    
    var nodePopupMenu = null;
    var curNodeListItem = null;
    var nodePopupMenuItems = new Array();
    
    var onMenuItemMouseEnter = function(e) {
        if (nodePopupMenu == null) {
            nodePopupMenu = Y.Node.one("#<portlet:namespace/>nodePopupMenu");
            nodePopupMenu.plug(Y.Plugin.NodeFocusManager, { descendants: ".yui-menuitem"});
            nodePopupMenu.plug(Y.Plugin.NodeMenuNav, { autoSubmenuDisplay: false, mouseOutHideDelay: 3000, submenuHideDelay: 3000 });
            var menuItemList = Y.Node.one("#<portlet:namespace/>nodePopupMenuItemList");
            menuItemList.get("children").each(function(v, k) {
                var id = v.one("A").get("id");
                nodePopupMenuItems[id.substring(id.lastIndexOf('_') + 1)] = v;
            });
            resetNodePopupMenuEventHandlers();
        }
        var menuItem = e.target;
        if ("true" == menuItem.getAttribute("editable")) {
            var nodeType = menuItem.getAttribute("nodeType");
            var isSpaceMenuItem = (menuItem.get("tagName") != "LI");
            var menuItemList = Y.Node.one("#<portlet:namespace/>nodePopupMenuItemList");
            menuItemList.get("children").each(function(v, k) {
                v.remove();
            });
            if (!isSpaceMenuItem) {
                menuItemList.appendChild(nodePopupMenuItems["rename"]);
                menuItemList.appendChild(nodePopupMenuItems["move"]);
                menuItemList.appendChild(nodePopupMenuItems["copy"]);
                if (".psml" != nodeType || menuItem.get("parentNode").get("children").size() > 1) {
                    menuItemList.appendChild(nodePopupMenuItems["delete"]);
                }
            }
            if (nodeType == "folder") {
                menuItemList.appendChild(nodePopupMenuItems["documentOrdering"]);
            }
            if (nodeType == ".link") {
                menuItemList.appendChild(nodePopupMenuItems["changeUrl"]);
            }
            var menuItemElem = Y.Node.getDOMNode(menuItem);
            var xy = menuItem.getXY();
            nodePopupMenu.setStyle("display", "block");
	        nodePopupMenu.setStyle("left", "" + (xy[0] + menuItemElem.offsetWidth - 12) + "px");
	        nodePopupMenu.setStyle("top", "" + xy[1] + "px");
	        curNodeListItem = menuItem;
        }
        e.halt();
    };
    
    var onRenameComplete = function(id, o, args) {
        var id = id; // Transaction ID. 
        var data = o.responseText;
        var dataOut = null;
        
        try {
            dataOut = Y.JSON.parse(data);
            if (!dataOut) {
                Y.log("Error: no data found.");
                return;
            }
        } catch (e) {
            Y.log("Error: " + e.message);
            return;
        }
        
        var listItem = args.complete[0];
        var title = args.complete[1]; 
        var titleElem = listItem.one("A");
        if (titleElem && title) {
            titleElem.set("text", title);
        }
    };
    
    var onRenameClick = function(e) {
    	if (!curNodeListItem) {
            return;
        }
    	var path = curNodeListItem.getAttribute("path");
        var nodeType = curNodeListItem.getAttribute("nodeType");
        var currentTitle = curNodeListItem.one("A").get("text");
        var title = prompt("<fmt:message key='spaces.pages.message.prompt.title'/>", currentTitle);
        if (title && title != currentTitle) {
            var uri = "<%=contextPath%>/services/pagemanagement/info/" + nodeType + path + "?_type=json";
            var config = {
                    on: { complete: onRenameComplete },
                    method: "POST",
                    data: "title=" + title,
                    arguments: { complete: [ curNodeListItem, title ] }
                };
            var request = Y.io(uri, config);
        }
        e.halt();
    };
    
    var onDeleteComplete = function(id, o, args) {
        var listItem = args.complete[0];
        if (listItem) {
            var redirectLocation = null;
            var nodeType = listItem.getAttribute("nodeType");
            if (listItem.hasClass("active") && ".psml" == nodeType) {
                var href = listItem.one("A").get("href");
                redirectLocation = href.substring(0, href.lastIndexOf('/'));
            } 
            listItem.remove();
            if (nodePopupMenu) {
                nodePopupMenu.setStyle("display", "none");
            }
            if (redirectLocation) {
                location.href = redirectLocation;
            }
        }
    };
    
    var onDeleteClick = function(e) {
        if (!curNodeListItem) {
            return;
        }
        var cf = confirm("<fmt:message key='spaces.pages.message.confirm.delete'/>");
        if (cf) {
            var path = curNodeListItem.getAttribute("path");
            var nodeType = curNodeListItem.getAttribute("nodeType");
            var uri = "<%=contextPath%>/services/pagemanagement/" + nodeType + path + "?_type=json";
            var config = {
                    on: { complete: onDeleteComplete },
                    method: "DELETE",
                    arguments: { complete: [ curNodeListItem ] }
                };
            var request = Y.io(uri, config);
        }
        e.halt();
    };

    var onFolderModalCancelClick = function(e) {
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal) {
            portal.hideModalPanel();
        }
        e.halt();
    };
    
    var onMoveComplete = function(id, o, args) {
        var listItem = args.complete[0];
        if (listItem) {
            onCopyComplete(id, o, args);
            listItem.remove();
            if (nodePopupMenu) {
                nodePopupMenu.setStyle("display", "none");
            }
            resetMenuItemEventHandlers();
        }
    };
    
    var onCopyComplete = function(id, o, args) {
        var listItem = args.complete[0];
        var target = args.complete[1];
        var targetPath = args.complete[2];
        if (listItem && target && targetPath) {
            var pageNav = null;
            if (listItem.getAttribute("nodeType") == ".link" && target == "${space.path}") {
                pageNav = Y.Node.one("#<portlet:namespace/>pageNavLinks");
            } else {
                pageNav = Y.Node.one("#<portlet:namespace/>pageNavMenu");
            }
            if (target == "${space.path}") {
                var clonedListItem = listItem.cloneNode(true);
                clonedListItem.setAttribute("path", targetPath);
                pageNav.append(clonedListItem);
            } else {
                pageNav.all("LI").each(function(v, k) {
	                if (target == v.getAttribute("path")) {
	                    var clonedListItem = listItem.cloneNode(true);
	                    clonedListItem.setAttribute("path", targetPath);
	                    v.one("UL").append(clonedListItem);
	                }
	            });
            }
            resetMenuItemEventHandlers();
        }
    };
    
    var currentFolderChoosingMode = "move";
    
    var onFolderModalOKClick = function(e) {
        var form = Y.Node.one("#<portlet:namespace/>targetFolderForm");
        var target = null;
        form.all("INPUT").each(function(v, k) {
            if (v.get("checked")) {
                target = v.get("value");
            }
        });
        
        if (target) {
            var nodeType = curNodeListItem.getAttribute("nodeType");
            var sourcePath = curNodeListItem.getAttribute("path");
            var targetPath = target + sourcePath.substring(sourcePath.lastIndexOf('/'));
            if (sourcePath == targetPath) {
                alert("<fmt:message key='spaces.pages.message.choose.different.target'/>");
                e.halt();
                return;
            }
            if (Y.Node.one("#<portlet:namespace/>pageNavMenu").one("LI[path='" + targetPath + "']")) {
                alert("<fmt:message key='spaces.pages.message.choose.existing.target'/>");
                e.halt();
                return;
            }
            if (currentFolderChoosingMode == "move") {
                var cf = confirm("<fmt:message key='spaces.pages.message.confirm.move'/>");
                if (cf) {
                    var uri = "<%=contextPath%>/services/pagemanagement/move/" + nodeType + sourcePath + "?_type=json";
                    var config = {
                            on: { complete: onMoveComplete },
                            method: "POST",
                            data: "deep=true&merge=true&target=" + targetPath,
                            arguments: { complete: [ curNodeListItem, target, targetPath ] }
                        };
                    var request = Y.io(uri, config);
                }
            } else if (currentFolderChoosingMode == "copy") {
                var cf = confirm("<fmt:message key='spaces.pages.message.confirm.copy'/>");
                if (cf) {
                    var uri = "<%=contextPath%>/services/pagemanagement/copy/" + nodeType + sourcePath + "?_type=json";
                    var config = {
                            on: { complete: onCopyComplete },
                            method: "POST",
                            data: "deep=true&merge=true&target=" + targetPath,
                            arguments: { complete: [ curNodeListItem, target, targetPath ] }
                        };
                    var request = Y.io(uri, config);
                }
            }
        }
        
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal) {
            portal.hideModalPanel();
        }
        e.halt();
    };

    var onMoveClick = function(e) {
        if (!curNodeListItem) {
            return;
        }
        resetFolderModalPanelEventHandlers();
        var modalFeatures = {
            "width": 400,
            "height": 300,
            "addClasses": ["layout-<%=theme.getPageLayoutDecoration().getName()%>"]
        };
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal) {
            currentFolderChoosingMode = "move";
            portal.showModalPanel("#<portlet:namespace/>folderModalHeader", "#<portlet:namespace/>folderModalBody", "#<portlet:namespace/>folderModalFooter", modalFeatures);
        }
        e.halt();
    };
    
    var onCopyClick = function(e) {
        if (!curNodeListItem) {
            return;
        }
        resetFolderModalPanelEventHandlers();
        var modalFeatures = {
            "width": 400,
            "height": 300,
            "addClasses": ["layout-<%=theme.getPageLayoutDecoration().getName()%>"]
        };
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal) {
            currentFolderChoosingMode = "copy";
            portal.showModalPanel("#<portlet:namespace/>folderModalHeader", "#<portlet:namespace/>folderModalBody", "#<portlet:namespace/>folderModalFooter", modalFeatures);
        }
        e.halt();
    };
    
    var onDocumentOrderingModalCancelClick = function(e) {
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal) {
            portal.hideModalPanel();
        }
        e.halt();
    };
    
    var onDocumentOrderingComplete = function(id, o, args) {
        var listItem = args.complete[0];
        var docOrders = args.complete[1];
        var subItems = new Array();
        var subItemList = null;
        var subList = null;
        if (listItem.get("tagName") == "LI") {
            subList = listItem.one("UL");
        } else {
            subList = Y.Node.one("#<portlet:namespace/>pageNavMenu");
        }
        subItemList = subList.get("children");
        subItemList.each(function(v, k) {
            subItems[v.getAttribute("path")] = v;
        });
        subItemList.each(function(v, k) {
            v.remove();
        });
        for (var i = 0; i < docOrders.length; i++) {
            var path = docOrders[i];
            var item = subItems[path];
            if (item) {
                subList.appendChild(item);
                subItems[path] = null;
            }
        }
        for (var path in subItems) {
            if (subItems[path]) {
                subList.appendChild(subItems[path]);
            }
        }
    };
    
    var onDocumentOrderingModalOKClick = function(e) {
        var curDocOrders = [];
        var newDocOrders = [];
        var menuItemList = null;
        if (curNodeListItem.get("tagName") == "LI") {
            menuItemList = curNodeListItem.one("UL");
        } else {
            menuItemList = Y.Node.one("#<portlet:namespace/>pageNavMenu");
        }
        menuItemList.get("children").each(function(v, k) {
            curDocOrders.push(v.getAttribute("path"));
        });
        var orderingItems = Y.Node.one("#<portlet:namespace/>documentOrderingItems");
        orderingItems.one("LI").one("UL").all("LI").each(function(v, k) {
            newDocOrders.push(v.getAttribute("path"));
        });
        if (curDocOrders.toString() != newDocOrders.toString()) {
            var docOrder = [];
            for (var i = 0; i < newDocOrders.length; i++) {
                var doc = newDocOrders[i];
                if (doc.lastIndexOf('/') >= 0) {
                    doc = doc.substring(doc.lastIndexOf('/') + 1);
                }
                docOrder.push(doc);
            }
            var nodeType = curNodeListItem.getAttribute("nodeType");
            var path = curNodeListItem.getAttribute("path");
            var uri = "<%=contextPath%>/services/pagemanagement/info/" + nodeType + path + "?_type=json";
            var config = {
                    on: { complete: onDocumentOrderingComplete },
                    method: "POST",
                    data: "docorder=" + docOrder.toString(),
                    arguments: { complete: [ curNodeListItem, newDocOrders ] }
                };
            var request = Y.io(uri, config);
        }
        
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal) {
            portal.hideModalPanel();
        }
        e.halt();
    };
    
    var onDocumentOrderingClick = function(e) {
        if (!curNodeListItem) {
            return;
        }
        resetDocumentOrderingModalPanelEventHandlers(curNodeListItem);
        var modalFeatures = {
                "width": 400,
                "height": 300,
                "addClasses": ["layout-<%=theme.getPageLayoutDecoration().getName()%>"]
            };
         var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
         if (portal) {
             portal.showModalPanel("#<portlet:namespace/>documentOrderingModalHeader", "#<portlet:namespace/>documentOrderingModalBody", "#<portlet:namespace/>documentOrderingModalFooter", modalFeatures);
         }
         e.halt();
    };
    
    var onChangeUrlComplete = function(id, o, args) {
        var id = id; // Transaction ID. 
        var data = o.responseText;
        var dataOut = null;
        
        try {
            dataOut = Y.JSON.parse(data);
            if (!dataOut) {
                Y.log("Error: no data found.");
                return;
            }
        } catch (e) {
            Y.log("Error: " + e.message);
            return;
        }
        
        var listItem = args.complete[0];
        var url = args.complete[1]; 
        var urlElem = listItem.one("A");
        if (urlElem && url) {
            urlElem.set("href", url);
        }
    };
    
    var onChangeUrlClick = function(e) {
        if (!curNodeListItem) {
            return;
        }
        var path = curNodeListItem.getAttribute("path");
        var nodeType = curNodeListItem.getAttribute("nodeType");
        var currentUrl = curNodeListItem.one("A").get("href");
        var url = prompt("<fmt:message key='spaces.pages.message.prompt.url'/>", currentUrl);
        if (url && url != currentUrl) {
            var uri = "<%=contextPath%>/services/pagemanagement/info/" + nodeType + path + "?_type=json";
            var config = {
                    on: { complete: onChangeUrlComplete },
                    method: "POST",
                    data: "url=" + url,
                    arguments: { complete: [ curNodeListItem, url ] }
                };
            var request = Y.io(uri, config);
        }
        e.halt();
    };

    var menuItemEventHandles = [];
    var resetMenuItemEventHandlers = function() {
        for (var i = 0; i < menuItemEventHandles.length; i++) {
            menuItemEventHandles[i].detach();
        }
        menuItemEventHandles = [];
        var pageNavMenu = Y.Node.one("#<portlet:namespace/>pageNavMenu");
        pageNavMenu.all("A").each(function(v, k) {
            menuItemEventHandles.push(v.on("click", onMenuItemClick));
        });

        <c:if test="${pageEditable}">
        var spaceMenuItem = Y.Node.one("#<portlet:namespace/>spaceMenuItem");
        menuItemEventHandles.push(spaceMenuItem.on("mouseenter", onMenuItemMouseEnter));
        
        pageNavMenu.all("LI").each(function(v, k) {
            menuItemEventHandles.push(v.on("mouseenter", onMenuItemMouseEnter));
        });

        var pageNavLinks = Y.Node.one("#<portlet:namespace/>pageNavLinks");
        if (pageNavLinks) {
            pageNavLinks.all("LI").each(function(v, k) {
                menuItemEventHandles.push(v.on("mouseenter", onMenuItemMouseEnter));
            });
        }
        </c:if>
    };
    
    var resetNodePopupMenuEventHandlers = function() {
        Y.detach("click", onRenameClick, "#<portlet:namespace/>nodePopupMenu_rename");
        Y.Node.one("#<portlet:namespace/>nodePopupMenu_rename").on("click", onRenameClick);
        Y.detach("click", onDeleteClick, "#<portlet:namespace/>nodePopupMenu_delete");
        Y.Node.one("#<portlet:namespace/>nodePopupMenu_delete").on("click", onDeleteClick);
        Y.detach("click", onMoveClick, "#<portlet:namespace/>nodePopupMenu_move");
        Y.Node.one("#<portlet:namespace/>nodePopupMenu_move").on("click", onMoveClick);
        Y.detach("click", onCopyClick, "#<portlet:namespace/>nodePopupMenu_copy");
        Y.Node.one("#<portlet:namespace/>nodePopupMenu_copy").on("click", onCopyClick);
        Y.detach("click", onChangeUrlClick, "#<portlet:namespace/>nodePopupMenu_documentOrdering");
        Y.Node.one("#<portlet:namespace/>nodePopupMenu_documentOrdering").on("click", onDocumentOrderingClick);
        Y.detach("click", onChangeUrlClick, "#<portlet:namespace/>nodePopupMenu_changeUrl");
        Y.Node.one("#<portlet:namespace/>nodePopupMenu_changeUrl").on("click", onChangeUrlClick);
    };
    
    var resetFolderModalPanelEventHandlers = function() {
        var targetSubFolders = Y.Node.one("#<portlet:namespace/>targetSubFolders");
        targetSubFolders.get("children").each(function(v, k) {
            v.remove();
        });
        var pageNavMenu = Y.Node.one("#<portlet:namespace/>pageNavMenu");
        pageNavMenu.get("children").each(function(v, k) {
            if (v.getAttribute("nodeType") == "folder") {
                var cloned = v.cloneNode(true);
                cloned.all("LI").each(function(v2, k2) {
                    if (v2.getAttribute("nodeType") != "folder") {
                        v2.remove();
                    } else {
                        var path = v2.getAttribute("path");
                        var option = Y.Node.create("<input type='radio' name='targetFolder' value='" + path + "' />");
                        v2.prepend(option);
                    }
                });
                var path = cloned.getAttribute("path");
                var option = Y.Node.create("<input type='radio' name='targetFolder' value='" + path + "' />");
                cloned.prepend(option);
                targetSubFolders.append(cloned);
            }
        });
        
        var targetFolders = Y.Node.one("#<portlet:namespace/>folderModalBody").one("UL");
        targetFolders.all("A").each(function(v, k) {
            v.on("click", onMenuItemClick);
        });
        Y.detach("click", onFolderModalOKClick, "#<portlet:namespace/>folderModalOK");
        Y.Node.one("#<portlet:namespace/>folderModalOK").on("click", onFolderModalOKClick);
        Y.detach("click", onFolderModalCancelClick, "#<portlet:namespace/>folderModalCancel");
        Y.Node.one("#<portlet:namespace/>folderModalCancel").on("click", onFolderModalCancelClick);
    };

    var onDummyLinkClick = function(e) {
        e.halt();
    };
    
    var onDocumentMoveUp = function(e) {
        var item = e.target;
        while (item.get("tagName") != "LI") {
            item = item.get("parentNode");
        }
        var prevItem = item.previous();
        if (prevItem) {
            var parentNode = item.get("parentNode");
            item.remove();
            parentNode.insertBefore(item, prevItem);
        }
        e.halt();
    };
    
    var onDocumentMoveDown = function(e) {
        var item = e.target;
        while (item.get("tagName") != "LI") {
            item = item.get("parentNode");
        }
        var parentNode = item.get("parentNode");
        var nextItem = item.next();
        if (nextItem) {
            var parentNode = item.get("parentNode");
            item.remove();
            var nextOfNextItem = nextItem.next();
            if (nextOfNextItem) {
                parentNode.insertBefore(item, nextOfNextItem);
            } else {
                parentNode.appendChild(item);
            }
        }
        e.halt();
    };
    
    var resetDocumentOrderingModalPanelEventHandlers = function(baseNode) {
        var documentOrderingItems = Y.Node.one("#<portlet:namespace/>documentOrderingItems");
        documentOrderingItems.get("children").each(function(v, k) {
            v.remove();
        });
        var cloned = null;
        if (baseNode.get("tagName") == "LI") {
            cloned = baseNode.cloneNode(true);
        } else {
            cloned = Y.Node.create("<LI class='expanded' nodetype='folder'></LI>");
            cloned.appendChild(baseNode.one("A").cloneNode(true));
            cloned.appendChild(Y.Node.one("#<portlet:namespace/>pageNavMenu").cloneNode(true));
        }
        cloned.replaceClass("closed", "expanded");
        cloned.one("A").setAttribute("href", "#");
        cloned.on("click", onDummyLinkClick);
        var childItems = cloned.one("UL");
        childItems.setStyle("display", "");
        childItems.all("UL").each(function(v, k) {
            v.remove();
        });
        var uplink = Y.Node.create("<A href='#' class='moveup' style='position: absolute; left: 300px;'><img border='0' src='<%=contextPath%>/decorations/layout/images/movePortletUp.gif'/></A>");
        var downlink = Y.Node.create("<A href='#' class='movedown' style='position: absolute; left: 320px;'><img border='0' src='<%=contextPath%>/decorations/layout/images/movePortletDown.gif'/></A>");
        childItems.all("LI").each(function(v, k) {
            var link = v.one("A");
            link.setAttribute("href", "#");
            link.on("click", onDummyLinkClick);
            var itemUplink = uplink.cloneNode(true);
            var itemDownlink = downlink.cloneNode(true);
            itemUplink.on("click", onDocumentMoveUp);
            itemDownlink.on("click", onDocumentMoveDown); 
            v.append(itemUplink);
            v.append(itemDownlink);
        });
        documentOrderingItems.append(cloned);
        Y.detach("click", onDocumentOrderingModalOKClick, "#<portlet:namespace/>documentOrderingModalOK");
        Y.Node.one("#<portlet:namespace/>documentOrderingModalOK").on("click", onDocumentOrderingModalOKClick);
        Y.detach("click", onDocumentOrderingModalCancelClick, "#<portlet:namespace/>documentOrderingModalCancel");
        Y.Node.one("#<portlet:namespace/>documentOrderingModalCancel").on("click", onDocumentOrderingModalCancelClick);
    };
    
    resetMenuItemEventHandlers();
});
</script>
