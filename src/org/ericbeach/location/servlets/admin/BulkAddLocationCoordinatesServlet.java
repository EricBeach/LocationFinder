package org.ericbeach.location.servlets.admin;

import org.ericbeach.location.datastore.AuthorizedUserDatastoreHelper;
import org.ericbeach.location.datastore.LocationCoordinatesDatastoreHelper;
import org.ericbeach.location.models.AuthorizedUser;
import org.ericbeach.location.models.LocationCoordinates;
import org.ericbeach.location.services.LockoutUnauthorizedUsersService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to bulk add location coordinates.
 */
@SuppressWarnings("serial")
public class BulkAddLocationCoordinatesServlet extends HttpServlet {
  private static final Logger log =
      Logger.getLogger(BulkAddLocationCoordinatesServlet.class.getName());

  private static final String DATA_FORM_FIELD_NAME = "data";
  private static final String IS_PREVIEW_FORM_FIELD_NAME = "isPreview";

  private final LockoutUnauthorizedUsersService loakoutUnauthorizedUsersService =
      new LockoutUnauthorizedUsersService();
  private final LocationCoordinatesDatastoreHelper locationCoordinatesDatastoreHelper =
      new LocationCoordinatesDatastoreHelper();
  private final AuthorizedUserDatastoreHelper authorizedUserDatastoreHelper =
      new AuthorizedUserDatastoreHelper();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedAdmins(req, resp);

    log.info("Requesting default add bulk location coordinates view.");

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

    String data = req.getParameter(DATA_FORM_FIELD_NAME);
    boolean isPreviewMode = true;
    if (req.getParameter(IS_PREVIEW_FORM_FIELD_NAME) == null) {
      isPreviewMode = false;
    }
    log.info("Preview mode: " + isPreviewMode);

    String htmlContents = "";
    try {
      // Parse Input.
      List<String> rowsOfDataEntered = parseRawInputDataIntoRows(data);
      List<LocationCoordinates> locationCoordinatesEntered =
          parseRowIntoLocationCoordinates(rowsOfDataEntered);
      Map<String, AuthorizedUser> authorizedUsersEntered =
          parseRowIntoAuthorizedUsers(rowsOfDataEntered);

      if (authorizedUsersEntered.size() != locationCoordinatesEntered.size()) {
        log.warning("Count of parsed users off from count of parsed coordinates");
        throw new Exception();
      }

      // Save input to datastore.
      if (!isPreviewMode) {
        for (Map.Entry<String, AuthorizedUser> authorizedUser : authorizedUsersEntered.entrySet()) {
          AuthorizedUser authorizedUserToEnter = authorizedUser.getValue();
          authorizedUserDatastoreHelper.addAuthorizedUser(
              authorizedUserToEnter.getEmail(), authorizedUserToEnter.getDisplayName());
        }

        for (LocationCoordinates locationCoordinate : locationCoordinatesEntered) {
          locationCoordinatesDatastoreHelper.addOrUpdateLocationCoordinatesEntity(
              locationCoordinate.getUserEmail(),
              locationCoordinate.getLatitude(),
              locationCoordinate.getLongitude(),
              locationCoordinate.getLocationType());
        }
      }

      // Display inputed data.
      if (isPreviewMode) {
        htmlContents += "<p>Preview Mode -- No Changes Made</p>";
      } else {
        htmlContents += "<p>Following Locations Added</p>";
      }
      for (LocationCoordinates locationCoordinate : locationCoordinatesEntered) {
        htmlContents += "<pre>" + authorizedUsersEntered.get(locationCoordinate.getUserEmail()).toJson() + " // " +  locationCoordinate.toJson() + "</pre>";
      }

    } catch (Exception exception) {
      htmlContents += "Error parsing input data.";
      exception.printStackTrace();
      log.warning("Error parsing input data");
    }

    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages() + htmlContents
        + CommonAdmin.getHtmlForBottomOfAdminPages());
  }

  private List<String> parseRawInputDataIntoRows(String rawInputData) throws Exception {
    List<String> rows = new ArrayList<String>();
    String lines[] = rawInputData.split("\\r?\\n");
    if (lines.length == 0) {
      log.warning("Error parsing input data into row");
      throw new Exception();
    } else {
      log.info("Parsed input data into " + lines.length + " rows");
      rows = Arrays.asList(lines);
    }
    return rows;
  }

  private Map<String, AuthorizedUser> parseRowIntoAuthorizedUsers(
      List<String> rows) throws Exception {
    Map<String, AuthorizedUser> users = new HashMap<String, AuthorizedUser>();

    for (String row : rows) {
      String rowComponents[] = row.split(",");
      if (rowComponents.length != 5) {
        log.warning("Error parsing row into authorized users");
        throw new Exception();
      }

      String displayName = rowComponents[0].trim();
      String email = rowComponents[1].trim();
      AuthorizedUser parsedAuthorizedUser =
          new AuthorizedUser(email, displayName);
      users.put(email, parsedAuthorizedUser);
    }

    return users;
  }

  private List<LocationCoordinates> parseRowIntoLocationCoordinates(
      List<String> rows) throws Exception {
    List<LocationCoordinates> coordinates = new ArrayList<LocationCoordinates>();

    for (String row : rows) {
      String rowComponents[] = row.split(",");
      if (rowComponents.length != 5) {
        log.warning("Error parsing row into location coordinates");
        throw new Exception();
      }

      String email = rowComponents[1].trim();
      double latitude = Double.parseDouble(rowComponents[2].trim());
      double longitude = Double.parseDouble(rowComponents[3].trim());
      int locationType = Integer.parseInt(rowComponents[4].trim());
      LocationCoordinates parsedLocationCoordinates =
          new LocationCoordinates(latitude, longitude, locationType, email);
      coordinates.add(parsedLocationCoordinates);
    }

    return coordinates;
  }

  private String getDefaultViewHtml() {
    String htmlContents = ""
      + "  <a name=\"add-location\"></a>"
      + "  <h1>Bulk Add Location Coordinates</h1>"
      + "  <h2 style=\"color: red\">WARNING: Use only if you really know what you're doing</h2>"
      + "  <p>Enter in the form of:"
      + "  <pre>display name, email address, lat, long, location type</pre>"
      + "  </p>"
      + "  <p>Location type 0 = office address</p>"
      + "  <p>For example:"
      + "  <pre>John Doe, user@domain.com, 38.900996, -77.017605, 0</pre>"
      + "  <pre>Sam Smith, another.user@anotherdomain.com, 38.890927, -76.999056, 0</pre>"
      + "  </p>"
      + "  <form action=\"/admin/_/bulk_add_location_coordinates\" method=\"post\">"
      + "  <textarea name=\"data\" rows=\"10\" cols=\"150\"></textarea>"
      + "  <p>Is Preview: <input type=\"checkbox\" name=\"" + IS_PREVIEW_FORM_FIELD_NAME + "\" checked></p>"
      + "  <p><button type=\"submit\" id=\"bulkAddlocationCoordinatesCheckFormDataBtn\">Bulk Add Form Data</button></p>"
      + "  </form>"
      + "  <p id=\"notificationText\"></p>";
    return htmlContents;
  }
}
