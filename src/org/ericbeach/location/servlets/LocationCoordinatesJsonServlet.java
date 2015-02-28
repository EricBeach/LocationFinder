package org.ericbeach.location.servlets;

import org.ericbeach.location.services.LockoutUnauthorizedUsersService;
import org.ericbeach.location.services.UsersService;
import org.ericbeach.location.datastore.LocationCoordinatesDatastoreHelper;
import org.ericbeach.location.models.LocationCoordinates;
import org.ericbeach.location.models.LocationType;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for list of location coordinates.
 */
@SuppressWarnings("serial")
public class LocationCoordinatesJsonServlet extends HttpServlet {
  private static final Logger log =
      Logger.getLogger(LocationCoordinatesJsonServlet.class.getName());
  private final LockoutUnauthorizedUsersService loakoutUnauthorizedUsersService =
      new LockoutUnauthorizedUsersService();
  private final UsersService usersService =
      new UsersService();
  private final LocationCoordinatesDatastoreHelper locationCoordinatesHelperService =
      new LocationCoordinatesDatastoreHelper();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedUsers(req, resp);

    resp.setContentType("application/json");
    resp.getWriter().println(getLocationCoordinatesJavaScript());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Note: You MUST call lockoutUnauthorizedUsers() to ensure all requests are authorized.
    loakoutUnauthorizedUsersService.lockoutUnauthorizedUsers(req, resp);

    double latitude = Double.parseDouble(req.getParameter(
        LocationCoordinatesDatastoreHelper.LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME));
    double longitude = Double.parseDouble(req.getParameter(
        LocationCoordinatesDatastoreHelper.LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME));
    int locationType = LocationType.parseLocationType(req.getParameter(
        LocationCoordinatesDatastoreHelper.LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME));

    log.info("Received request to add coordinateses: latitude " + latitude + ", longitude "
        + longitude + ", location type " + locationType);

    resp.setContentType("application/json");
    String newLocationCoordinatesJson = LocationCoordinates.toJson(
        latitude, longitude, locationType, usersService.getCurrentUserEmailAddress());
    String responseContents = "{"
        + "\"newLocationCoordinates\": " + newLocationCoordinatesJson;

    locationCoordinatesHelperService.addOrUpdateLocationCoordinatesEntity(
        usersService.getCurrentUserEmailAddress(),
        latitude, longitude, locationType);

    responseContents += "}";
    resp.getWriter().println(responseContents);
  }

  private String getLocationCoordinatesJavaScript() {    
    List<LocationCoordinates> listOfLocationCoordinates =
        locationCoordinatesHelperService.getAllLocationCoordinates();
    log.info("Returning " + listOfLocationCoordinates.size() + " location coordinates");

    String returnJavaScript = "[";
    for (LocationCoordinates locationCoordinates : listOfLocationCoordinates) {
      returnJavaScript += locationCoordinates.toJson() + ",";
    }

    // Eliminate the final "," which isn't valid JSON. Only do this if there are elements.
    if (returnJavaScript.charAt(returnJavaScript.length() - 1) == ',') {
      returnJavaScript = returnJavaScript.substring(0, returnJavaScript.length() - 1);
    }
    returnJavaScript += "]";
    return returnJavaScript;
  }
}
