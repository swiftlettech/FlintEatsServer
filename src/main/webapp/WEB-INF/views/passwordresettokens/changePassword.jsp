<div xmlns:spring="http://www.springframework.org/tags"
     xmlns:fn="http://java.sun.com/jsp/jstl/functions"
     xmlns:c="http://java.sun.com/jsp/jstl/core"
     xmlns:jsp="http://java.sun.com/JSP/Page"
     version="2.0">
    <jsp:directive.page contentType="text/html;charset=UTF-8"/>
    <jsp:output omit-xml-declaration="yes"/>
    <spring:url value="changePassword" var="form_url"/>

    <form name="f" action="${fn:escapeXml(form_url)}" method="POST">
        <div>
            <input name = "token" type="hidden" value="${token}"/>
            <label for="new_password_one">
                <spring:message code="password_reset_form_password1"/>
            </label>
            <input id="new_password_one" type='password' name='password' style="width:150px"/>
            <spring:message code="password_reset_form_password1_message" var="pwd1_msg" htmlEscape="false"/>

            <script type="text/javascript">
                <c:set var="sec_name_msg1">
                    <spring:escapeBody javaScriptEscape="true">${pwd1_msg}</spring:escapeBody>
                </c:set>
                Spring.addDecoration(new Spring.ElementDecoration({
                    elementId: "new_password_one",
                    widgetType: "dijit.form.ValidationTextBox",
                    widgetAttrs: {promptMessage: "${sec_name_msg1}", required: true}
                }));
            </script>
            <br/>
            <label for="new_password_two">
                <spring:message code="password_reset_form_password2"/>
            </label>
            <input id="new_password_two" type='password' name='new_password_two' style="width:150px"/>
            <spring:message code="password_reset_form_password2_message" var="pwd2_msg" htmlEscape="false"/>
            <script type="text/javascript">
                <c:set var="sec_name_msg2">
                    <spring:escapeBody javaScriptEscape="true">${pwd2_msg}</spring:escapeBody>
                </c:set>
                Spring.addDecoration(new Spring.ElementDecoration({
                    elementId: "new_password_two",
                    widgetType: "dijit.form.ValidationTextBox",
                    widgetAttrs: {promptMessage: "${sec_name_msg}", required: true, constraints: "{'other': 'password'}"}
                }));
            </script>
        </div>
        <br/>
        <p>
            Passwords must be at least 8 characters long and meet 2 of the following requirments:
        </p>
        <ul>
            <li>Have a mix of upper case and lower case letters.</li>
            <li>Have a mix of letters and numbers</li>
            <li>Have at least one special character (%,$,!,@,*,#)</li>
        </ul>
        <div class="submit">
            <script type="text/javascript">Spring.addDecoration(new Spring.ValidateAllDecoration({
                elementId: 'proceed',
                event: 'onclick'
            }));</script>
            <spring:message code="button_submit" var="submit_label" htmlEscape="false"/>
            <input id="proceed" type="submit" value="${fn:escapeXml(submit_label)}"/>
        </div>
        <script type="text/javascript">
            function prepareValidation() {
                dijit.byId("new_password_two").validator = function (value, constraints) {
                    var otherInput =  dijit.byId("new_password_one").value;
                    return value===otherInput;
                }
            }
            dojo.addOnLoad(prepareValidation);
        </script>
    </form>
</div>
