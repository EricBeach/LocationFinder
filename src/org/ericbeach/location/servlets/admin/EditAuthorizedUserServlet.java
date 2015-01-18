package org.ericbeach.location.servlets.admin;

import org.ericbeach.location.datastore.AuthorizedUserDatastoreHelper;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to edit authorized users list.
 */
@SuppressWarnings("serial")
public class EditAuthorizedUserServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(EditAuthorizedUserServlet.class.getName());

  private static final String ACITON_TYPE_REMOVE_USER = "REMOVE";
  private static final String ACTION_TYPE_ADD_USER = "ADD";

  private static final String DISPLAY_NAME_FORM_FIELD_NAME = "displayName";
  private static final String EMAIL_ADDRESS_FORM_FIELD_NAME = "emailAddress";

  private final LockoutUnauthorizedUsersService loakoutUnauthorizedUsersService =
      new LockoutUnauthorizedUsersService();
  private final AuthorizedUserDatastoreHelper authorizedUsersDatastoreHelper =
      new AuthorizedUserDatastoreHelper();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedAdmins(req, resp);

    log.info("Requesting default edit authorized users view.");

    // Add HTTP response header to help prevent clickjacking.
    resp.addHeader("X-Frame-Options", "SAMEORIGIN");
    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages()
        + getDefaultViewHtml()
        + CommonAdmin.getHtmlForBottomOfAdminPages());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedAdmins(req, resp);

    String emailAddressToTakeActionOn = req.getParameter(EMAIL_ADDRESS_FORM_FIELD_NAME);
    String userDisplayName = req.getParameter(DISPLAY_NAME_FORM_FIELD_NAME);

    log.info("Performing action: " + req.getParameter("actionType") + " on user "
        + emailAddressToTakeActionOn);

    String htmlContents = "";
    if (req.getParameter("actionType").equals(ACTION_TYPE_ADD_USER)) {
      authorizedUsersDatastoreHelper.addAuthorizedUser(emailAddressToTakeActionOn, userDisplayName);
      htmlContents += "<p>Successfully granted regular access to "
          + emailAddressToTakeActionOn + "</p>";
    } else if (req.getParameter("actionType").equals(ACITON_TYPE_REMOVE_USER)) {
      authorizedUsersDatastoreHelper.removeAuthorizedUser(emailAddressToTakeActionOn);
      htmlContents += "<p>Successfully removed regular access to "
          + emailAddressToTakeActionOn + "</p>";
    }
    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages() + htmlContents
        + CommonAdmin.getHtmlForBottomOfAdminPages());
  }

  private String getDefaultViewHtml() {
    String htmlContents = ""
      + "  <a name=\"add-user\"></a>"
      + "  <h1>Add User</h1>"
      + "  <form action=\"/admin/_/edit_authorized_users\" method=\"post\">"
      + "    <input type=\"email\" size=\"35\" required placeholder=\"email address to add\""
      + "        name=\"" + EMAIL_ADDRESS_FORM_FIELD_NAME + "\" />"
      + "    <input type=\"text\" size=\"35\" required minlength=\"4\" placeholder=\"name to add\""
      + "        name=\"" + DISPLAY_NAME_FORM_FIELD_NAME + "\" />"
      + "    <input type=\"hidden\" name=\"actionType\" value=\"" +  ACTION_TYPE_ADD_USER + "\">"
      + "    <button type=\"submit\">Add</button> "
      + "  </form>"

      + "  <a name=\"remove-user\"></a>"
      + "  <h1>Remove User</h1>"
      + "  <ul>";

      List<String> listOfAuthorizedEmailAddresses =
          authorizedUsersDatastoreHelper.getAllAuthorizedUserEmailAddresses();
      for (String emailAddress : listOfAuthorizedEmailAddresses) {
        htmlContents += "<li>" + emailAddress + "</li> "
          + "<form action=\"/admin/_/edit_authorized_users\" method=\"post\">"
          + "<input type=\"hidden\" name=\"" + EMAIL_ADDRESS_FORM_FIELD_NAME + "\""
          + "    value=\"" +  emailAddress + "\">"
          + "<input type=\"hidden\" name=\"actionType\" value=\"" + ACITON_TYPE_REMOVE_USER + "\">"
          + "<button type=\"submit\">Remove</button>"
          + "</form>";
      }

      htmlContents += "  </ul>";
    return htmlContents;
  }
}
