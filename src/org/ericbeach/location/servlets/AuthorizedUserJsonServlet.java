package org.ericbeach.location.servlets;

import org.ericbeach.location.datastore.AuthorizedUserDatastoreHelper;
import org.ericbeach.location.models.AuthorizedUser;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for authorized users list.
 */
@SuppressWarnings("serial")
public class AuthorizedUserJsonServlet extends HttpServlet {
  private static final Logger log =
      Logger.getLogger(LocationCoordinatesJsonServlet.class.getName());
  private final LockoutUnauthorizedUsersService loakoutUnauthorizedUsersService =
      new LockoutUnauthorizedUsersService();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedUsers(req, resp);

    resp.setContentType("application/json");
    resp.getWriter().println(getAuthorizedUserJavaScriptInfo());
  }

  private String getAuthorizedUserJavaScriptInfo() {
    AuthorizedUserDatastoreHelper authorizedUserDatastoreHelper =
        new AuthorizedUserDatastoreHelper();
    List<AuthorizedUser> allAuthorizedUserObjects =
        authorizedUserDatastoreHelper.getAllAuthorizedUserObjects();

    log.info("Returning " + allAuthorizedUserObjects.size() + " authorized users");

    String returnJavaScript = "[";

    for (AuthorizedUser authorizedUser : allAuthorizedUserObjects) {
      returnJavaScript += "{"
        + "\"" + AuthorizedUserDatastoreHelper.AUTHORIZED_USER_EMAIL_PROPERTY_NAME + "\" : "
        +       "\"" + authorizedUser.getEmail() + "\","
        + "\"" + AuthorizedUserDatastoreHelper.AUTHORIZED_USER_DISPLAY_NAME_PROPERTY_NAME + "\" : "
        +       "\"" + authorizedUser.getDisplayName() + "\""
        + "},";
    }

    // Eliminate the final "," which isn't valid JSON. Only do this is there are elements.
    if (returnJavaScript.charAt(returnJavaScript.length() - 1) == ',') {
      returnJavaScript = returnJavaScript.substring(0, returnJavaScript.length() - 1);
    }
    returnJavaScript += "]";
    return returnJavaScript;
  }
}
