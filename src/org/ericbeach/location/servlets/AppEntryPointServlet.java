package org.ericbeach.location.servlets;

import org.ericbeach.location.Configuration;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Main application entry point servlet.
 */
@SuppressWarnings("serial")
public class AppEntryPointServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(AppEntryPointServlet.class.getName());
  private final LockoutUnauthorizedUsersService loakoutUnauthorizedUsersService =
      new LockoutUnauthorizedUsersService();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedUsers(req, resp);
    log.info("User authorized, returning app entry point HTML");

    // Add HTTP response header to help prevent clickjacking.
    resp.addHeader("X-Frame-Options", "SAMEORIGIN");
    resp.getWriter().println(getFrontendHtml());
  }

  /**
   * If the homepage is served as a .html file, we will not be able to employ Java logic
   * to only permit certain authorized users (i.e., permit x@gmail.com but not y@gmail.com). 
   * 
   * WARNING: If you serve a flat .html file instead of serving the HTML through a Java
   * handler, you open up a security hole where a user who is signed into a Gmail account
   * but not authorized to access the site could guess the .html file and view the app.
   * 
   * @return Frontend HTML.
   */
  private String getFrontendHtml() {
    String htmlContents = "<!DOCTYPE html>"
        + "<html class=\"app-entry-point\">"
        + "<head>"
        + "  <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">"
        + "  <meta charset=\"utf-8\">"
        + "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/static/css/style.css\">"
        + "  <title>" + Configuration.WEBSITE_TITLE + "</title>"
        + "  <script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp\""
        + "      type=\"text/javascript\"></script>"
        + "  <script src=\"/static/js/locationFinderApp.js\" type=\"text/javascript\"></script>"
        + "</head>"
        + "<body class=\"app-entry-point\">"
        + "  <header>"
        + "    <input type=\"text\" id=\"location-address\" placeholder=\"location address for "
        +     loakoutUnauthorizedUsersService.getCurrentlyLoggedInUserEmailAddress() + "\" "
        +      "size=\"43\" />"
        + "    <select id=\"location_type\" name=\"locationType\">"
        + "      <option value=\"0\">OFFICE</option></select> "
        + "    <button id=\"btn-set-location\">Set Location</button> "
        + "    <button class=\"hidden\">Remove</button>"
        + "  </header>"
        + "    <main>"
        + "      <div id=\"map-canvas\"></div>"
        + "    </main>"
        + "  </body>"
        + "</html>";
    return htmlContents;
  }
}