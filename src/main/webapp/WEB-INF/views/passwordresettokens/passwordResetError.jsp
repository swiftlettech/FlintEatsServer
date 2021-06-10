<%@ page session="false" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%><!DOCTYPE html>
<html>
<head>
<title>This is a welcome page</title>
</head>
<body>
<spring:url value="/password-reset/" var="password_reset"/>
<div class="alert alert-warning">
    The token provided is invalid. Please initiate a <a
        href="${password_reset}">new password change request.</a>
</div>
</body>
</html>