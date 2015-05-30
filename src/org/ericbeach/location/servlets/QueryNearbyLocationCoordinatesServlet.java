package org.ericbeach.location.servlets;

import org.ericbeach.location.Configuration;
import org.ericbeach.location.datastore.LocationCoordinatesDatastoreHelper;
import org.ericbeach.location.models.LocationCoordinates;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;
import org.ericbeach.location.services.NearbyLocationCoordinatesService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle finding nearby location coordinates.
 */
@SuppressWarnings("serial")
public class QueryNearbyLocationCoordinatesServlet extends HttpServlet {
  private static final Logger log =
      Logger.getLogger(FindNearbyLocationCoordinatesServlet.class.getName());

  private final LockoutUnauthorizedUsersService loakoutUnauthorizedUsersService =
      new LockoutUnauthorizedUsersService();

  private static final int PAGE_MODE_RESULTS = 1;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedUsers(req, resp);
    log.info("User authorized, returning nearby locations query page");

    String html = "<!DOCTYPE html>"
        + "<html>"
        + "<head>"
        + "  <link rel=\"stylesheet\" type=\"text/css\" href=\"/static/css/style.css\">"
        + "  <meta charset=\"utf-8\">"
        + "  <title>" + Configuration.WEBSITE_TITLE + "</title>"
        + "</head>"
        + "<body>";
    html += getHtmlForQueryInput();

    if (req.getParameter("page_mode") != null
        && req.getParameter("distance") != null
        && Integer.parseInt(req.getParameter("page_mode"), 10) == PAGE_MODE_RESULTS) {

      // Load a cache of all location coordinates to prevent API quota problems.
      LocationCoordinatesDatastoreHelper locationCoordinatesDatastoreService =
          new LocationCoordinatesDatastoreHelper();
      List<LocationCoordinates> allLocationCoordinates =
          locationCoordinatesDatastoreService.getAllLocationCoordinates();

      NearbyLocationCoordinatesService findNearbyLocationCoordinatesService =
          new NearbyLocationCoordinatesService(allLocationCoordinates);

      // STEP 1: Get Variables
      List<String> baseUsersEmailAddressesToQuery = getListOfEmailAddressesToQueryFrom(req);
      double distanceInMilesConsideredCloseby = Double.parseDouble(
          req.getParameter("distance"));

      for (String authorizedUserEmailToQuery : baseUsersEmailAddressesToQuery) {
        // STEP 2: For Each Authorized User, Look for Nearby Location Coordinates
        List<LocationCoordinates> nearbyLocationCoordinates =
            findNearbyLocationCoordinatesService.getNearbyLocationCoordinates(
                authorizedUserEmailToQuery, distanceInMilesConsideredCloseby);
        log.info("For user " + authorizedUserEmailToQuery + " "
                + nearbyLocationCoordinates.size() + " nearby location coordinates found");

        // STEP 3: Print out nearby location coordinates
        html += "<h3>" + authorizedUserEmailToQuery + "</h3>"; 
        for (LocationCoordinates nearbyCoordinate : nearbyLocationCoordinates) {
          html += "<p>" + nearbyCoordinate.getUserEmail() + "</p>";
        }
      }
      html += getHtmlForQueryResults();
    }

    html += "</body>"
        + "</html>";

    // Add HTTP response header to help prevent clickjacking.
    resp.addHeader("X-Frame-Options", "SAMEORIGIN");
    resp.getWriter().println(html);
  }

  private List<String> getListOfEmailAddressesToQueryFrom(HttpServletRequest req) {
    List<String> emailAddresses = new ArrayList<String>();
    String rawInputs = req.getParameter("emailsToQuery");

    String splieEmailAddresses[] = rawInputs.split(",");
    for (int i = 0; i < splieEmailAddresses.length; i++) {
      emailAddresses.add(splieEmailAddresses[i].trim());
    }

    return emailAddresses;
  }

  private String getHtmlForQueryInput() {
    String html = "<form method\"GET\" action=\"/queryNearbyLocationCoordinates\">"
        + "<input type=\"text\" name=\"emailsToQuery\" size=\"50\" required "
        + "  placeholder=\"email addresses\"/>"
        + "&nbsp; <input type=\"number\" size=\"10\" step=\"any\" required"
        + "  placeholder=\"miles\" name=\"distance\" min=\"0\" max=\"180\" />"
        + " <input type=\"hidden\" name=\"page_mode\" value=\"" + PAGE_MODE_RESULTS + "\" />"
        + "<button type=\"submit\">Query</button>"
        + "</form>";
    return html;
  }

  private String getHtmlForQueryResults() {
    String html = "";
    return html;
  }
}
