package org.ericbeach.location.servlets;

import org.ericbeach.location.Configuration;
import org.ericbeach.location.models.LocationType;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;
import org.ericbeach.location.services.UsersService;

import com.google.apphosting.api.ApiProxy.OverQuotaException;

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
  private final UsersService usersService = new UsersService();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    try {
      loakoutUnauthorizedUsersService.lockoutUnauthorizedUsers(req, resp);
    } catch (OverQuotaException exception) {
      log.warning("Over quota request");
      resp.sendRedirect("/static/quota.html");
      resp.flushBuffer();
    }
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
    String keyString = "";
    if (Configuration.GOOGLE_MAPS_API_KEY.length() > 0) {
      keyString = "key=" + Configuration.GOOGLE_MAPS_API_KEY + "&";
    }
    String htmlContents = "<!DOCTYPE html>"
        + "<html class=\"app-entry-point\">"
        + "<head>"
        + "  <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">"
        + "  <meta charset=\"utf-8\">"
        + "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/static/css/style.css\">"
        + "  <title>" + Configuration.WEBSITE_TITLE + "</title>"
        + "  <script src=\"https://maps.googleapis.com/maps/api/js?" + keyString + "v=3\""
        + "      type=\"text/javascript\"></script>"
        + "  <script src=\"/static/js/locationFinderAppConfig.js\" type=\"text/javascript\"></script>"
        + "  <script src=\"/static/js/geocodeHelper.js\" type=\"text/javascript\"></script>"
        + "  <script src=\"/static/js/locationFinderApp.js\" type=\"text/javascript\"></script>"
        + "</head>"
        + "<body class=\"app-entry-point\">"

        + "  <div id=\"full-screen-notification-container\" class=\"hidden\">"
        
        + "   <div id=\"notification-contents-container\">"
        + "     <div id=\"notification-contents\">"
        + "       <div id=\"close-notification-button\" class=\"notification-close-icon\">X</div>"
        + "       <div id=\"notification-contents-text\"></div>"
        + "     </div>"
        + "   </div>"
        + "  </div>"

        + "  <header>"
        + "    <input type=\"text\" id=\"location-address\" placeholder=\"location address for "
        +     usersService.getCurrentUserEmailAddress() + "\" "
        +      "size=\"43\" />"
        + "    <select id=\"location_type\" name=\"locationType\">"
        + "      <option value=\"" + LocationType.OFFICE + "\">OFFICE</option>"
        + "      <option value=\"" + LocationType.HOME + "\">HOME</option>"
        + "    </select> "
        + "    <button id=\"btn-set-location\">Set Location</button> "
        + "    <button class=\"hidden\">Remove</button>"
        + "  </header>"
        + "    <main>"
        + "      <div id=\"map-canvas\"></div>"
        + "      <div id=\"custom-legend\">"
        + "        <p><img src=\"static/img/red_dot.png\" /> Office</p>"
        + "        <p><img src=\"static/img/yellow_dot.png\" /> Home</p>"
        + "      </div>"
        + "    </main>"
        + "  </body>"
        + "</html>";
    return htmlContents;
  }
}
