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
    center: null
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
   * @type {!Array.<google.maps.Marker>}
   * @private
   */
  this.mapMarkers_ = [];

  /**
   * @type {!Array.<google.maps.InfoWindow>}
   * @private
   */
  this.infoWindows_ = [];
};


/**
 * Initialize.
 */
MapManager.prototype.init = function() {
  this.getCurrentUserLocation_()
      .then(this.initializeMap_.bind(this))
      .then(this.loadLocationCoordinates_.bind(this))
      .then(this.placeDataPointsOnMap_.bind(this));
};


/**
 * @param {number} markerNum
 */
MapManager.prototype.handleMapIconClickedFunction = function(markerNum) {
  this.infoWindows_[markerNum].open(this.map_, this.mapMarkers_[markerNum]);
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
      this_.mapOptions_.center = new google.maps.LatLng(38.8951, -77.0367);
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
 * @private
 */
MapManager.prototype.placeDataPointsOnMap_ = function() {
  for (var i = 0; i < this.locationCoordinates_.length; i++) {
    var locationCoordinate = new google.maps.LatLng(
        this.locationCoordinates_[i].latitude,
        this.locationCoordinates_[i].longitude);

    this.mapMarkers_[i] = new google.maps.Marker({
      position: locationCoordinate,
      map: this.map_,
      icon: 'static/img/red_dot.png'
    });

    this.infoWindows_[i] = new google.maps.InfoWindow({
      content: this.locationCoordinates_[i].email
    });

    google.maps.event.addListener(this.mapMarkers_[i], 'click',
        this.handleMapIconClickedFunction.bind(this, i));
  }
};


function initializeMap() {
  var mapManager = new MapManager();
  mapManager.init();
}
google.maps.event.addDomListener(window, 'load', initializeMap);



/**
 * @constructor
 */
GuiHelper = function() {
  /**
   * @type {!google.maps.Geocoder}
   * @private
   */
  this.geocoder_ = new google.maps.Geocoder();
};


/**
 * Initialize the location coordinates helper.
 */
GuiHelper.prototype.init = function() {
  document.getElementById('btn-set-location')
    .addEventListener('click', this.handleSetLocationBtnClicked_.bind(this));
};


/**
 * @param {!String} message
 * @private
 */
GuiHelper.prototype.showFullScreenNotification_ = function(message) {
  alert(message);
};


/**
 * @private
 */
GuiHelper.prototype.handleSetLocationBtnClicked_ = function() {
  var address = document.getElementById('location-address').value;

  var this_ = this;
  this.geocodeAddress_(address)
    .then(this.addLocationCoordinates_.bind(this))
    .then(function(locationCoordinates) {
        // New locationCoordinates successfully added on backend.
        this_.showFullScreenNotification_
          .call(this_, 'Successfully set address');
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
        resolve(locationCoordinates);
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
        if (typeof results[0].geometry.location.lat == 'number' &&
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


document.addEventListener('DOMContentLoaded', function(event) {
  var guiHelper = new GuiHelper();
  guiHelper.init();
});
