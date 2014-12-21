package org.ericbeach.location.datastore;

import org.ericbeach.location.models.LocationCoordinates;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper for the datastore that fetches authorized location coordinates entity.
 */
public class LocationCoordinatesDatastoreHelper {
  private static final Logger log =
      Logger.getLogger(AuthorizedUserDatastoreHelper.class.getName());

  private static final String LOCATION_COORDINATES_ENTITY_NAME = "location_coordinates";
  public static final String LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME = "email";
  public static final String LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME = "latitude";
  public static final String LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME = "longitude";
  public static final String LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME = "location_type";

  private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  public void updateLocationCoordinatesEntity(final String userEmailAddress, final double latitude,
      final double longitude, final int locationType) {
    // Remove any existing location coordinate with the same email + type to prevent duplicates.
    deleteLocationCoordinatesEntity(userEmailAddress, locationType);

    log.info("Updating location coordinates for user " + userEmailAddress + " to "
      + " latitude, longitude: " + latitude + ", " + longitude);
    Entity locationEntity = new Entity(LOCATION_COORDINATES_ENTITY_NAME);
    locationEntity.setProperty(LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME,
        userEmailAddress);
    locationEntity.setProperty(LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME,
        latitude);
    locationEntity.setProperty(LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME,
        longitude);
    locationEntity.setProperty(LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME,
        locationType);
    datastoreService.put(locationEntity);
  }

  public void deleteLocationCoordinatesEntity(final String userEmailAddress,
      final int locationType) {
    Entity locationCoordinatesEntity =
        getLocationCoordinatesEntityByUserEmailAndLocationType(userEmailAddress, locationType);
    if (locationCoordinatesEntity != null) {
      datastoreService.delete(locationCoordinatesEntity.getKey());
    }
  }

  private Entity getLocationCoordinatesEntityByUserEmailAndLocationType(
      final String userEmailAddress, final int locationType) {
    Filter userEmailFilter = new FilterPredicate(LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME,
        FilterOperator.EQUAL,
        userEmailAddress);
    Filter locationTypeFilter = new FilterPredicate(
        LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME,
        FilterOperator.EQUAL,
        locationType);

    Filter compositeFilter = new CompositeFilter(CompositeFilterOperator.AND,
        Arrays.asList(userEmailFilter, locationTypeFilter));

    Query query = new Query(LOCATION_COORDINATES_ENTITY_NAME).setFilter(compositeFilter);
    List<Entity> queryResultList =
        datastoreService.prepare(query).asList(FetchOptions.Builder.withLimit(1));

    if (queryResultList.size() == 1) {
      return queryResultList.get(0);
    } else {
      return null;
    }
  }

  public List<LocationCoordinates> getAllLocationCoordinates() {
    Query query = new Query(LOCATION_COORDINATES_ENTITY_NAME);
    List<Entity> listOfLocationCoordinates =
        datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());

    List<LocationCoordinates> locationCoordinates = new ArrayList<LocationCoordinates>();
    for (Entity locationCoordinatesEntity : listOfLocationCoordinates) {
      locationCoordinates.add(new LocationCoordinates(
          (double) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_LATITUDE_PROPERTY_NAME),
          (double) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_LONGITUDE_PROPERTY_NAME),
          (int) (long) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_LOCATION_TYPE_PROPERTY_NAME),
          (String) locationCoordinatesEntity.getProperty(
              LOCATION_COORDINATES_USER_EMAIL_PROPERTY_NAME)
          ));
    }

    return locationCoordinates;
  }
}
