package org.ericbeach.location.services;

import org.ericbeach.location.datastore.LocationCoordinatesDatastoreHelper;
import org.ericbeach.location.datastore.NoSuchEntityException;
import org.ericbeach.location.models.LocationCoordinates;
import org.ericbeach.location.models.LocationType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service to find nearby location coordinates.
 */
public class NearbyLocationCoordinatesService {
  private static final Logger log =
      Logger.getLogger(NearbyLocationCoordinatesService.class.getName());
  private LocationCoordinatesDatastoreHelper locationCoordinatesDatastoreHelper =
      new LocationCoordinatesDatastoreHelper();

  public List<LocationCoordinates> getNearbyLocationCoordinates(String emailAddress,
      double acceptableNearbyDistanceInMiles) {
    log.info("Getting nearby location coordinates for " + emailAddress);
    try {
      LocationCoordinates baseLocationCoordinates = locationCoordinatesDatastoreHelper
          .getLocationCoordinatesByUserEmailAndLocationType(emailAddress, LocationType.OFFICE);
      return getNearbyLocationCoordinates(baseLocationCoordinates, acceptableNearbyDistanceInMiles,
          true);
    } catch (NoSuchEntityException e) {
      return new ArrayList<LocationCoordinates>();
    }
  }

  private List<LocationCoordinates> getNearbyLocationCoordinates(LocationCoordinates baseLocation,
      double acceptableNearbyDistanceInMiles, boolean onlyIncludeLocationsOfSameType) {
    log.info("Request to find nearby location coordinates within "
        + acceptableNearbyDistanceInMiles + " miles.");
    List<LocationCoordinates> nearbyLocationCoordinates = new ArrayList<LocationCoordinates>();

    List<LocationCoordinates> allLocationCoordinates =
        locationCoordinatesDatastoreHelper.getAllLocationCoordinates();
    for (LocationCoordinates potentialNearbyLocation : allLocationCoordinates) {
      log.info("Comparing " + baseLocation.toJson() + " with " + potentialNearbyLocation.toJson());
      if ((onlyIncludeLocationsOfSameType && potentialNearbyLocation.getLocationType() !=
          baseLocation.getLocationType()) || baseLocation.equals(potentialNearbyLocation)) {
        continue;
      }

      if (areTwoLocationCoordinatesWithinProximity(baseLocation, potentialNearbyLocation,
          acceptableNearbyDistanceInMiles)) {
        nearbyLocationCoordinates.add(potentialNearbyLocation);
      }
    }

    log.info("Found " + nearbyLocationCoordinates.size() + " nearby location coordinates");
    return nearbyLocationCoordinates;
  }

  private boolean areTwoLocationCoordinatesWithinProximity(LocationCoordinates baseLocation,
      LocationCoordinates potentiallyNearbyLocation, double acceptableDistanceInMiles) {
    double distanceBetweenLocationCoordinatesInMiles =
        getDistanceBetweenTwoPoints(baseLocation.getLatitude(), baseLocation.getLatitude(),
            potentiallyNearbyLocation.getLatitude(), potentiallyNearbyLocation.getLatitude(), 'M');
    return (distanceBetweenLocationCoordinatesInMiles <= acceptableDistanceInMiles);
  }

  /**
   * @see http://www.geodatasource.com/developers/java
   */
  private double getDistanceBetweenTwoPoints(double lat1, double lon1, double lat2, double lon2,
      char unit) {
    double theta = lon1 - lon2;
    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
        * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    dist = Math.acos(dist);
    dist = rad2deg(dist);
    dist = dist * 60 * 1.1515;

    if (unit == 'K') {
      dist = dist * 1.609344;
    } else if (unit == 'N') {
      dist = dist * 0.8684;
    }
    return (dist);
  }

  /**
   * @see http://www.geodatasource.com/developers/java
   */
  private double deg2rad(double deg) {
    return (deg * Math.PI / 180.0);
  }

  /**
   * @see http://www.geodatasource.com/developers/java
   */
  private double rad2deg(double rad) {
    return (rad * 180 / Math.PI);
  }
}
