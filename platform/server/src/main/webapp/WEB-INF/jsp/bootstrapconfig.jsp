<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>MOTECH - Mobile Technology for Community Health</title>

    <%@ include file="header.jsp" %>

    <script type="text/javascript" src="<%=request.getContextPath()%>/static/js/bootstrap-page.js"></script>

    <c:if test="${redirect}">
        <script type="text/javascript">
            $(document).ready(function() {
                startLoading();
            });
        </script>
    </c:if>
</head>
<body class="body-startup" ng-app="bootstrapApp" ng-controller="bootstrapFormController" >
<div class="bodywrap">
    <div class="navbar-collapse hidden-xs">
        <div class="margin-before5"></div>
    </div>
    <div class="clearfix"></div>
    <div class="startup">
        <a href=".">
            <div class="startup-logo" ng-cloak>
                <img src="../../static/common/img/motech-logo.gif" alt="motech-logo">
            </div>
        </a>
        <div class="clearfix"></div>
        <div class="startup-strip">
            <div class="form-group" ng-cloak>
                <c:choose>
                    <c:when test="${redirect}">
                        <h2 id="loadingTitle" class="title"><spring:message code="server.bootstrap.loading"/></h2>
                        <h2 id="loadingFailureTitle" class="title" hidden="true"><spring:message code="server.bootstrap.loading.failure"/></h2>
                    </c:when>
                    <c:otherwise>
                        <h2 class="title"><spring:message code="server.welcome.startup"/></h2>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="clearfix"></div>
        <c:choose>
            <c:when test="${redirect}">
                <div class="margin-before margin-after loadingbar" ng-cloak>
                    <div class="progress">
                        <div id="startupProgressPercentage" class="progress-bar progress-bar-striped active" style="min-width: 1em; width: 0%;" aria-valuemax="100" aria-valuemin="0" aria-valuenow="0" role="progressbar"></div>
                    </div>
                    <div class="loading-text-block first" id="loading-org.motechproject.motech-platform-osgi-web-util"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform OSGi Web Util</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-config-core"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Config Core</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-commons-sql"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Commons SQL</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-event"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Event</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-dataservices"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Data Services</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-server-config"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Server Config</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-email"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Email</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-web-security"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Web Security</div>
                    <div class="loading-text-block" id="loading-org.motechproject.motech-platform-server-bundle"><img class="circle-loader fa-lg" src="../../static/common/img/circle-loader.gif"><img class="circle-loader-spinner fa-lg" src="../../static/common/img/circle-loader-spinner.gif"><i class="fa fa-check-circle fa-lg icon-green icon-green"></i><i class="fa fa-times-circle fa-lg icon-red"></i> MOTECH Platform Server Bundle</div>
                    <div class="alert alert-danger" id="retrievalError" style="display: none;"><spring:message code="server.error.statusRetrieval"/></div>
                    <div id="bundleErrors" hidden>
                        <h4><spring:message code="server.error.startupErrors"/></h4>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="startup-form">
                    <div class="diver" ng-cloak>
                        <form action="#" method="POST" class="form-horizontal bootstrap-config-form col-sm-12" name="bcform">
                            <div class="alerts-container form-group">
                                <c:if test="${not empty errors}">
                                    <div class="alert alert-danger">
                                        <c:forEach var="error" items="${errors}">
                                            <spring:message text="${error}"/>   <br/>
                                        </c:forEach>
                                    </div>
                                </c:if>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.brokerUrl"/></label>
                                <div class="col-sm-6" ng-class="{ 'has-error' : bcform.queueUrl.$invalid }">
                                    <input type="text" class="form-control" id="queueUrl" ng-required="true" name="queueUrl" ng-model="config.queueUrl"/>
                                </div>
                                <div class="col-sm-3">
                                    <span ng-show="bcform.queueUrl.$error.required && !bcform.queueUrl.$pristine" class="form-hint"><spring:message code="server.bootstrap.form.required"/></span>
                                </div>
                                <div class="suggestion col-sm-9">
                                    <div id="queueUrlSuggestion">
                                        <span><i><spring:message code="server.suggestion"/>: </i> ${queueUrlSuggestion}</span>
                                        <button type="button" class="btn btn-default btn-xs" ng-click="config.queueUrl='${queueUrlSuggestion}'"><spring:message code="server.use"/></button>
                                    </div>
                                </div>
                                <input class="btn btn-secondary pull-right button-suggestion" type="button" name="VERIFYACTIVEMQ" ng-disabled="bcform.queueUrl.$error.required" value="<spring:message code="server.bootstrap.verify.amq"/>" onclick="verifyConnection('verifyAmq')"/>
                                <div class="alerts-container col-sm-9">
                                    <div class="alert alert-success" id="verifyAmq-info" style="display:none">
                                        <spring:message code="server.bootstrap.verify.amq.success"/>
                                    </div>
                                    <div class="alert alert-danger" id="verifyAmq-error" style="display:none"></div>
                                    <div class="alert alert-danger" id="verifyAmq-alert" style="display:none"></div>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.sqlUrl"/></label>
                                <div class="col-sm-6" ng-class="{ 'has-error' : bcform.sqlUrl.$invalid }">
                                    <input type="text" class="form-control" id="sqlUrl" ng-required="true" name="sqlUrl" ng-model="config.sqlUrl"/>
                                </div>
                                <div class="col-sm-3">
                                    <span ng-show="bcform.sqlUrl.$error.required && !bcform.sqlUrl.$pristine" class="form-hint"><spring:message code="server.bootstrap.form.required"/></span>
                                </div>
                                <div class="suggestion col-sm-9">
                                    <div id="mysqlUrlSuggestion">
                                        <span><i><spring:message code="server.suggestion"/>#1: </i> ${mysqlUrlSuggestion}</span>
                                        <button type="button" class="btn btn-default btn-xs" ng-click="config.sqlUrl='${mysqlUrlSuggestion}'"><spring:message code="server.use"/></button>
                                    </div>
                                </div>
                                <div class="suggestion col-sm-9">
                                    <div id="postgresUrlSuggestion">
                                        <span><i><spring:message code="server.suggestion"/>#2: </i> ${postgresUrlSuggestion}</span>
                                        <button type="button" class="btn btn-default btn-xs" ng-click="config.sqlUrl='${postgresUrlSuggestion}'"><spring:message code="server.use"/></button>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.sqlDriver"/></label>
                                <div class="col-sm-6" ng-class="{ 'has-error' : bcform.sqlDriver.$invalid }">
                                    <input type="text" class="form-control" id="sqlDriver" ng-required="true" name="sqlDriver" ng-model="config.sqlDriver"/>
                                </div>
                                <div class="col-sm-3">
                                    <span ng-show="bcform.sqlDriver.$error.required && !bcform.sqlDriver.$pristine" class="form-hint"><spring:message code="server.bootstrap.form.required"/></span>
                                </div>
                                <div class="suggestion col-sm-9">
                                    <div id="sqlDriverSuggestion">
                                        <span><i><spring:message code="server.suggestion"/>#1: </i> ${mysqlDriverSuggestion}</span>
                                        <button type="button" class="btn btn-default btn-xs" ng-click="config.sqlDriver='${mysqlDriverSuggestion}'"><spring:message code="server.use"/></button>
                                    </div>
                                </div>
                                <div class="suggestion col-sm-9">
                                    <div id="sqlDriverSuggestion">
                                        <span><i><spring:message code="server.suggestion"/>#2: </i> ${postgresDriverSuggestion}</span>
                                        <button type="button" class="btn btn-default btn-xs" ng-click="config.sqlDriver='${postgresDriverSuggestion}'"><spring:message code="server.use"/></button>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.sqlUsername"/></label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control" name="sqlUsername" ng-required="true"  ng-model="config.sqlUserName"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.sqlPassword"/></label>
                                <div class="col-sm-6">
                                    <input type="password" class="form-control" name="sqlPassword" ng-required="true"  ng-model="config.sqlPassword"/>
                                </div>
                                <input class="btn btn-secondary pull-right button-suggestion" type="button" name="VERIFYSQL" ng-disabled="bcform.sqlUrl.$error.required || bcform.sqlDriver.$error.required" value="<spring:message code="server.bootstrap.verifySql"/>" onclick="verifyConnection('verifySql')"/>
                                <div class="alerts-container col-sm-9">
                                    <div class="alert alert-success" id="verifySql-info" style="display:none">
                                        <spring:message code="server.bootstrap.verifySql.success"/>
                                    </div>
                                    <div class="alert alert-danger" id="verifySql-error" style="display:none"></div>
                                    <div class="alert alert-danger" id="verifySql-alert" style="display:none"></div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.felixPath"/></label>
                                 <c:set var="search" value="\\"/>
                                 <c:set var="replace" value="\\\\" />
                                 <c:set var="felixPathReplace" value="${fn:replace(felixPath, search, replace)}" />

                                <div class="col-sm-6" ng-class="{ 'has-error' : bcform.OsgiFrameworkStorage.$invalid }" >
                                    <input type="text"  class="form-control" name="OsgiFrameworkStorage" ng-model="config.OsgiFrameworkStorage" ng-init="config.OsgiFrameworkStorage='${felixPathReplace}'" />
                                </div>
                                <div class="col-sm-3">
                                    <span ng-show="bcform.OsgiFrameworkStorage.$error.required && !bcform.OsgiFrameworkStorage.$pristine""" class="form-hint"><spring:message code="server.bootstrap.form.required"/></span>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.motechPath"/></label>
                                 <c:set var="search" value="\\"/>
                                 <c:set var="replace" value="\\\\" />
                                 <c:set var="motechPathReplace" value="${fn:replace(motechPath, search, replace)}" />

                                <div class="col-sm-6" ng-class="{ 'has-error' : bcform.motechDir.$invalid }" >
                                    <input type="text"  class="form-control" name="motechDir" ng-model="config.motechDir" ng-init="config.motechDir='${motechPathReplace}'" />
                                </div>
                                <div class="col-sm-3">
                                    <span ng-show="bcform.motechDir.$error.required && !bcform.motechDir.$pristine""" class="form-hint"><spring:message code="server.bootstrap.form.required"/></span>
                                </div>
                            </div>
                            <div class="form-group primary-bg">
                                <label class="col-sm-3 control-label"><spring:message code="server.bootstrap.configSource"/></label>
                                <div class="col-sm-9">
                                    <label class="radio-inline">
                                        <input type="radio" value="file" name="configSource" <c:if test="${bootstrapConfig.configSource == 'file'}">checked=checked</c:if> />
                                        <spring:message code="server.configSource.file"/>
                                    </label>
                                    <label class="radio-inline">
                                        <input type="radio" value="ui" name="configSource" <c:if test="${bootstrapConfig.configSource != 'file'}">checked=checked</c:if> />
                                        <spring:message code="server.configSource.ui"/>
                                    </label>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label"><img id="loader" alt="loading" src="../../static/common/img/load.gif" style="display:none"/></label>
                                <div class="col-sm-9">
                                    <input class="btn btn-primary button-suggestion" type="submit" name="BOOTSTRAP" ng-disabled="bcform.sqlUserName.$error.required || bcform.sqlPassword.$error.required || bcform.sqlUrl.$error.required || bcform.sqlDriver.$error.required || bcform.OsgiFrameworkStorage.$error.required || bcform.motechDir.$error.required || bcform.queueUrl.$error.required" ng-click="saveBootstrapData()" value="<spring:message code="server.bootstrap.submit"/>"/>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="clearfix"></div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
