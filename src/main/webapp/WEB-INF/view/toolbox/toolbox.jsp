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
<%@ page import="java.util.List"%>
<%@ page import="java.text.DecimalFormat"%>
<%@ page import="java.text.NumberFormat"%>
<%@ page import="org.apache.jetspeed.spaces.Space"%>
<%@ page import="org.apache.jetspeed.om.page.Page"%>
<%@ page import="org.apache.jetspeed.request.RequestContext"%>

<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.toolbox.resources.JetspeedToolboxResources" />

<c_rt:set var="requestContext" value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>"/>
<c:set var="portalContextPath" value="${requestContext.request.contextPath}"/>
<c:set var="portalContextPathInUrlTag" value="${portalContextPath}"/>
<c:if test="${empty portalContextPathInUrlTag}">
  <c:set var="portalContextPathInUrlTag" value="/"/>
</c:if>
<c:set var="portalPagePath" value="${requestContext.portalURL.path}"/>
<c:if test="${empty portalPagePath}">
  <c:set var="portalPagePath" value="/"/>
</c:if>

<c:set var="portletPageSize" value="${prefs['Rows'][0]}"/>

<c:set var="pageDeco" value="" />
<c:forEach var="theme" items="${themes}">
  <c:if test="${theme.selected}">
    <c:set var="pageDeco" value="${theme.name}" />
  </c:if>
</c:forEach>

<ul id="<portlet:namespace/>tabActions" class="default-tabs">
  <li class="not-selected"><a href="#" id="<portlet:namespace/>portletsTabAction" title="<fmt:message key='toolbox.label.portlets'/>"><fmt:message key="toolbox.label.portlets"/></a></li>
  <li class="not-selected"><a href="#" id="<portlet:namespace/>layoutTabAction" title="<fmt:message key='toolbox.label.layout'/>"><fmt:message key="toolbox.label.layout"/></a></li>
  <li class="not-selected"><a href="#" id="<portlet:namespace/>themeTabAction" title="<fmt:message key='toolbox.label.theme'/>"><fmt:message key="toolbox.label.theme"/></a></li>
</ul>

<form id="<portlet:namespace/>form">
<table id="<portlet:namespace/>portletsTab" style="display: none; border-collapse: collapse;  width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
	    <tr>
	        <th class="portlet-section-header" colspan="2"></th>
	    </tr>        
    <tr>
        <td width="99%" class="portlet-section-subheader" nowrap="true">
            <input type='text' name='query' class='portlet-form-input-field' size='25' value='' title='<fmt:message key="toolbox.message.search"/>' style='WIDTH: 100%' />
        </td>
        <td width="1%" class="portlet-section-subheader" nowrap="true">
            <input type='image' src='<c:url value="/images/search.png"/>' width="25" height="20" 
                   alt='<fmt:message key="toolbox.label.search"/>' title='<fmt:message key="toolbox.message.search"/>' />
        </td>
    </tr>
    
    <tr>
        <td colspan="2" class="portlet-section-subheader">
            <select id="<portlet:namespace/>categories" class="portlet-form-field" style="WIDTH: 100%">
                <option value=""><fmt:message key="toolbox.message.chooseOne"/></option>
                <c:forEach var="category" items="${categories}">
                    <option value="${category}" keywords="${categoryKeywords[category]}">${category}</option>
                </c:forEach>
            </select>
        </td>
    </tr>
    
    <tr>
        <td colspan="2">
            <div id="<portlet:namespace/>portletItemsPanel"></div>
        </td>
    </tr>

    <tr>
        <td colspan="2" class="portlet-section-subheader">
            <div id="<portlet:namespace/>pageNavBar" class="jstbPageNavBar">
	            <a href="#" class="jstbPageNav" pageIndex="first"><fmt:message key="toolbox.label.moveFirst"/></a>
	            <a href="#" class="jstbPageNav" pageIndex="prev"><fmt:message key="toolbox.label.movePrev"/></a>
	            <select id="<portlet:namespace/>pageNavIndex" class="portlet-form-field">
	              <option value=""><fmt:message key="toolbox.label.page"/></option>
	            </select>
	            <a href="#" class="jstbPageNav" pageIndex="next"><fmt:message key="toolbox.label.moveNext"/></a>
	            <a href="#" class="jstbPageNav" pageIndex="last"><fmt:message key="toolbox.label.moveLast"/></a>
	        </div>
        </td>
    </tr>

</table>
</form>

<div id="<portlet:namespace/>portletItemTemplate" style="display: none; border-collapse: collapse; width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
    <table style="border-collapse: collapse; width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
	    <tr>
	        <td rowspan="2" width="20%" class="portlet-section-body" style="text-align: center; vertical-align: middle;">
	            <img/>
	        </td>
	        <td width="80%" class="portlet-section-body">
	            <div class='tooltext'>#</div>
	        </td>
	    </tr>       
	    <tr>
	       <td class="portlet-section-body">
	           <a href="#" name="preview" class='tooltext'><fmt:message key="toolbox.label.preview"/></a>  
	           <a href="#" name="add" class='tooltext'><fmt:message key="toolbox.label.add"/></a>
	           <a href="#" name="clone" class='tooltext'><fmt:message key="toolbox.label.clone"/></a>
	       </td>
	    </tr>
    </table>
</div>

<div id="<portlet:namespace/>layoutTab" style="display: none; border-collapse: collapse; width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
    <table style="border-collapse: collapse; width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
	    <tr>
	        <th class="portlet-section-header" colspan="2"></th>
	    </tr>    
	    <c:forEach var="layout" items="${layouts}"> 
        <c:choose>
            <c:when test="${layout.selected}">
                <c:set var="borderStyle" value="solid"/>
            </c:when>
            <c:otherwise>
                <c:set var="borderStyle" value="none"/>
            </c:otherwise>
        </c:choose>
	    <tr>        
	        <c:choose>
	            <c:when test="${editAccess}">
	                <td><a href="<portlet:actionURL><portlet:param name='layout' value='${layout.layoutPortlet}'/></portlet:actionURL>"><img style="border-style: ${borderStyle}" src="<c:url context='${portalContextPathInUrlTag}' value='/layouts/${layout.image}'/>"/></a></td>
	            </c:when>
	            <c:otherwise>
	                <td><img style="border-style: ${borderStyle}" src="<c:url context='${portalContextPathInUrlTag}' value='/layouts/${layout.image}'/>"/></td>
	            </c:otherwise>
	        </c:choose>
	        <td style="vertical-align: middle">${layout.title}</td>
	    </tr>
	    </c:forEach>
    </table>
</div>

<div id="<portlet:namespace/>themeTab" style="display: none; border-collapse: collapse; width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
    <table style="border-collapse: collapse; width: 100%; margin-top: 0px; margin-bottom: 0px; float: left;">
	    <tr>
	        <th class="portlet-section-header" colspan="2"></th>
	    </tr>    
	    <c:forEach var="theme" items="${themes}">
	    <c:choose>
	        <c:when test="${theme.selected}">
	            <c:set var="borderStyle" value="solid"/>
	        </c:when>
	        <c:otherwise>
                <c:set var="borderStyle" value="none"/>
	        </c:otherwise>
	    </c:choose>
	    <tr>
	        <c:choose>
	            <c:when test="${editAccess}">
	                <td><a href="<portlet:actionURL><portlet:param name='theme' value='${theme.name}'/></portlet:actionURL>"><img style="border-style: ${borderStyle}" src="<c:url context='${portalContextPathInUrlTag}' value='/decorations/layout/${theme.name}/${theme.image}'/>" /></a></td>
	            </c:when>
	            <c:otherwise>
	                <td><img style="border-style: ${borderStyle}" src="<c:url context='${portalContextPathInUrlTag}' value='/decorations/layout/${theme.name}/${theme.image}'/>"/></td>
	            </c:otherwise>
	        </c:choose>
	        <td style="vertical-align: middle">${theme.title}</td>
	    </tr>
	    </c:forEach>
	</table>
</div>

<div id="<portlet:namespace/>previewOverlay" style="BACKGROUND-COLOR: #eee; BORDER-LEFT: #fff solid 2px; BORDER-TOP: #fff solid 2px; BORDER-RIGHT: #aaa solid 2px; BORDER-BOTTOM: #aaa solid 2px; DISPLAY: none; PADDING: 5px">
    <div class="yui-widget-hd"><h2><fmt:message key="toolbox.label.preview"/></h2></div>
	<div class="yui-widget-bd"></div>
    <div class="yui-widget-ft" align="center"><a id="<portlet:namespace/>previewOverlayClose" href="#"><fmt:message key="toolbox.label.close"/></a></div>
</div>

<div style="display: none">
  <div id="<portlet:namespace/>portletCloneModalHeader">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <td><h2 id="<portlet:namespace/>portletCloneModalHeader"><fmt:message key='toolbox.label.clonePortlet'/></h2></td>
        <td align="right"><a id="<portlet:namespace/>portletCloneModalHeaderClose" href="#"><img border="0" src='<c:url context="${portalContextPathInUrlTag}" value="/decorations/images/close.gif"/>'/></a></td>
      </tr>
    </table>
  </div>
  <div id="<portlet:namespace/>portletCloneModalBody">
    <div>
      <iframe id="<portlet:namespace/>portletCloneModalBodyIFrame" align="BOTTOM" width="100%" scrolling="AUTO" height="480" frameborder="0" marginwidth="0" marginheight="0">
      </iframe>
    </div>
  </div> 
  <div id="<portlet:namespace/>portletCloneModalFooter" align="center">
  </div>
</div>

<div class="modal-window-close-script" style="DISPLAY: none">var portal = (parent.JETUI_YUI ? parent.JETUI_YUI.getPortalInstance() : null); if (portal) { portal.hideModalPanel(); }</div>

<script language="javascript">
YUI().use('jetui-portal', 'io', 'json', 'node', 'cookie', 'overlay', 'anim', 'plugin', function(Y) {
    
    var previewOverlay = null;
    
    function AnimPlugin(config) {
        AnimPlugin.superclass.constructor.apply(this, arguments);
    }
    
    AnimPlugin.NS = "fx";
    AnimPlugin.NAME = "animPlugin";
    AnimPlugin.ATTRS = {
        duration : { value: 0.1 },
        animVisible : {
            valueFn : function() {
		        var host = this.get("host");
                var boundingBox = host.get("boundingBox");
                var anim = new Y.Anim({
                    node: boundingBox,
                    to: { opacity: 1 },
                    duration: this.get("duration")
                });
                if (!host.get("visible")) {
                    boundingBox.setStyle("opacity", 0);
                }
                anim.on("destroy", function() {
                    if (Y.UA.ie) {
                        this.get("node").setStyle("opacity", 1);
                    } else {
                        this.get("node").setStyle("opacity", "");
                    }
                });
                return anim;
            }
        },
        animHidden : {
            valueFn : function() {
                return new Y.Anim({
                    node: this.get("host").get("boundingBox"),
                    to: { opacity: 0 },
                    duration: this.get("duration")
                });
            }
        }
    }
    
    Y.extend(AnimPlugin, Y.Plugin.Base, {
        initializer : function(config) {
            this._bindAnimVisible();
            this._bindAnimHidden();
            this.after("animVisibleChange", this._bindAnimVisible);
            this.after("animHiddenChange", this._bindAnimHidden);
            this.doBefore("_uiSetVisible", this._uiAnimSetVisible);
        },
        destructor : function() {
            this.get("animVisible").destroy();
            this.get("animHidden").destroy();
        },
        _uiAnimSetVisible : function(val) {
            if (this.get("host").get("rendered")) {
                if (val) {
                    this.get("animHidden").stop();
                    this.get("animVisible").run();
                } else {
                    this.get("animVisible").stop();
                    this.get("animHidden").run();
                }
                return new Y.Do.Prevent("AnimPlugin prevented default show/hide");
            }
        },
        _uiSetVisible : function(val) {
            var host = this.get("host");
            var hiddenClass = host.getClassName("hidden");
            if (!val) {
                host.get("boundingBox").addClass(hiddenClass);
            } else {
                host.get("boundingBox").removeClass(hiddenClass);
            }
        },
        _bindAnimVisible : function() {
            var animVisible = this.get("animVisible");
            animVisible.on("start", Y.bind(function() {
                this._uiSetVisible(true);
            }, this));
        },
        _bindAnimHidden : function() {
            var animHidden = this.get("animHidden");
            animHidden.after("end", Y.bind(function() {
                this._uiSetVisible(false);
            }, this));
        }
    });
    
    var pagination = {
            portlet : { uri : null, totalSize : 0, pageSize : ${portletPageSize}, beginIndex : 0 },

            getPageBeginIndex : function(info, scrollTo) {
                if (isNaN(scrollTo)) {
	                if ("first" == scrollTo) {
	                    return 0;
	                } else if ("prev" == scrollTo) {
	                    return Math.max(info.beginIndex - info.pageSize, 0);
	                } else if ("next" == scrollTo) {
                        var lastPageBeginIndex = 0;
                        if (info.totalSize > 0) {
                            lastPageBeginIndex = parseInt((info.totalSize - 1) / info.pageSize) * info.pageSize;
                        }
	                    return Math.min(info.beginIndex + info.pageSize, lastPageBeginIndex);
	                } else if ("last" == scrollTo) {
	                    var lastPageBeginIndex = 0;
	                    if (info.totalSize > 0) {
	                        lastPageBeginIndex = parseInt((info.totalSize - 1) / info.pageSize) * info.pageSize;
	                    }
	                    return lastPageBeginIndex;
	                } else {
	                    return -1;
	                }
                } else {
                    var lastPageBeginIndex = 0;
                    if (info.totalSize > 0) {
                        lastPageBeginIndex = parseInt((info.totalSize - 1) / info.pageSize) * info.pageSize;
                    }
                    return Math.min((parseInt(scrollTo) - 1) * info.pageSize, lastPageBeginIndex);
                }
            },

            getPageCount : function(info) {
                var count = parseInt(info.totalSize / info.pageSize);
                if (info.totalSize % info.pageSize != 0) {
                    ++count;
                }
                return count;
            },
            
            getCurrentPageIndex : function(info) {
                if (info.totalSize > 0) {
                    return parseInt(info.beginIndex / info.pageSize) + 1;
                } else {
                    return 0;
                }
            }
    };

    function switchTab(e) {
        var tabId = null;
        if ('string' == typeof(e)) {
            tabId = e;
        } else {
            tabId = e.target.get("id").replace(/TabAction$/, "").substring("<portlet:namespace/>".length);
        }
        
        Y.Node.one("#<portlet:namespace/>tabActions").all("A").each(function(v, k) {
            if (v.get("id") == "<portlet:namespace/>" + tabId + "TabAction") {
                v.get("parentNode").replaceClass("not-selected", "selected");
            } else {
                v.get("parentNode").replaceClass("selected", "not-selected");
            }
        });
        
        Y.Node.one("#<portlet:namespace/>portletsTab").setStyle("display", "none");
        Y.Node.one("#<portlet:namespace/>layoutTab").setStyle("display", "none");
        Y.Node.one("#<portlet:namespace/>themeTab").setStyle("display", "none");
        Y.Node.one("#<portlet:namespace/>" + tabId + "Tab").setStyle("display", "");
        
        Y.Cookie.setSub("JS2TOOLBOX", "TAB", tabId, { path: "/" });

        if (tabId == "portlets") {
	        var category = Y.Cookie.getSub("JS2TOOLBOX", "CAT");
	        if (!category) {
		        category = "${defaultCategory}";
	        }
	        if (category) {
	            var categories = Y.Node.getDOMNode(Y.Node.one("#<portlet:namespace/>categories"));
	            for (var i = 0; i < categories.options.length; i++) {
	                if (category == categories.options[i].value && i != categories.selectedIndex) {
	                    categories.options[i].selected = true;
	                    loadPortletsInCategory(category);
	                    break;
	                }
	            }
	        }
        }

        if ('string' != typeof(e)) {
            e.halt();
        }
    }
    
    var onLoadPortletComplete = function(id, o, args) { 
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
        
        pagination.portlet.totalSize = dataOut.totalSize;
        pagination.portlet.beginIndex = Math.max(dataOut.beginIndex, 0);
        
        var portletsPanel = Y.Node.one("#<portlet:namespace/>portletItemsPanel");
        var clonedPortletsPanel = portletsPanel.cloneNode(false);
        portletsPanel.get("parentNode").replaceChild(clonedPortletsPanel, portletsPanel);
        portletsPanel = clonedPortletsPanel;
        
        var templatePanel = Y.Node.one("#<portlet:namespace/>portletItemTemplate");
        var defs = dataOut.definitions;
        
        for (var i = 0; i < defs.length; i++) {
            var def = defs[i];
            var clone = templatePanel.cloneNode(true);
            clone.setStyle('display', '');
            var imgNode = clone.one('img');
            if (def.portletIcon) {
                var iconContextPath = ("portlet" == def.portletIconHolder && def.applicationContextPath ? def.applicationContextPath : "${portalContextPath}"); 
                imgNode.set("src", iconContextPath + (def.portletIconBasePath ? def.portletIconBasePath : "/images/portlets") + "/" + def.portletIcon);
            } else {
                imgNode.set("src", "${portalContextPath}/images/portlets/applications-other.png");
            }
            
            var nameNode = clone.one('div');
            var portletDisplayName = def.displayNames[0].value;
            nameNode.setContent(portletDisplayName);
            
            var refreshOnAdd = false;
            var metadataFields = def.metadata.fields;
            if (metadataFields) {
                for (var j = 0; j < metadataFields.length; j++) {
                    var field = metadataFields[j];
                    if (field.name == "jetui.refresh.page.on.add.portlet") {
                        refreshOnAdd = ("TRUE" == ("" + field.value).toUpperCase());
                        break;
                    }
                }
            }
            
            <c:choose>
	            <c:when test="${editAccess}">
	                var addLink = clone.one("[name='add']");
	                var addLinkNode = Y.Node.getDOMNode(addLink);
	                addLinkNode.setAttribute("portletUniqueName", def.uniqueName);
	                if (refreshOnAdd) {
	                    addLinkNode.setAttribute("refreshOnAdd", "" + refreshOnAdd);
	                }
	                addLink.on("click", addPortlet);
	            </c:when>
	            <c:otherwise>
	                clone.all("a").item(1).remove();
	            </c:otherwise>
	        </c:choose>

            <c:choose>
                <c:when test="${userInAdminRole}">
                    var cloneLink = clone.one("[name='clone']");
                    var cloneLinkNode = Y.Node.getDOMNode(cloneLink);
                    cloneLinkNode.setAttribute("portletUniqueName", def.uniqueName);
                    cloneLink.on("click", clonePortlet);
                </c:when>
                <c:otherwise>
                    clone.all("a").item(1).remove();
                </c:otherwise>
            </c:choose>
            
            var previewLink = clone.one("[name='preview']");
            var previewLinkNode = Y.Node.getDOMNode(previewLink);
            previewLinkNode.setAttribute("portletUniqueName", def.uniqueName);
            previewLinkNode.setAttribute("portletDisplayName", portletDisplayName);
            previewLink.on("click", previewPortlet);
            
            clone.set("id", "<portlet:namespace/>portletItem-" + def.uniqueName);
            
            portletsPanel.appendChild(clone);
        }

        var navSel = Y.one("#<portlet:namespace/>pageNavIndex");
        var options = navSel.getElementsByTagName("OPTION");
        for (var i = options.size() - 1; i > 0; i--) {
            navSel.removeChild(options.item(i));
        }
        var pageCount = pagination.getPageCount(pagination.portlet);
        for (var i = 1; i <= pageCount; i++) {
            var option = Y.Node.create("<option value='" + i + "'>" + (i < 10 ? " " + i : i) + "</option>");
            navSel.appendChild(option);
        }
        var curPageIndex = pagination.getCurrentPageIndex(pagination.portlet);
        var pageIndices = Y.Node.getDOMNode(navSel);
        pageIndices.options[curPageIndex].selected = true;
    };
    
    var loadPortletsInCategory = function(e) {
        var category = ('string' == typeof(e) ? e : null);
        var keywords = null;
        if (!category) {
	        var categories = Y.Node.getDOMNode(e.target);
	        category = categories.options[categories.selectedIndex].value;
        }
        if (!category) {
            return;
        }
        var option = Y.Node.one("#<portlet:namespace/>categories").one("[value='" + category + "']");
        if (option) {
            keywords = option.getAttribute("keywords").replace(/\s*,\s*/g, " | ");
        }
        if (!keywords) {
            keywords = category;
        }
        Y.Cookie.setSub("JS2TOOLBOX", "CAT", category, { path: "/" });
        var uri = "${portalContextPath}/services/portletregistry/definition/?_type=json";
        uri += "&max=" + pagination.portlet.pageSize + "&begin=0";
        uri += "&keywords=" + encodeURIComponent(keywords);
        pagination.portlet.uri = uri;
        var request = Y.io(uri, { on: { complete: onLoadPortletComplete } });
        Y.Node.getDOMNode(Y.Node.one("#<portlet:namespace/>form")).query.value = "";
    }
    
    var loadPortletsByQuery = function(e) {
        var form = Y.Node.getDOMNode(e.target);
        var query = form.query.value;
        var uri = "${portalContextPath}/services/portletregistry/definition/?_type=json";
        uri += "&max=" + pagination.portlet.pageSize + "&begin=0";
        if (query) {
            uri += "&query=" + query;
        }
        pagination.portlet.uri = uri;
        var request = Y.io(uri, { on: { complete: onLoadPortletComplete } });
        Y.Node.getDOMNode(Y.Node.one("#<portlet:namespace/>categories")).selectedIndex = 0;
        e.halt();
    };

    var onPortletPaginationLinkClick = function(e) {
        var navElem = e.target;
        var beginIndex = pagination.getPageBeginIndex(pagination.portlet, navElem.getAttribute("pageIndex"));
        
        if (beginIndex != -1 && beginIndex != pagination.portlet.beginIndex) {
            var uri = pagination.portlet.uri.replace(/&begin=\d+/, "&begin=" + beginIndex);
            var request = Y.io(uri, { on: { complete: onLoadPortletComplete } });
        }
        
        e.halt();
    };

    var onPortletPaginationSelectChange = function(e) {
        var pageIndices = Y.Node.getDOMNode(e.target);
        var pageIndex = pageIndices.options[pageIndices.selectedIndex].value;
        if (pageIndex) {
	        var beginIndex = pagination.getPageBeginIndex(pagination.portlet, pageIndex);
	        
	        if (beginIndex != -1 && beginIndex != pagination.portlet.beginIndex) {
	            var uri = pagination.portlet.uri.replace(/&begin=\d+/, "&begin=" + beginIndex);
	            var request = Y.io(uri, { on: { complete: onLoadPortletComplete } });
	        }
        } else {
            var curPageIndex = pagination.getCurrentPageIndex(pagination.portlet);
            pageIndices.options[curPageIndex].selected = true;
        }
        e.halt();
    };

    var onAddPortletComplete = function(id, o, args) {
        var id = id;
        var data = o.responseText;
        var dataOut = null;
        var refreshOnAdd = args.complete[0];

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
        
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal && !refreshOnAdd) {
	        portal.addPortlet(dataOut);
        } else {
            location.href = location.href;
        }
    };
    
    var addPortlet = function(e) {
        var a = Y.Node.getDOMNode(e.target);
        var portletUniqueName = a.getAttribute("portletUniqueName");
        var refreshOnAdd = ("true" == a.getAttribute("refreshOnAdd"));

        if (portletUniqueName) {
            var uri = "${portalContextPath}/services/pagelayout/fragment/portlet/" + portletUniqueName + "/?_type=json";
            var config = {
                    on: { complete: onAddPortletComplete },
                    arguments: { complete: [ refreshOnAdd ] },
                    method: "POST",
                    headers: { "X-Portal-Path" : "${portalPagePath}" },
                    data: "minrowscol=true"
                };
            var request = Y.io(uri, config);
        }
        
        e.halt();
    };
    
    var onPreviewPortletComplete = function(id, o, args) {
        var point = args.complete[0];
        var portletDisplayName = args.complete[1];
        var previewNode = Y.Node.getDOMNode(Y.Node.one("#<portlet:namespace/>previewOverlay"));
        var windowWidth = window.innerWidth;
        var windowHeight = window.innerHeight;
        
        if (!previewOverlay) {
            Y.Node.getDOMNode(Y.Node.one("#<portlet:namespace/>previewOverlay")).style.display = "";
            previewOverlay = new Y.Overlay({
                contentBox: "#<portlet:namespace/>previewOverlay",
                xy: [windowWidth, windowHeight],
                visible: false,
                plugins : [{fn:AnimPlugin, cfg:{duration:0.5}}]
            });
        }
        
        var previewPortletWindow = Y.Node.create(
                "<div class='portlet'>" +
                "<div class='PTitle'><div class='PTitleContent'></div></div>" + 
                "<div class='PContentBorder'><div class='PContent'></div></div>" +
                "</div>"
                );
        previewPortletWindow.one(".PTitleContent").setContent(portletDisplayName);
        previewPortletWindow.one(".PContent").setContent(o.responseText);
        previewOverlay.set("bodyContent", previewPortletWindow);
        previewOverlay.set("zIndex", 16777271);
        previewOverlay.render();

        var windowWidth = window.innerWidth;
        var windowHeight = window.innerHeight;
        var offsetWidth = Math.max(200, previewNode.offsetWidth);
        var offsetHeight = Math.max(150, previewNode.offsetHeight);
        point[0] += 10;
        point[1] -= 10;
        if (point[0] > windowWidth - offsetWidth) point[0] = windowWidth - offsetWidth;
        if (point[1] > windowHeight - offsetHeight) point[1] = windowHeight - offsetHeight;
        
        previewOverlay.move(point[0], point[1]);
        previewOverlay.show();
    };
    
    var previewPortlet = function(e) {
        var a = Y.Node.getDOMNode(e.target);
        var portletUniqueName = a.getAttribute("portletUniqueName");
        var portletDisplayName = a.getAttribute("portletDisplayName");
        if (portletUniqueName) {
            if (previewOverlay) {
                previewOverlay.hide();
            }
            var point = [ e.pageX, e.pageY ];
            var uri = "${portalContextPath}/portlet/?mode=preview&portlet=" + portletUniqueName + "&entity=" + portletUniqueName;
            var request = Y.io(uri, { on: { complete: onPreviewPortletComplete }, arguments: { complete: [ point, portletDisplayName ] } });
        }
        
        e.halt();
    };

    var closePreviewOverlay = function(e) {
        previewOverlay.hide();
        e.halt();
    };

    var clonePortlet = function(e) {
        var a = Y.Node.getDOMNode(e.target);
        var portletUniqueName = a.getAttribute("portletUniqueName");

        var modalFeatures = {
            "width": 600,
            "height": 540
        };
        var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null);
        if (portal) {
            var iframe = Y.Node.one("#<portlet:namespace/>portletCloneModalBodyIFrame");
            iframe.set("src", "${requestContext.portalURL.basePath}/system/prm/cloneportlet.psml?_inheritdeco=${pageDeco}&portlet=" + portletUniqueName);
            portal.showModalPanel("#<portlet:namespace/>portletCloneModalHeader", "#<portlet:namespace/>portletCloneModalBody", "#<portlet:namespace/>portletCloneModalFooter", modalFeatures);
        }
        e.halt();
    };

    var hidePortalModalPanel = function() {
    	var portal = (JETUI_YUI ? JETUI_YUI.getPortalInstance() : null); 
    	if (portal) { 
        	portal.hideModalPanel(); 
        }
    };
    
    Y.on("click", switchTab, "#<portlet:namespace/>portletsTabAction");
    Y.on("click", switchTab, "#<portlet:namespace/>layoutTabAction");
    Y.on("click", switchTab, "#<portlet:namespace/>themeTabAction");
    Y.on("change", loadPortletsInCategory, "#<portlet:namespace/>categories");
    Y.on("submit", loadPortletsByQuery, "#<portlet:namespace/>form");
    
    var pageNavBar = Y.one("#<portlet:namespace/>pageNavBar");
    var navLinks = pageNavBar.all(".jstbPageNav");
    navLinks.each(function(v, k) {
        v.on("click", onPortletPaginationLinkClick);
    });
    Y.one("#<portlet:namespace/>pageNavIndex").on("change", onPortletPaginationSelectChange);
    
    var tabId = Y.Cookie.getSub("JS2TOOLBOX", "TAB");
    if (!tabId) {
        tabId = "portlets";
    }
    
    switchTab(tabId);

    Y.Node.one("#<portlet:namespace/>previewOverlayClose").on("click", closePreviewOverlay);
    Y.Node.one("#<portlet:namespace/>portletCloneModalHeaderClose").on("click", hidePortalModalPanel);
});
</script>