


/**
 * @constructor
 */
GeocodeHelper = function() {
  /**
   * @type {!google.maps.Geocoder}
   * @private
   */
  this.geocoder_ = new google.maps.Geocoder();
};


/**
 * @param {!string} address
 * @return {Promise}
 * @see https://developers.google.com/maps/documentation/javascript/geocoding
 */
GeocodeHelper.prototype.geocodeAddress = function(address) {
  var this_ = this;
  var promise = new Promise(function(resolve, reject) {
    this_.geocoder_.geocode({ 'address': address}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        this_.getLatLongFromMapsAPIResults_(results, resolve, reject);
      } else {
        reject('Geocode was not successful for the following reason: ' +
            status);
      }
    });
  });
  return promise;
};


/**
 * @param {Object} results
 * @param {*} resolve
 * @param {*} reject
 * @private
 */
GeocodeHelper.prototype.getLatLongFromMapsAPIResults_ = function(results,
    resolve, reject) {
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
  } else if (typeof results[0].geometry.location == 'object' &&
             Object.keys(results[0].geometry.location).length == 2) {
    // A successful result exists and there are lat + long properties.
    var geocodedLocationObj = results[0].geometry.location;
    var geocodedLocationObjKeys = Object.keys(geocodedLocationObj);

    if (typeof geocodedLocationObj[geocodedLocationObjKeys[0]] == 'number' &&
        typeof geocodedLocationObj[geocodedLocationObjKeys[1]] == 'number') {
      var coordinates = {
        latitude: geocodedLocationObj[geocodedLocationObjKeys[0]],
        longitude: geocodedLocationObj[geocodedLocationObjKeys[1]]
      };
      resolve(coordinates);
    } else if (typeof geocodedLocationObj[geocodedLocationObjKeys[0]] == 'function' &&
               typeof geocodedLocationObj[geocodedLocationObjKeys[1]] == 'function') {
      var coordinates = {
        latitude: geocodedLocationObj[geocodedLocationObjKeys[0]](),
        longitude: geocodedLocationObj[geocodedLocationObjKeys[1]]()
      };
      resolve(coordinates);
    } else {
      // Query executed successfully, but data appears malformed.
      reject('Unable to identify a single specific point on the map.' +
          ' If this persists, please contact the administrator.');
    }
  } else {
    // Query executed successfully, but could not pinpoint one address.
    reject('Unable to identify a single specific point on the map.' +
        ' Try a more specific address.');
  }
};
