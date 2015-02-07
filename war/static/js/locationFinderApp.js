/**
 * @fileoverview Location finder application.
 */



/**
 * @constructor
 */
MapManager = function() {
  /**
   * @private
   */
  this.mapOptions_ = {
    zoom: 13,
    center: new google.maps.LatLng(38.8951, -77.0367)
  };

  /**
   * @private {!google.maps.Map}
   */
  this.map_ = null;

  /**
   * @private
   */
  this.locationCoordinates_ = [];

  /**
   * @private
   */
  this.authorizedUsers_ = {};

  /**
   * @type {!Array.<google.maps.Marker>}
   * @private
   */
  this.mapMarkers_ = [];

  /**
   * @type {!Array.<google.maps.InfoWindow>}
   * @private
   */
  this.infoWindows_ = [];

  /**
   * @type {!Array.<number>}
   * @private
   */
  this.currentlyOpenInfoWindowIndexes_ = [];
};


/**
 * Initialize.
 */
MapManager.prototype.init = function() {
  this.getCurrentUserLocation_()
      .then(this.initializeMap_.bind(this))
      .then(this.loadLocationCoordinates_.bind(this))
      .then(this.loadAuthorizedUsers_.bind(this))
      .then(this.placeAllDataPointsOnMap_.bind(this));
};


/**
 * @param {number} markerNum
 */
MapManager.prototype.handleMapIconClickedFunction = function(markerNum) {
  // Close all open windows.
  for (var i = 0; i < this.currentlyOpenInfoWindowIndexes_.length; i++) {
    this.infoWindows_[this.currentlyOpenInfoWindowIndexes_[i]].close();
  }
  this.currentlyOpenInfoWindowIndexes_ = [];

  this.infoWindows_[markerNum].open(this.map_, this.mapMarkers_[markerNum]);
  this.currentlyOpenInfoWindowIndexes_.push(markerNum);
};


/**
 * @param {!Object} locationCoordinatesUpdateInformation
 */
MapManager.prototype.updateLocationCoordinates =
    function(locationCoordinatesUpdateInformation) {
  if (locationCoordinatesUpdateInformation.oldLocationCoordinates &&
      locationCoordinatesUpdateInformation.newLocationCoordinates) {
    // Location Updated, old data needs to be updated.
    var indexNumber = this.getIndexNumByLocationCoordinates_(
        locationCoordinatesUpdateInformation.oldLocationCoordinates);
    this.mapMarkers_[indexNumber].setPosition(new google.maps.LatLng(
        locationCoordinatesUpdateInformation.newLocationCoordinates.latitude,
        locationCoordinatesUpdateInformation.newLocationCoordinates.longitude));
    this.locationCoordinates_[indexNumber] =
        locationCoordinatesUpdateInformation.newLocationCoordinates;
  }
};


/**
 * The index number refers to the index of the arrays in this class holding
 * the Map Markers, Info Windows, and Location Coordinates objects.
 * @param {!Object} targetLocationCoordinates
 * @return {number} index of location coordinates array
 * @private
 */
MapManager.prototype.getIndexNumByLocationCoordinates_ =
    function(targetLocationCoordinates) {
  var minIndex = 0;
  var maxIndex = this.locationCoordinates_.length - 1;
  var currentIndex;
  var currentElement;

  while (minIndex <= maxIndex) {
    currentIndex = (minIndex + maxIndex) / 2 | 0;
    currentElement = this.locationCoordinates_[currentIndex];

    if (this.compareTwoLocationCoordinatesObjects_(
        targetLocationCoordinates, currentElement) < 0) {
      maxIndex = currentIndex - 1;
    } else if (this.compareTwoLocationCoordinatesObjects_(
        targetLocationCoordinates, currentElement) > 0) {
      minIndex = currentIndex + 1;
    } else {
      return currentIndex;
    }
  }
  return -1;
};


/**
 * Compare two location coordinates objects. Used for binary search.
 * Compares first by email address, then by location type.
 * @param {!Object} obj1
 * @param {!Object} obj2
 * @return {number} 0 if objects are equal
 * @private
 */
MapManager.prototype.compareTwoLocationCoordinatesObjects_ =
    function(obj1, obj2) {
  var email1 = obj1.email.toLowerCase();
  var email2 = obj2.email.toLowerCase();
  if (email1 < email2) {
    return -1;
  } else if (email1 > email2) {
    return 1;
  } else {
    // Email addresses are equal, so compare by location type.
    var locationType1 = parseInt(obj1.location_type, 10);
    var locationType2 = parseInt(obj2.location_type, 10);
    if (locationType1 < locationType2) {
      return -1;
    } else if (locationType1 > locationType2) {
      return 1;
    } else {
      return 0;
    }
  }
};


/**
 * @return {Promise}
 * @private
 */
MapManager.prototype.getCurrentUserLocation_ = function() {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(function(position) {
        this_.mapOptions_.center = new google.maps.LatLng(
            position.coords.latitude, position.coords.longitude);
        resolve();
      });
    } else {
      resolve();
    }
  });
  return promise;
};


/**
 * @return {Promise}
 * @private
 */
MapManager.prototype.initializeMap_ = function() {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    this_.map_ = new google.maps.Map(
        document.getElementById('map-canvas'), this_.mapOptions_);
    resolve();
  });
  return promise;
};


/**
 * @return {Promise}
 * @private
 */
MapManager.prototype.loadLocationCoordinates_ = function() {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('GET', '/_/getLocationCoordinates');

    req.onload = function() {
      if (req.status == 200) {
        this_.locationCoordinates_ = JSON.parse(req.response);
        resolve();
      }
      else {
        reject(Error(req.statusText));
      }
    };

    // Handle network errors
    req.onerror = function() {
      reject(Error('Network Error'));
    };

    // Make the request
    req.send();
  });
  return promise;
};


/**
 * @return {Promise}
 * @private
 */
MapManager.prototype.loadAuthorizedUsers_ = function() {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('GET', '/_/getAuthorizedUsers');

    req.onload = function() {
      if (req.status == 200) {
        this_.authorizedUsers_ = JSON.parse(req.response);
        resolve();
      }
      else {
        reject(Error(req.statusText));
      }
    };

    // Handle network errors
    req.onerror = function() {
      reject(Error('Network Error'));
    };

    // Make the request
    req.send();
  });
  return promise;
};


/**
 * @private
 */
MapManager.prototype.placeAllDataPointsOnMap_ = function() {
  this.placeDataPointsOnMap_(0, this.locationCoordinates_.length);
};


/**
 * @param {number} beginIndex
 * @param {number} endIndex
 * @private
 */
MapManager.prototype.placeDataPointsOnMap_ = function(beginIndex, endIndex) {
  for (var i = beginIndex; i < endIndex; i++) {
    var locationCoordinate = new google.maps.LatLng(
        this.locationCoordinates_[i].latitude,
        this.locationCoordinates_[i].longitude);

    this.mapMarkers_[i] = new google.maps.Marker({
      position: locationCoordinate,
      map: this.map_,
      icon: 'static/img/red_dot.png'
    });

    var displayName = '';
    var authorizedUser =
        this.authorizedUsers_[this.locationCoordinates_[i].email];
    if (authorizedUser) {
      displayName = authorizedUser.display_name + '<br>';
    }
    this.infoWindows_[i] = new google.maps.InfoWindow({
      content: displayName + this.locationCoordinates_[i].email
    });

    google.maps.event.addListener(this.mapMarkers_[i], 'click',
        this.handleMapIconClickedFunction.bind(this, i));
  }
};



/**
 * @param {!MapManager} mapManager
 * @constructor
 */
GuiHelper = function(mapManager) {
  /**
   * @type {!google.maps.Geocoder}
   * @private
   */
  this.geocoder_ = new google.maps.Geocoder();

  /**
   * @type {!MapManager}
   * @private
   */
  this.mapManager_ = mapManager;
};


/**
 * Initialize the location coordinates helper.
 */
GuiHelper.prototype.init = function() {
  document.getElementById('btn-set-location')
    .addEventListener('click', this.handleSetLocationBtnClicked_.bind(this));
  document.getElementById('close-notification-button')
    .addEventListener('click', function() {
        var notificationContainerElement =
            document.getElementById('full-screen-notification-container');
        notificationContainerElement.className = 'hidden';
      });
};


/**
 * @param {!String} message
 * @private
 */
GuiHelper.prototype.showFullScreenNotification_ = function(message) {
  document.getElementById('notification-contents-text').innerHTML = message;
  var notificationContainerElement =
      document.getElementById('full-screen-notification-container');
  notificationContainerElement.className = 'shown';
};


/**
 * @private
 */
GuiHelper.prototype.handleSetLocationBtnClicked_ = function() {
  var address = document.getElementById('location-address').value;

  var this_ = this;
  this.geocodeAddress_(address)
    .then(this.addLocationCoordinates_.bind(this))
    .then(function(locationCoordinatesUpdateInformation) {
        // LocationCoordinates successfully added on backend.
        if (locationCoordinatesUpdateInformation.oldLocationCoordinates &&
            locationCoordinatesUpdateInformation.newLocationCoordinates) {
          // Location Updated.
          this_.showFullScreenNotification_
          .call(this_, 'Successfully updated location');
          this_.mapManager_.updateLocationCoordinates.call(
              this_.mapManager_,
              locationCoordinatesUpdateInformation);
        } else if (!locationCoordinatesUpdateInformation
                      .oldLocationCoordinates &&
                    locationCoordinatesUpdateInformation
                      .newLocationCoordinates) {
          // Location Added for the first time.
          this_.showFullScreenNotification_
            .call(this_, 'Successfully added location. ' +
              'Please reload to see new location.');
        } else if (locationCoordinatesUpdateInformation
                     .oldLocationCoordinates &&
                   !locationCoordinatesUpdateInformation
                     .newLocationCoordinates) {
          // Location Removed.
          this_.showFullScreenNotification_
            .call(this_, 'Successfully removed location. ' +
              'Map will update upon reloading website.');
        }
      }, function(rejectionReason) {
        this_.showFullScreenNotification_.call(this_,
            rejectionReason);
      });
};


/**
 * @param {{latitude: number, longitude: number}} locationCoordinates
 * @return {Promise}
 * @private
 */
GuiHelper.prototype.addLocationCoordinates_ = function(locationCoordinates) {
  var locationType = parseInt(
      document.getElementById('location_type').value, 10);

  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    var req = new XMLHttpRequest();
    req.open('POST', '/_/postLocationCoordinates');
    req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

    req.onload = function() {
      if (req.status == 200) {
        var locationCoordinatesUpdateInformation = JSON.parse(req.response);
        resolve(locationCoordinatesUpdateInformation);
      }
      else {
        reject(Error('Unknown Error'));
      }
    };

    // Handle network errors
    req.onerror = function() {
      reject(Error('Network Error'));
    };

    // Make the request
    req.send('latitude=' + locationCoordinates.latitude + '&' +
        'longitude=' + locationCoordinates.longitude + '&location_type=' +
        locationType);
  });
  return promise;
};


/**
 * @param {!string} address
 * @return {Promise}
 * @see https://developers.google.com/maps/documentation/javascript/geocoding
 * @private
 */
GuiHelper.prototype.geocodeAddress_ = function(address) {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    this_.geocoder_.geocode({ 'address': address}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        // Note: At times even when you enter a very specific address, Google
        // returns 2 or 3 possible results. The proper way to handle this is to
        // let the user decide which address. Until then, arbitrarily setting
        // cutoff at 4 meaning if Google returns 4 or more addresses, we tell
        // the user to be more specific. If Google returns 3 or less, we
        // simply use the first result.
        if (results.length >= 4) {
          reject('Multiple possible locations found. Please enter a more ' +
              'specific address. For example: 1600 Pennsylvania Ave NW, ' +
              'Washington, DC, 20500');
        } else if (results.length == 0) {
          reject('Unable to find any locations cooresponding to that address.');
        } else if (typeof results[0].geometry.location.lat == 'number' &&
            typeof results[0].geometry.location.lng == 'number') {
          var coordinates = {
            latitude: results[0].geometry.location.lat,
            longitude: results[0].geometry.location.lng
          };
          resolve(coordinates);
        } else if (typeof results[0].geometry.location.D == 'number' &&
                   typeof results[0].geometry.location.k == 'number') {
          var coordinates = {
            latitude: results[0].geometry.location.k,
            longitude: results[0].geometry.location.D
          };
          resolve(coordinates);
        } else {
          // Query executed successfully, but could not pinpoint one address.
          reject('Unable to identify a single specific point on the map.' +
              ' Try a more specific address.');
        }
      } else {
        reject('Geocode was not successful for the following reason: ' +
            status);
      }
    });
  });
  return promise;
};


function initializeMap() {
  var mapManager = new MapManager();
  mapManager.init();
  var guiHelper = new GuiHelper(mapManager);
  guiHelper.init();
}
google.maps.event.addDomListener(window, 'load', initializeMap);
