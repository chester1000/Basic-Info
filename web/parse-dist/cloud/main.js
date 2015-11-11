// Generated by IcedCoffeeScript 108.0.9
var app, checkVanity, crypto, express, getHash, getRecord, resError;

express = require('express');

crypto = require('crypto');

app = express();

app.use(express.bodyParser());

getHash = function(string) {
  return crypto.createHash('sha256').update('' + string).digest('hex').substring(0, 8);
};

checkVanity = function(vanity, cb) {
  var hashMatches, vanityLower, vanityMatches;
  vanityLower = vanity.toLowerCase();
  hashMatches = new Parse.Query('Latest');
  hashMatches.equalTo('hash', vanityLower);
  vanityMatches = new Parse.Query('Latest');
  vanityMatches.equalTo('vanityLowerCase', vanityLower);
  return Parse.Query.or(hashMatches, vanityMatches).first({
    success: function(o) {
      return cb(null, o);
    },
    error: function(error) {
      return cb(error);
    }
  });
};

getRecord = function(key, cb) {
  var q;
  q = new Parse.Query('Latest');
  q.equalTo('hash', getHash(key));
  return q.find({
    success: function(records) {
      switch (records.length) {
        case 0:
          return cb.create();
        case 1:
          return cb.update(records[0]);
        default:
          return cb.error('too many records');
      }
    },
    error: cb.error
  });
};

app.get('/:id', function(req, res) {
  var id, _ref;
  id = (_ref = req.params.id) != null ? _ref : '';
  if (id === 'favicon.ico') {
    res.end('Really?');
    return;
  }
  return checkVanity(id, function(err, o) {
    if (err) {
      res.jsonp({
        error: err
      });
      return;
    }
    if (!o) {
      res.jsonp(200, {});
      return;
    }
    return res.jsonp(200, {
      vanity: o != null ? o.get('vanity') : void 0,
      hash: o != null ? o.get('hash') : void 0,
      phone: o != null ? o.get('phone') : void 0,
      location: o != null ? o.get('location') : void 0
    });
  });
});

resError = function(res) {
  return function(error) {
    return res.json(500, {
      error: error
    });
  };
};

app.post('/update', function(req, res) {
  var errorer, key;
  errorer = resError(res);
  key = req.body.key;
  if (!key) {
    return errorer(['"key" missing']);
  }
  return getRecord(key, {
    create: function() {
      var Latest, newLocation, newPhone, newVanity, record, saveable;
      Latest = Parse.Object.extend('Latest');
      record = new Latest();
      saveable = {
        hash: getHash(key)
      };
      newPhone = req.body['phone'];
      if (newPhone) {
        saveable.phone = newPhone;
      }
      newLocation = req.body['location'];
      if (newLocation) {
        saveable.location = newLocation;
      }
      newVanity = req.body['vanity'];
      if (newVanity) {
        return checkVanity(newVanity, function(err, o) {
          if (!o) {
            saveable.vanity = newVanity;
            return record.save(saveable, {
              success: function() {
                return res.json(200, {
                  action: 'created'
                });
              },
              error: errorer
            });
          } else {
            return res.json(500, {
              error: 'vanity taken'
            });
          }
        });
      } else {
        return record.save(saveable, {
          success: function() {
            return res.json(200, {
              action: 'created'
            });
          },
          error: errorer
        });
      }
    },
    update: function(record) {
      var change, changes, newLocation, newPhone, newVanity, oldLocation, oldPhone, oldVanity, _i, _len;
      changes = [];
      newPhone = req.body['phone'];
      if (newPhone) {
        oldPhone = record.get('phone');
        if (oldPhone !== newPhone) {
          changes.push({
            name: 'phone',
            oldValue: oldPhone,
            newValue: newPhone
          });
        }
      }
      newLocation = req.body['location'];
      if (newLocation) {
        oldLocation = record.get('location');
        if (oldLocation.city !== newLocation.city || oldLocation.country !== newLocation.country) {
          changes.push({
            name: 'location',
            oldValue: oldLocation,
            newValue: newLocation
          });
        }
      }
      newVanity = req.body['vanity'];
      if (newVanity) {
        oldVanity = record.get('vanity');
        if (oldVanity !== newVanity) {
          return checkVanity(newVanity, function(err, o) {
            var change, _i, _len;
            if (!o) {
              changes.push({
                name: 'vanity',
                oldValue: oldVanity,
                newValue: newVanity
              });
              changes.push({
                name: 'vanityLowerCase',
                oldValue: oldVanity,
                newValue: newVanity.toLowerCase()
              });
              if (!changes.length) {
                res.json(200, {
                  action: 'not updated'
                });
                return;
              }
              for (_i = 0, _len = changes.length; _i < _len; _i++) {
                change = changes[_i];
                record.set(change.name, change.newValue);
              }
              return record.save(null, {
                success: function() {
                  return res.json(200, {
                    action: 'updated'
                  });
                },
                error: errorer
              });
            } else {
              return res.json(500, {
                error: 'vanity taken'
              });
            }
          });
        }
      } else {
        if (!changes.length) {
          res.json(200, {
            action: 'not updated'
          });
          return;
        }
        for (_i = 0, _len = changes.length; _i < _len; _i++) {
          change = changes[_i];
          record.set(change.name, change.newValue);
        }
        return record.save(null, {
          success: function() {
            return res.json(200, {
              action: 'updated'
            });
          },
          error: errorer
        });
      }
    },
    error: errorer
  });
});

app.listen();