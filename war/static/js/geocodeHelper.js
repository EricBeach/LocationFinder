


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
        } else if (typeof results[0].geometry.location.A == 'number' &&
            typeof results[0].geometry.location.F == 'number') {
          var coordinates = {
            latitude: results[0].geometry.location.A,
            longitude: results[0].geometry.location.F
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
