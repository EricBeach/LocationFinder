package org.ericbeach.location.servlets.admin;

import org.ericbeach.location.datastore.LocationCoordinatesDatastoreHelper;
import org.ericbeach.location.datastore.NoSuchEntityException;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to edit location coordinates.
 */
@SuppressWarnings("serial")
public class EditLocationCoordinatesServlet extends HttpServlet {
  private static final Logger log =
      Logger.getLogger(EditLocationCoordinatesServlet.class.getName());

  private static final String ACITON_TYPE_REMOVE_LOCATION_COORDINATES = "REMOVE";
  private static final String ACTION_TYPE_ADD_LOCATION_COORDINATES = "ADD";

  private static final String LATITUDE_FORM_FIELD_NAME = "latitude";
  private static final String LONGITUDE_FORM_FIELD_NAME = "longitude";
  private static final String LOCATION_TYPE_FORM_FIELD_NAME = "locationType";
  private static final String EMAIL_ADDRESS_FORM_FIELD_NAME = "emailAddress";

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
    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages(true)
        + getDefaultViewHtml()
        + CommonAdmin.getHtmlForBottomOfAdminPages());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedAdmins(req, resp);

    String userEmailAddress = req.getParameter(EMAIL_ADDRESS_FORM_FIELD_NAME);
    int locationType = Integer.parseInt(req.getParameter(LOCATION_TYPE_FORM_FIELD_NAME));
    double latitude = Double.parseDouble(req.getParameter(LATITUDE_FORM_FIELD_NAME));
    double longitude = Double.parseDouble(req.getParameter(LONGITUDE_FORM_FIELD_NAME));

    log.info("Performing action: " + req.getParameter("actionType") + " on user "
        + userEmailAddress);

    String htmlContents = "";
    try {
      if (req.getParameter("actionType").equals(ACTION_TYPE_ADD_LOCATION_COORDINATES)) {
        locationCoordinatesDatastoreHelper.addOrUpdateLocationCoordinatesEntity(
            userEmailAddress, latitude, longitude, locationType);
        htmlContents += "<p>Successfully added location coordinates for user "
            + userEmailAddress + " with location type " + locationType + "</p>";
      } else if (req.getParameter("actionType").equals(ACITON_TYPE_REMOVE_LOCATION_COORDINATES)) {
        locationCoordinatesDatastoreHelper.deleteLocationCoordinatesEntity(
            userEmailAddress, locationType);
        htmlContents += "<p>Successfully removed location coordinates for "
            + userEmailAddress + " and location type " + locationType + "</p>";
      }
    } catch (NoSuchEntityException e) {
    }

    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages(true) + htmlContents
        + CommonAdmin.getHtmlForBottomOfAdminPages());
  }

  private String getDefaultViewHtml() {
    String htmlContents = ""
      + "  <a name=\"add-location\"></a>"
      + "  <h1>Add Location Coordinates</h1>"
      + "  <form action=\"/admin/_/edit_location_coordinates\" method=\"post\">"
      + "    <input type=\"email\" size=\"35\" required placeholder=\"email address to add\""
      + "        name=\"" + EMAIL_ADDRESS_FORM_FIELD_NAME + "\" />"
      + "    <input type=\"number\" size=\"30\" step=\"any\" required"
      + "        placeholder=\"latitude\" name=\"" + LATITUDE_FORM_FIELD_NAME + "\" id=\"latitude\" min=\"-90\" max=\"90\" />"
      + "    <input type=\"number\" size=\"30\" step=\"any\" required"
      + "        placeholder=\"longitude\" name=\"" + LONGITUDE_FORM_FIELD_NAME + "\" id=\"longitude\" min=\"-180\" max=\"180\" />"
      + "    <select name=\"" + LOCATION_TYPE_FORM_FIELD_NAME + "\">"
      + "      <option value=\"0\">OFFICE</option>"
      + "    </select>"
      + "    <input type=\"hidden\" name=\"actionType\" "
      + "        value=\"" +  ACTION_TYPE_ADD_LOCATION_COORDINATES + "\">"
      + "    <p>Email Authorized to Access App: <span id=\"isEmailValid\">no</span></p>"
      + "    <p>Valid Coordinates: <span id=\"areCoordinatesValid\">no</span></p>"
      + "    <p><button type=\"button\" id=\"locationCoordinatesCheckFormDataBtn\">Check Form Data</button></p>"
      + "    <p><button type=\"submit\" id=\"locationCoordinatesAddBtn\" disabled>Add Coordinates</button></p>"
      + "  </form>"
      + "  <p></p>"
      + "  <hr />"
      + "  <p></p>"
      + "  <p>Enter address to Geo Code: <input type=\"text\" id=\"location-address\" "
      + "    placeholder=\"location address\" size=\"55\" /> <button id=\"geocodeBtn\">Geocode</button> </p>"
      + "  <p id=\"notificationText\"></p>";
    return htmlContents;
  }
}
