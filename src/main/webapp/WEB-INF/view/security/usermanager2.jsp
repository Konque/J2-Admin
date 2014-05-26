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
<%@ page contentType="text/html" %>
<%@ page import="org.apache.jetspeed.request.RequestContext"%>
<%@ page import="org.apache.jetspeed.security.UserResultList"%>
<%@ page import="org.apache.jetspeed.security.User"%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects/>
<c_rt:set var="requestContext" value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>"/>
<c:set var="portalContextPrefix" value="${requestContext.request.contextPath}"/>
<c:set var="portalContextPath" value="${requestContext.request.contextPath}"/>
<c:set var="portalPagePath" value="${requestContext.portalURL.path}"/>
<c:if test="${empty portalPagePath}">
  <c:set var="portalPagePath" value="/"/>
</c:if>

<div id="searchPage">
	<span>FIND USERS</span>
	<div class="search-form">
		<div id="constraints">
			<div id="constraint" style="clear:both;">
				<select id="constraintKey" style="float:left;width:214px" class="portlet-form-field-label">
					<option value="user">user</option>
					<option value="hasRole">has role</option>
					<option value="hasGroup">has group</option>
					<option disabled="true" value="">---</option>
					<option value="attribute_user.name.given">first name</option>
					<option value="attribute_user.name.family">last name</option>
					<option value="attribute_user.email">email</option>
				</select>
				<div style="float:left;margin-left:1em;width:214px;padding-bottom:0.5em;"> 
					<input id="constraintValue0" type="text" class="portlet-form-field-label" style="font-family:verdana;width:100%"> 
				</div><br/>
			</div>
		</div>	
	</div><br/>
	<div id="filterActionContainer" style="clear:both;width:655px;margin-top:10px; text-align:right">
		<div style="float:left">
			<a id="actionAddConstraint" style="font-family:verdana;" href="#">Add more constraints...</a>
		</div>
		<div style="">
			<input id="actionFindUser" type="button" value="Find" />
		</div><br/>
	</div>
	<hr style="clear:both" align="left" class="hr2"/>
	<div class="yui-skin-sam" style="margin-bottom:20px">
		<div id="searchResult" style="width:655px;"></div>
	</div>
	<div style="width:655px;text-align:right">
		<input id="actionNewUser" type="button" value="New User" />
	</div>
</div>

<div id="editUserPage" class="hidden-node">
	<span><a id="actionEditUserBack" href="#">FIND USER</a> | EDIT USER</span>
	<div class="yui-skin-sam">
	<div id="user-properties" class="yui-navset" style="width:665px;">
		<form id="editUserForm">
		<div id="editUserError" class="portlet-msg-error hidden-node"></div>
		
		<%-- overwrite jetspeed defined margin style here for <ul> element --%>
		<ul class="yui-nav" style="margin:0">
			<li class="selected"><a href="#tab1"><em>USER</em></a></li>
			<li><a href="#tab2"><em>ROLES</em></a></li>
			<li><a href="#tab3"><em>GROUPS</em></a></li>

		</ul>            
		<div style="margin-bottom:20px" class="yui-content">
			<div>
				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em">User Name*</div>
					<div style="float:left;width:214px;padding-bottom:1em;"> 
						<input id="inputUserName" name="name" disabled="true" type="text" value="admin" style="width:100%;"/> 
						<input id="inputUserNameHidden" name="name" type="hidden" value="admin" style="width:100%;"/> 
					</div>
				</div>	
				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em"><label for="password">Password*</label></div>
					<div style="float:left;width:214px;padding-bottom:1em;"> 
						<input id="password" name="password" type="password" style="width:100%"/> 
					</div>
				</div>	
				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em">Password (confirm)*</div>
					<div style="float:left;width:214px;padding-bottom:1em;"> 
						<input id="passwordConfirm" name="password_confirm" type="password" style="width:100%"/>
						<p class="portlet-form-field">Please confirm the password.</p> 
					</div>
				</div>

				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em"><label for="credentialUpdateRequired">Need to change password on login</label></div>
					<div style="float:left;width:214px;padding-bottom:1em;">
						<input id="credentialUpdateRequired" name="credential_update_required" value="true" type="checkbox" style=""/> 
					</div>
				</div>

				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em">First Name</div>
					<div style="float:left;width:214px;padding-bottom:1em;"> 
						<input id="inputUserNameGiven" name="user_name_given" type="text" style="width:100%"/> 
					</div>
				</div>

				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em">Last Name</div>
					<div style="float:left;width:214px;padding-bottom:1em;"> 
						<input id="inputUserNameFamily" name="user_name_family" type="text" style="width:100%"/> 
					</div>
				</div>

				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em">Email</div>
					<div style="float:left;width:214px;padding-bottom:1em;"> 
						<input id="inputUserEmail" name="user_email" type="text" style="width:100%"/> 
					</div><br />
				</div>

				<div style="clear:both">
					<div style="float:left;margin-left:20px;margin-right:20px;width:20em"><label for="userEnabled">Enabled</label></div>
					<div style="float:left;width:214px;">
						<input id="userEnabled" name="user_enabled" value="true" type="checkbox" style=""/> 
					</div>
				</div>
			</div>
			<div>
				<div>
					<div style="float:left;margin-left:20px">
						<label for="availableRoles">Available Roles</label><br/>
						<select id="availableRoles" name="top5" size="10" multiple="multiple" style="width:20em;margin-top:2px">
						</select>
					</div>
					<div style="float:left;margin-left:20px;margin-right:20px;padding-top:60px">
						<input id="actionAssignRoles" type="button" name="toRight" value="&gt;"/><br />
						<input id="actionRemoveRoles" type="button" name="toLeft" value="&lt;" style="margin-top:5px"/>
					</div>
					<div style="float:left">
						<label for="assignedRoles" style="">Assigned Roles</label><br/>
						<select id="assignedRoles" name="roles" size="10" multiple="multiple" style="width:20em;margin-top:2px">
						</select>
					</div>
				</div>
			</div>
			<div>
				<div>
					<div style="float:left;margin-left:20px">
						<label for="availableGroups" style="">Available Groups</label><br/>
						<select id="availableGroups" name="top5" size="10" multiple="multiple" style="width:20em;margin-top:2px">
						</select>
					</div>
					<div style="float:left;margin-left:20px;margin-right:20px;padding-top:3em">
						<input id="actionAssignGroups" type="button" name="toRight" value="&gt;"/><br />
						<input id="actionRemoveGroups" type="button" name="toLeft" value="&lt;" style="margin-top:5px"//>
					</div>
					<div style="float:left">
						<label for="assignedGroups" style="">Assigned Groups</label><br/>
						<select id="assignedGroups" name="groups" size="10" multiple="multiple" style="width:20em;margin-top:2px">
						</select>
					</div>
				</div>
			</div>
		</div>
		</form>
	</div>
	</div>
	<div style="clear:both; width:665px; text-align:right">
		<input id="actionUpdateUser" type="button" value="Update"/>
		<input id="actionDeleteUser" style="margin-right:10px" type="button" value="Remove"/>
	</div>
</div>

<div id="newUserPage" class="hidden-node">
	<span><a id="actionNewUserBack" href="#">FIND USER</a> | NEW USER</span>
	<div class="yui-skin-sam">
	<div id="user-properties" class="yui-navset" style="width:665px;">
		<form id="newUserForm">
		<div id="newUserError" class="portlet-msg-error hidden-node"></div>
		<div>
			<div style="clear:both">
				<div style="float:left;margin-left:20px;margin-right:20px;width:20em">User Name*</div>
				<div style="float:left;width:214px;padding-bottom:1em;"> 
					<input id="inputUserName" name="name" type="text" style="width:100%;"/> 
				</div>
			</div>	
			<div style="clear:both">
				<div style="float:left;margin-left:20px;margin-right:20px;width:20em"><label for="password">Password*</label></div>
				<div style="float:left;width:214px;padding-bottom:1em;"> 
					<input id="password" name="password" type="password" style="width:100%"/> 
				</div>
			</div>	
			<div style="clear:both">
				<div style="float:left;margin-left:20px;margin-right:20px;width:20em">Password (confirm)*</div>
				<div style="float:left;width:214px;padding-bottom:1em;"> 
					<input id="passwordConfirm" name="password_confirm" type="password" style="width:100%"/>
					<p class="portlet-form-field">Please confirm the password.</p> 
				</div>
			</div>
			<div style="clear:both">
				<div style="float:left;margin-left:20px;margin-right:20px;width:20em"><label for="credentialUpdateRequired">Need to change password on login</label></div>
				<div style="float:left;width:214px;padding-bottom:1em;">
					<input id="credentialUpdateRequired" name="credential_update_required" value="true" type="checkbox" style=""/> 
				</div>
			</div>
			<div style="clear:both">
				<div style="float:left;margin-left:20px;margin-right:20px;width:20em">First Name</div>
				<div style="float:left;width:214px;padding-bottom:1em;"> 
					<input id="inputUserNameGiven" name="user_name_given" type="text" style="width:100%"/> 
				</div>
			</div>
			<div style="clear:both">
				<div style="float:left;margin-left:20px;margin-right:20px;width:20em">Last Name</div>
				<div style="float:left;width:214px;padding-bottom:1em;"> 
					<input id="inputUserNameFamily" name="user_name_family" type="text" style="width:100%"/> 
				</div>
			</div>
			<div style="clear:both">
				<div style="float:left;margin-left:20px;margin-right:20px;width:20em">Email</div>
				<div style="float:left;width:214px;padding-bottom:1em;"> 
					<input id="inputUserEmail" name="user_email" type="text" style="width:100%"/> 
				</div><br />
			</div>
		</div>
		</form>
	</div>
	</div>
	<div style="clear:both; width:665px; text-align:right">
		<input id="actionCreateUser" type="button" value="Save"/>
	</div>
</div>


<script type="text/javascript">

var constraintUIId = 1;

YUI({combine: true, timeout: 10000}).use('io', 'node', 'json', 'collection', function(Y) {

    YAHOO.widget.Logger.enableBrowserConsole();
	
	
    <%--// convenient method to create a new constraint in the find user filter dialog --%> 
    var getConstraint = function(id) {
		return '<div id="constraint" style="clear:both">'+
					'<select id="constraintKey" style="float:left;width:214px" class="portlet-form-field-label">'+
						'<option value="user">user</option>'+
						'<option value="hasRole">has role</option>'+
						'<option value="hasGroup">has group</option>'+
						'<option disabled="true" value="">---</option>'+
						'<option value="attribute_user.name.given">first name</option>'+
						'<option value="attribute_user.name.family">last name</option>'+
						'<option value="attribute_user.email">email</option>'+
					'</select>'+
					'<div style="float:left;margin-left:1em;width:214px;padding-bottom:0.5em"> '+
						'<input id="constraintValue'+id+'" type="text" class="portlet-form-field-label" style="font-family:verdana;width:100%"> '+
					'</div>'+
					'<div style="float:left;margin-left:1em;width:150px;padding-bottom:0.5em;padding-top:2px"> '+
						'<a id="actionRemoveConstraint'+id+'" href="#" style="font-family:verdana;width:100%">remove</a> '+
					'</div><br/>'+
				'</div>';
    }
    
    <%--//  add a constraint line in the find user filter dialog --%> 
	var addConstraint = function(e) {
        var node = Y.one('#constraints');
        node.append(getConstraint(constraintUIId));  // added as lastChild
		node.get('lastChild').one('#actionRemoveConstraint'+constraintUIId).on('click', removeConstraint);
		constraintUIId++;
    };
    
	<%--//  remove a constraint line in the find user filter dialog --%>
	var removeConstraint = function(e) {
		var node = e.currentTarget.get('parentNode').get('parentNode');
		node.remove();
    };

	<%--//  transform the constraints into a query, fire it and display the results --%>
	var actionFindUser = function(e) {
        var node = Y.one('#constraints');
        var constraintNodes = node.all('#constraint');
        UserResultTable.settings.userName = '';
        UserResultTable.settings.roles = [];
        UserResultTable.settings.groups = [];
        UserResultTable.settings.attributeKeys = [];
        UserResultTable.settings.attributeValues = [];
        for (var i=0; i<constraintNodes.size(); i++) {
        	var cNode = constraintNodes.item(i);
        	var keyNode = cNode.one('#constraintKey')._node;
        	var key = keyNode.options[keyNode.selectedIndex].value;
        	var value = cNode.one('#constraintValue'+i).get('value');
        	if (key == 'user') {
        		UserResultTable.settings.userName = value;
        	} else if (key == 'hasRole') {
        		UserResultTable.settings.roles.push(value);
        	} else if (key == 'hasGroup') {
        		UserResultTable.settings.groups.push(value);
        	} else {
        		if (key.indexOf('attribute_') == 0) {
	        		UserResultTable.settings.attributeKeys.push(key.substr(key.indexOf('_')+1));
	        		UserResultTable.settings.attributeValues.push(value);
        		}
        	}
        }
        UserResultTable.fireQuery();
    };
    
	<%--//  edit a user, get the data from the server --%>
    var actionEditUser = function(userName) {
    	var node = Y.one('#editUserPage');
    	node.toggleClass('hidden-node');
    	var node = Y.one('#searchPage');
    	node.toggleClass('hidden-node');
		var node = Y.one('#editUserError');
		if (!node.hasClass('hidden-node')) {
			node.toggleClass('hidden-node');
		}
    	var uri = "${portalContextPrefix}/services/usermanager/users/"+userName+"/?_type=json";
    	var request = Y.io(uri, { on: { complete: onGetUserByNameComplete } });
    }
    
    <%--// callback for getting a users detail data, display it --%>
    var onGetUserByNameComplete = function(id, o, args) {
        var id = id;
        var data = o.responseText;
        var dataOut = null;

        try {
			Y.log(data);
            dataOut = Y.JSON.parse(data);
            if (!dataOut) {
                Y.log("Error: no data found.");
                return;
            }
        } catch (e) {
            Y.log("Error: " + e.message);
            return;
        }
        Y.one('#inputUserName').set('value', dataOut.name);
        Y.one('#inputUserNameHidden').set('value', dataOut.name);
        Y.one('#inputUserNameGiven').set('value', dataOut.infoMap["user.name.given"]);
        Y.one('#inputUserNameFamily').set('value', dataOut.infoMap["user.name.family"]);
        Y.one('#inputUserEmail').set('value', dataOut.infoMap["user.email"]);
        Y.one('#userEnabled').set('checked', dataOut.enabled);
        Y.one('#credentialUpdateRequired').set('checked', dataOut.credentialUpdateRequired);
		Y.one('#editUserPage * #password').set('value', '');
        Y.one('#editUserPage * #passwordConfirm').set('value', '');
        
        var availableRoles = Y.one('#availableRoles')
        availableRoles.get('children').remove();
        if (dataOut.availableRoles) {
	        for (var i=0; i<dataOut.availableRoles.length; i++) {
	        	availableRoles.appendChild(new Option(dataOut.availableRoles[i], dataOut.availableRoles[i]));
	        }
	    }
        var assignedRoles = Y.one('#assignedRoles')
        assignedRoles.get('children').remove();
        if (dataOut.roles) {
	        for (var i=0; i<dataOut.roles.length; i++) {
	        	assignedRoles.appendChild(new Option(dataOut.roles[i], dataOut.roles[i]));
	        }
		}
        var availableGroups = Y.one('#availableGroups')
        availableGroups.get('children').remove();
        if (dataOut.availableGroups) {
	        for (var i=0; i<dataOut.availableGroups.length; i++) {
	        	availableGroups.appendChild(new Option(dataOut.availableGroups[i], dataOut.availableGroups[i]));
	        }
	    }
        var assignedGroups = Y.one('#assignedGroups')
        assignedGroups.get('children').remove();
        if (dataOut.groups) {
	        for (var i=0; i<dataOut.groups.length; i++) {
	        	assignedGroups.appendChild(new Option(dataOut.groups[i], dataOut.groups[i]));
	        }
	    }
	    userTabView.selectTab(0);
        
    }
    
	<%--//  go back to the find user dialog from the edit user page  --%>
    var actionEditUserBack = function(e) {
    	var node = Y.one('#editUserPage');
    	node.toggleClass('hidden-node');
    	var node = Y.one('#searchPage');
    	node.toggleClass('hidden-node');
    }
    
	<%--//  PUT user detail data back to the service for updating  --%>
    var actionUpdateUserDetail = function() {
		var userName = Y.one('#inputUserNameHidden').get('value');
		doSelectAll(Y.one('#assignedRoles'));
		doSelectAll(Y.one('#assignedGroups'));
		
		// add form data to request
		var formObject = document.getElementById('editUserForm');
		<%--// Since we use the PUT method, we have to add the form data manually.
		// setForm will only include form data automatically for GET and POST requests --%>
		var formData = YAHOO.util.Connect.setForm(formObject);
		var cObj = YAHOO.util.Connect.asyncRequest('POST', '${portalContextPrefix}/services/usermanager/users/'+userName+'/?_type=json', actionUpdateUserDetailDone, formData);
    }
    
	<%--//  callback for updating user data  --%>
    var actionUpdateUserDetailDone = {
    	success: function(o) {
	    	var node = Y.one('#editUserPage');
	    	node.toggleClass('hidden-node');
	    	var node = Y.one('#searchPage');
	    	node.toggleClass('hidden-node');
			UserResultTable.fireQuery(true);

    	},
    	failure: function(o) {
    		Y.log(o);
    		if (!o.responseText) {
    			o.responseText = 'Update user failed with unspecified error!';
    		}
			var node = Y.one('#editUserError');
			node.setContent(o.responseText);
			if (node.hasClass('hidden-node')) {
				node.toggleClass('hidden-node');
			}
    	}
    }; 

	var actionNewUser = function(e) {
    	var node = Y.one('#newUserPage');
    	node.toggleClass('hidden-node');
    	var node = Y.one('#searchPage');
    	node.toggleClass('hidden-node');
    	document.getElementById('newUserForm').reset();
    	
	}

	var actionCreateUser = function(e) {
		// add form data to request
		var formObject = document.getElementById('newUserForm');
		YAHOO.util.Connect.setForm(formObject);
		var cObj = YAHOO.util.Connect.asyncRequest('POST', '${portalContextPrefix}/services/usermanager/users/?_type=json', actionCreateUserDone);
	}

	<%--//  callback for creating a new user --%>
    var actionCreateUserDone = {
    	success: function(o) {
	    	var node = Y.one('#newUserPage');
	    	node.toggleClass('hidden-node');
	    	var node = Y.one('#searchPage');
	    	node.toggleClass('hidden-node');
			UserResultTable.fireQuery(true);
    	},
    	failure: function(o) {
    		Y.log(o);
    		if (!o.responseText) {
    			o.responseText = 'Create user failed with unspecified error!';
    		}
			var node = Y.one('#newUserError');
			node.setContent(o.responseText);
			if (node.hasClass('hidden-node')) {
				node.toggleClass('hidden-node');
			}
    	}
    };
	<%--//  go back to the find user dialog from the new user page  --%>
    var actionNewUserBack = function(e) {
    	var node = Y.one('#newUserPage');
    	node.toggleClass('hidden-node');
    	var node = Y.one('#searchPage');
    	node.toggleClass('hidden-node');
	};

	var actionDeleteUser = function(e) {
		var userName = Y.one('#inputUserNameHidden').get('value');
		var cObj = YAHOO.util.Connect.asyncRequest('DELETE', '${portalContextPrefix}/services/usermanager/users/' + userName + '/?_type=json', actionDeleteUserDone);
	}
	
	<%--//  callback for creating a new user --%>
    var actionDeleteUserDone = {
    	success: function(o) {
	    	var node = Y.one('#editUserPage');
	    	node.toggleClass('hidden-node');
	    	var node = Y.one('#searchPage');
	    	node.toggleClass('hidden-node');
			UserResultTable.fireQuery(true);
    	},
    	failure: function(o) {
    		Y.log(o);
    		if (!o.responseText) {
    			o.responseText = 'Delete user failed with unspecified error!';
    		}
			var node = Y.one('#editUserError');
			node.setContent(o.responseText);
			if (node.hasClass('hidden-node')) {
				node.toggleClass('hidden-node');
			}
    	}
    };
 	

    Y.one('#actionEditUserBack').on('click', actionEditUserBack);
    
    Y.one('#actionAddConstraint').on('click', addConstraint);

    Y.one('#actionFindUser').on('click', actionFindUser);

    Y.one('#actionNewUser').on('click', actionNewUser);
    
    Y.one('#actionCreateUser').on('click', actionCreateUser);
	
	Y.one('#actionUpdateUser').on('click', actionUpdateUserDetail);
	
    Y.one('#actionNewUserBack').on('click', actionNewUserBack);
    
    Y.one('#actionDeleteUser').on('click', actionDeleteUser);
    
    

	<%--//  define the search results table --%>
	var UserResultTable,
		myDataSource,
		myDataTable;
	
	UserResultTable = {
		
		settings: {
			userName: '',
			roles: [],
			groups: [],
			attributeKeys: [],
			attributeValues: []
		},
		
		init: function () {
	        var myColumnDefs = [
	                {key:"userName", label:"User Name", sortable:true, width:200},
	                {key:"firstName", label:"First Name", width:200},
	                {key:"lastName", label:"Last Name", width:200}
            ];
			var myDataSource = new YAHOO.util.DataSource("${portalContextPrefix}/services/usermanager/users/?_type=json&");
	        myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
	        myDataSource.responseSchema = {
	            resultsList: "records",
	            fields: [
	                {key:"userName"},
	                {key:"firstName"},
	                {key:"lastName"}
	            ],
		        metaFields: {
		            totalRecords: "totalRecords" // Access to value in the server response
		        }
	        };
			var oConfigs = {
	                paginator: new YAHOO.widget.Paginator({
	                    rowsPerPage: 10,
						template:'{PreviousPageLink} {PageLinks} {NextPageLink} {CurrentPageReport}',
	                    pageReportTemplate:'{totalRecords} total accounts',
	                    nextPageLinkLabel:'>>',
	                    previousPageLinkLabel:'<<',
	                    alwaysVisible:true
	                }),
	                initialRequest: "start=0&results=10",
	                dynamicData: true, // Enables dynamic server-driven data 
					selectionMode:"single",
					// This configuration item is what builds the query string
    				// passed to the DataSource.
    				generateRequest: this.requestBuilder					
	        };
	        var myDataTable = new YAHOO.widget.DataTable("searchResult", myColumnDefs,
	                myDataSource, oConfigs);
	        // Update totalRecords on the fly with value from server
		    myDataTable.handleDataReturnPayload = function(oRequest, oResponse, oPayload) {
		        oPayload.totalRecords = oResponse.meta.totalRecords;
		        return oPayload;
		    }
	        // Subscribe to events for row selection
	        myDataTable.subscribe("rowMouseoverEvent", myDataTable.onEventHighlightRow);
	        myDataTable.subscribe("rowMouseoutEvent", myDataTable.onEventUnhighlightRow);
	        myDataTable.subscribe("rowClickEvent", myDataTable.onEventSelectRow);
	        
	        myDataTable.subscribe("rowSelectEvent", function() {
				var data = this.getRecordSet().getRecord(this.getSelectedRows()[0])._oData;
				actionEditUser(data.userName);
			});
			
			// Store the DataTable and DataSource for use elsewhere in this script.
    		UserResultTable.myDataSource = myDataSource;
    		UserResultTable.myDataTable = myDataTable;
		},
		/**
		 * This method is passed into the DataTable's "generateRequest" configuration
		 * setting overriding the default generateRequest function. This function puts
		 * together a query string which is passed to the DataSource each time a new
		 * set of data is requested. All of the custom sorting and filtering options
		 * added in by this script are gathered up here and inserted into the
		 * query string.
		 * @param {Object} oState
		 * @param {Object} oSelf
		 * These parameters are explained in detail in DataTable's API
		 * documentation. It's important to note that oState contains
		 * a reference to the paginator and the pagination state and
		 * the column sorting state as well.
		 */
		requestBuilder: function (oState, oSelf) {
			var sort, dir, startIndex, results;
			var startIndex, results;
 
			oState = oState || {pagination: null, sortedBy: null};
			sort = (oState.sortedBy) ? oState.sortedBy.key : oSelf.getColumnSet().keys[0].getKey();
			dir = (oState.sortedBy && oState.sortedBy.dir === DataTable.CLASS_DESC) ? "desc" : "asc"; 
			startIndex = (oState.pagination) ? oState.pagination.recordOffset : 0;
			results = (oState.pagination) ? oState.pagination.rowsPerPage : null;
 
			var resultParams =	"results=" 	+ results +
					"&start=" 	+ startIndex +
					"&sort=" 		+ sort +
					"&dir=" 		+ dir +
				    "&name=" 	+ UserResultTable.settings.userName +
				    "&roles=" 		+ UserResultTable.settings.roles.join(',')
				    + "&groups=" 		+ UserResultTable.settings.groups.join(',');
				    for (var i=0; i<UserResultTable.settings.attributeKeys.length; i++) {
				    	resultParams += "&attribute_key=" 	+ UserResultTable.settings.attributeKeys[i];
				    	resultParams += "&attribute_value=" 	+ UserResultTable.settings.attributeValues[i];
				    }
			return resultParams;
		},
		/**
		 * This method is used to fire off a request for new data for the
		 * DataTable from the DataSource. The new state of the DataTable,
		 * after the request for new data, will be determined here.
		 * @param {Boolean} resetRecordOffset
		 */
		fireQuery: function (keepPagination) {
            var oState = UserResultTable.myDataTable.getState(),
            	request,
            	oCallback;
 
			if (!keepPagination) {
				/* reset the recordOffset.*/
				oState.pagination.recordOffset = 0;
			}
 
			/* using onDataReturnSetRows because that method
			will clear out the old data in the DataTable, making way for
			the new data.*/
			oCallback = {
			    success : UserResultTable.myDataTable.onDataReturnSetRows,
			    failure : UserResultTable.myDataTable.onDataReturnSetRows,
                argument : oState,
			    scope : UserResultTable.myDataTable
			};
 
			// Generate a query string
            request = UserResultTable.myDataTable.get("generateRequest")(oState, UserResultTable.myDataTable);
 
			// Fire off a request for new data.
			UserResultTable.myDataSource.sendRequest(request, oCallback);
    	}
	};
	
	// initialize the results table once.
	UserResultTable.init();

	/*
	* USER DETAILS TAB VIEW
	*/

	var userTabView = new YAHOO.widget.TabView('user-properties');
	
    // assign roles to the user, sort them
    var assignRoles = function(e) {
        var availableRolesNode = Y.one('#availableRoles');
		var selectedRoles = getSelectedOptions(availableRolesNode);
		var assignedRolesNode = Y.one('#assignedRoles');
		Y.Array.each(selectedRoles, function(selectedRole, idx) {
										selectedRole.selected = false;
										assignedRolesNode.appendChild(selectedRole);
		});
		sortOptions(assignedRolesNode);
    };

    // remove roles from a user, sort them
    var removeRoles = function(e) {
        var assignedRolesNode = Y.one('#assignedRoles');
		var selectedRoles = getSelectedOptions(assignedRolesNode);
		var availableRolesNode = Y.one('#availableRoles');
		Y.Array.each(selectedRoles, function(selectedRole, idx) {
										selectedRole.selected = false;
										availableRolesNode.appendChild(selectedRole);
		});
		sortOptions(availableRolesNode);
    };

    // assign groups for a user, sort them
    var assignGroups = function(e) {
        var availableGroupsNode = Y.one('#availableGroups');
		var selectedGroups = getSelectedOptions(availableGroupsNode);
		var assignedGroupsNode = Y.one('#assignedGroups');
		Y.Array.each(selectedGroups, function(selectedGroup, idx) {
										selectedGroup.selected = false;
										assignedGroupsNode.appendChild(selectedGroup);
		});
		sortOptions(assignedGroupsNode);
    };

    // remove groups from a user, sort them
    var removeGroups = function(e) {
        var assignedGroupsNode = Y.one('#assignedGroups');
		var selectedGroups = getSelectedOptions(assignedGroupsNode);
		var availableGroupsNode = Y.one('#availableGroups');
		Y.Array.each(selectedGroups, function(selectedGroup, idx) {
										selectedGroup.selected = false;
										availableGroupsNode.appendChild(selectedGroup);
		});
		sortOptions(availableGroupsNode);
    };

	
	// convenience method to get all selected options from a list box 
	function getSelectedOptions(selectBox) {
		return Y.Array.reduce(
            Y.NodeList.getDOMNodes(selectBox.get('options')), new Array(),
				function(list, value) {
					if (value.selected) {
						list.push(value);
					}
					return list;
				});
	};
	
	// convenience method to select all options from a list box
	function doSelectAll(selectBox) {
		return Y.Array.each(
            Y.NodeList.getDOMNodes(selectBox.get('options')), 
				function(option, i) {
					if (option.selected == false) {
						option.selected = true;
					}
				});
	};
	
	// convenience method to sort options of a select box 
	function sortOptions(selectBox) {
		var my_options = Y.NodeList.getDOMNodes(selectBox.get('children'));

		my_options.sort(function(a,b) {
			if (a.text > b.text) return 1;
			else if (a.text < b.text) return -1;
			else return 0
		});
		selectBox.get('children').each(function(node, idx) {node.remove()});
		Y.Array.each(my_options, function(option) {selectBox.append(option)});
	}
	
    // register callbacks to certain elements
    Y.one('#actionAssignRoles').on('click', assignRoles);
    Y.one('#actionRemoveRoles').on('click', removeRoles);

    Y.one('#actionAssignGroups').on('click', assignGroups);
    Y.one('#actionRemoveGroups').on('click', removeGroups);
	
});


</script>
