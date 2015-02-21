package org.ericbeach.location.servlets.admin;

import org.ericbeach.location.datastore.LocationCoordinatesDatastoreHelper;
import org.ericbeach.location.models.LocationCoordinates;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to view location coordinates.
 */
@SuppressWarnings("serial")
public class ViewLocationCoordinatesServlet extends HttpServlet {
  private static final Logger log =
      Logger.getLogger(ViewLocationCoordinatesServlet.class.getName());

  private final LockoutUnauthorizedUsersService loakoutUnauthorizedUsersService =
      new LockoutUnauthorizedUsersService();
  private final LocationCoordinatesDatastoreHelper locationCoordinatesDatastoreHelper =
      new LocationCoordinatesDatastoreHelper();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedAdmins(req, resp);

    log.info("Requesting default edit location coordinates view.");

    // Add HTTP response header to help prevent clickjacking.
    resp.addHeader("X-Frame-Options", "SAMEORIGIN");
    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages()
        + getDefaultViewHtml()
        + CommonAdmin.getHtmlForBottomOfAdminPages());
  }

  private String getDefaultViewHtml() {
    String htmlContents = ""
      + "  <a name=\"view-locations\"></a>"
      + "  <h1>View Location Coordinates</h1>";

    htmlContents += "<table>"
        + "<tr><th>Email</th><th>Latitude</th><th>Longitude</th></tr>";
    List<LocationCoordinates> allLocationCoordinates =
        locationCoordinatesDatastoreHelper.getAllLocationCoordinates();
    for (LocationCoordinates locationCoordinates : allLocationCoordinates) {
      htmlContents += "<tr>"
          + "<td>" + locationCoordinates.getUserEmail() + "</td>"
          + "<td>" + locationCoordinates.getLatitude() + "</td>"
          + "<td>" + locationCoordinates.getLongitude() + "</td>"
          + "</tr>";
    }
    htmlContents += "</table>";
    return htmlContents;
  }
}
