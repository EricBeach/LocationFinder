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

  private static final String FORM_FIELD_NAME__DATA = "data";
  private static final String FORM_FIELD_NAME__DATA_INPUT_MODE = "data_input_mode";
  private static final String IS_PREVIEW_FORM_FIELD_NAME = "isPreview";

  private static final int DATA_INPUT_MODE__AUTHORIZED_USERS_ONLY = 0;
  private static final int DATA_INPUT_MODE__LOCATION_COORDINATES_ONLY = 1;
  private static final int DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES = 2;

  private static final Map<Integer, Integer> requiredInputValuesPerDataInputMode =
      new HashMap<Integer, Integer>();
  static {
    requiredInputValuesPerDataInputMode.put(
        DATA_INPUT_MODE__AUTHORIZED_USERS_ONLY, 2);
    requiredInputValuesPerDataInputMode.put(
        DATA_INPUT_MODE__LOCATION_COORDINATES_ONLY, 4);
    requiredInputValuesPerDataInputMode.put(
        DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES, 5);
  }

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
    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages(false)
        + getDefaultViewHtml()
        + CommonAdmin.getHtmlForBottomOfAdminPages());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedAdmins(req, resp);

    if (req.getParameter(FORM_FIELD_NAME__DATA) == null
        || req.getParameter(FORM_FIELD_NAME__DATA_INPUT_MODE) == null) {
      // We do not have the required fields to proceed.
      return;
    }

    String data = req.getParameter(FORM_FIELD_NAME__DATA);
    Integer dataInputMode = Integer.parseInt(req.getParameter(FORM_FIELD_NAME__DATA_INPUT_MODE));
    if (!requiredInputValuesPerDataInputMode.containsKey(dataInputMode)) {
      // We do not have a valid data input mode, so don't proceed.
      return;
    }

    boolean isPreviewMode = true;
    if (req.getParameter(IS_PREVIEW_FORM_FIELD_NAME) == null) {
      isPreviewMode = false;
    }
    log.info("Preview mode: " + isPreviewMode);

    String htmlContents = "";
    try {
      // Parse Input.
      List<LocationCoordinates> locationCoordinatesEntered = new ArrayList<LocationCoordinates>();
      List<String> rowsOfDataEntered = parseRawInputDataIntoRows(data);
      if (dataInputMode == DATA_INPUT_MODE__LOCATION_COORDINATES_ONLY
          || dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES) {
        locationCoordinatesEntered =
            parseRowIntoLocationCoordinates(rowsOfDataEntered, dataInputMode);
      }

      Map<String, AuthorizedUser> authorizedUsersEntered = new HashMap<String, AuthorizedUser>();
      if (dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_ONLY
          || dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES) {
        authorizedUsersEntered = parseRowIntoAuthorizedUsers(rowsOfDataEntered, dataInputMode);
      }

      if (dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES
          && authorizedUsersEntered.size() != locationCoordinatesEntered.size()) {
        log.warning("Count of parsed users off from count of parsed coordinates");
        throw new Exception();
      }

      // Save input to datastore.
      if (!isPreviewMode) {
        if (dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_ONLY
            || dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES) {
          // Enter Authorized Users
          for (Map.Entry<String, AuthorizedUser> authorizedUser : authorizedUsersEntered.entrySet()) {
            AuthorizedUser authorizedUserToEnter = authorizedUser.getValue();
            authorizedUserDatastoreHelper.addAuthorizedUser(
                authorizedUserToEnter.getEmail(), authorizedUserToEnter.getDisplayName());
          }
        }

        if (dataInputMode == DATA_INPUT_MODE__LOCATION_COORDINATES_ONLY
            || dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES) {
          // Enter Location Coordinates
          for (LocationCoordinates locationCoordinate : locationCoordinatesEntered) {
            locationCoordinatesDatastoreHelper.addOrUpdateLocationCoordinatesEntity(
                locationCoordinate.getUserEmail(),
                locationCoordinate.getLatitude(),
                locationCoordinate.getLongitude(),
                locationCoordinate.getLocationType());
          }
        }
      }

      // Display inputed data.
      if (isPreviewMode) {
        htmlContents += "<h2>Preview Mode -- No Changes Made</h2>";
      } else {
        htmlContents += "<h2>Following Data Added</h2>";
      }
      htmlContents += "<h3>Data Entry Mode: " + dataInputMode + "</h3>";

      if (dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES) {
        for (LocationCoordinates locationCoordinate : locationCoordinatesEntered) {
          htmlContents += "<pre>" + authorizedUsersEntered.get(
              locationCoordinate.getUserEmail()).toJson() + " // "
              +  locationCoordinate.toJson() + "</pre>";
        }
      }
      if (dataInputMode == DATA_INPUT_MODE__AUTHORIZED_USERS_ONLY) {
        for (String authorizedUserKey : authorizedUsersEntered.keySet()) {
          htmlContents += "<pre>" + authorizedUsersEntered.get(
              authorizedUserKey).toJson() + "</pre>";
        }
      }
      if (dataInputMode == DATA_INPUT_MODE__LOCATION_COORDINATES_ONLY) {
        for (LocationCoordinates locationCoordinate : locationCoordinatesEntered) {
          htmlContents += "<pre>" + locationCoordinate.toJson() + "</pre>";
        }
      }

    } catch (Exception exception) {
      htmlContents += "Error parsing input data.";
      exception.printStackTrace();
      log.warning("Error parsing input data");
    }

    resp.getWriter().println(CommonAdmin.getHtmlForTopOfAdminPages(false) + htmlContents
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
      List<String> rows, int dataInputMode) throws Exception {
    Map<String, AuthorizedUser> users = new HashMap<String, AuthorizedUser>();

    for (String row : rows) {
      String rowComponents[] = row.split(",");
      if (rowComponents.length != requiredInputValuesPerDataInputMode.get(dataInputMode)) {
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
      List<String> rows, int dataInputMode) throws Exception {
    List<LocationCoordinates> coordinates = new ArrayList<LocationCoordinates>();

    for (String row : rows) {
      String rowComponents[] = row.split(",");
      if (rowComponents.length != requiredInputValuesPerDataInputMode.get(dataInputMode)) {
        log.warning("Error parsing row into location coordinates");
        throw new Exception();
      }

      int startingIndexOffset = 0;
      if (dataInputMode == DATA_INPUT_MODE__LOCATION_COORDINATES_ONLY) {
        startingIndexOffset = -1;
      }

      String email = rowComponents[startingIndexOffset + 1].trim();
      double latitude = Double.parseDouble(rowComponents[startingIndexOffset + 2].trim());
      double longitude = Double.parseDouble(rowComponents[startingIndexOffset + 3].trim());
      int locationType = Integer.parseInt(rowComponents[startingIndexOffset + 4].trim());
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
      + "  <pre id=\"formatDescription\">display name, email address, lat, long, location type</pre>"
      + "  </p>"
      + "  <p>Location type 0 = office address</p>"
      + "  <p>For example:"
      + "  <pre id=\"formatExample\">John Doe, user@domain.com, 38.900996, -77.017605, 0</pre>"
      + "  </p>"
      + "  <form action=\"/admin/_/bulk_add_location_coordinates\" method=\"post\">"
      + "  <textarea name=\"data\" rows=\"10\" cols=\"150\"></textarea>"
      + "  <p>Is Preview: <input type=\"checkbox\" name=\"" + IS_PREVIEW_FORM_FIELD_NAME + "\" checked></p>"
      + "  <p>Data Input Mode: <select id=\"bulkAddLocationDataInputModeSelector\" width=\"100\""
      + "    name=\"" + FORM_FIELD_NAME__DATA_INPUT_MODE + "\">"
      + "    <option value=\"" + DATA_INPUT_MODE__AUTHORIZED_USERS_AND_LOCATION_COORDINATES + "\">Authorized Users & Location Coordinates</option>"
      + "    <option value=\"" + DATA_INPUT_MODE__LOCATION_COORDINATES_ONLY + "\">Location Coordinates Only</option>"
      + "    <option value=\"" + DATA_INPUT_MODE__AUTHORIZED_USERS_ONLY + "\">Authorized Users Only</option></select></p>"
      + "  <p><button type=\"submit\" id=\"bulkAddlocationCoordinatesCheckFormDataBtn\">Bulk Add Form Data</button></p>"
      + "  </form>"
      + "  <p id=\"notificationText\"></p>";
    return htmlContents;
  }
}
