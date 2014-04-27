"use strict";

module.exports = function registerRoutes(app) {

    app.all('/error/500/no-content', function (req, res) {
        res.send(500);
    });

    app.all('/error/500/with-content', function (req, res) {
        res.send(500, {msg: 'an error has occurred'});
    });

    app.get('/error/400/no-content', function (req, res) {
        res.send(400);
    });

    app.get('/error/400/with-content', function (req, res) {
        res.send(400, {msg: 'an error has occurred'});
    });

    var counters = {};

    function cleanupCounters() {
        var tooOld = Date.now() - (60 * 60 * 1000);
        Object.keys(counters).forEach(function (key) {
            if (Number(key) < tooOld) {
                delete counters[key];
            }
        });
    }

    app.get('/error/503/:requestTimestamp/:retryCount', function (req, res) {

        var requestTimestamp = req.param('requestTimestamp'),
            retryCount = Number(req.param('retryCount')),
            currentRetry;

        cleanupCounters(); // avoid memory leak
        currentRetry = counters[requestTimestamp] || 0;
        counters[requestTimestamp] = currentRetry + 1;

        if (currentRetry < retryCount) {
            console.log('503 -> ' + currentRetry + ' of ' + retryCount);
            res.send(503);
        } else {
            res.send(200, 'Now it works');
        }
    });
};
