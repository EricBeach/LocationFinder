// Please ensure locationFinderAppConfig.js is included before this
// file in the HTML.

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
    center: new google.maps.LatLng(defaultMapCenterLat, defaultMapCenterLong)
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
  this.mapOfDisplayedLatitudeToLocationCoordinatesByIndex_ = {};

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
  this.initializeMap_()
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
    var displayName = '';
    var authorizedUser =
        this.authorizedUsers_[this.locationCoordinates_[i].email];
    if (authorizedUser) {
      displayName = authorizedUser.display_name + '<br>';
    }

    if (this.getIndexOfDuplicateLatLong_(i) > 0) {
      var indexOfInfoWindowToUpdate = this.getIndexOfDuplicateLatLong_(i);

      // Append new location info to existing map marker.
      this.infoWindows_[indexOfInfoWindowToUpdate] =
          new google.maps.InfoWindow({
            content: this.infoWindows_[indexOfInfoWindowToUpdate].content +
                '<br>' + displayName + this.locationCoordinates_[i].email
          });
    } else {
      // Add new mapp marker.
      var locationCoordinate = new google.maps.LatLng(
          this.locationCoordinates_[i].latitude,
          this.locationCoordinates_[i].longitude);

      var iconUrl = 'static/img/red_dot.png';
      if (this.locationCoordinates_[i].location_type == 1) {
    	iconUrl = 'static/img/yellow_dot.png';
      }

      this.mapMarkers_[i] = new google.maps.Marker({
        position: locationCoordinate,
        map: this.map_,
        icon: iconUrl
      });

      this.infoWindows_[i] = new google.maps.InfoWindow({
        content: displayName + this.locationCoordinates_[i].email
      });

      google.maps.event.addListener(this.mapMarkers_[i], 'click',
          this.handleMapIconClickedFunction.bind(this, i));

      var coordinateHash = this.getHashOfLatLong_(
          this.locationCoordinates_[i].latitude,
          this.locationCoordinates_[i].longitude);
      this.mapOfDisplayedLatitudeToLocationCoordinatesByIndex_[
          coordinateHash] = i;
    }
  }
};


/**
 * @param {number} i index of a duplicate lat/long coordinate.
 * @return {number} the index of any duplicate latitude/longitude coordinate.
 * @private
 */
MapManager.prototype.getIndexOfDuplicateLatLong_ = function(i) {
  var coordinateHash = this.getHashOfLatLong_(
      this.locationCoordinates_[i].latitude,
      this.locationCoordinates_[i].longitude);
  if (this.mapOfDisplayedLatitudeToLocationCoordinatesByIndex_[
          coordinateHash] > 0) {
    return this.mapOfDisplayedLatitudeToLocationCoordinatesByIndex_[
        coordinateHash];
  } else {
    return -1;
  }
};


/**
 * @param {number} lat Latitude.
 * @param {number} long Longitude.
 * @return {string} Hash of latitude and longitude coordinate.
 * @private
 */
MapManager.prototype.getHashOfLatLong_ = function(lat, long) {
  return lat + ' ## ' + long;
};



/**
 * @param {!MapManager} mapManager
 * @constructor
 */
GuiHelper = function(mapManager) {
  /**
   * @type {GeocodeHelper}
   * @private
   */
  this.geocodeHelper_ = new GeocodeHelper();

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
  this.geocodeHelper_.geocodeAddress(address)
    .then(this.addLocationCoordinates_.bind(this))
    .then(function(locationCoordinatesUpdateInformation) {
        // LocationCoordinates successfully added on backend.
        this_.showFullScreenNotification_
        .call(this_, 'Your change was saved! ' +
            'Map will update upon reloading website.');
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


function initializeMap() {
  var mapManager = new MapManager();
  mapManager.init();
  var guiHelper = new GuiHelper(mapManager);
  guiHelper.init();
}
google.maps.event.addDomListener(window, 'load', initializeMap);
