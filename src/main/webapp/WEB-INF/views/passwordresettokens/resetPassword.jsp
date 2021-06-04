<div xmlns:spring="http://www.springframework.org/tags"
     xmlns:fn="http://java.sun.com/jsp/jstl/functions"
     xmlns:c="http://java.sun.com/jsp/jstl/core"
     xmlns:jsp="http://java.sun.com/JSP/Page"
     version="2.0">
    <jsp:directive.page contentType="text/html;charset=UTF-8" />
    <jsp:output omit-xml-declaration="yes" />
    <spring:url value="/password-reset" var="form_url" />
    <form name="f" action="${fn:escapeXml(form_url)}" method="POST">
        <div>
            <label for="email">
                <spring:message code="password_reset_form_email" />
            </label>
            <input id="email" type='text' name='email' style="width:150px" />
            <spring:message code="password_reset_form_email_message" var="name_msg" htmlEscape="false" />
            <script type="text/javascript">
                <c:set var="sec_name_msg">
                <spring:escapeBody javaScriptEscape="true">${name_msg}</spring:escapeBody>
                </c:set>
                Spring.addDecoration(new Spring.ElementDecoration({elementId : "email", widgetType : "dijit.form.ValidationTextBox", widgetAttrs : {promptMessage: "${sec_name_msg}", required : true}}));
            </script>
        </div>
        <br />
        <div class="submit">
            <script type="text/javascript">Spring.addDecoration(new Spring.ValidateAllDecoration({elementId:'proceed', event:'onclick'}));</script>
            <spring:message code="button_submit" var="submit_label" htmlEscape="false" />
            <input id="proceed" type="submit" value="${fn:escapeXml(submit_label)}" />
        </div>
    </form>
</div>
