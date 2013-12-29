"use strict";

module.exports = function registerRoutes(app) {

    function rawBody(req, res, next) {
        var data = '';
        req.setEncoding('utf8');
        req.on('data', function(chunk) {
            data += chunk;
        });
        req.on('end', function() {
            req.rawBody = data;
            next();
        });
    }

    app.post('/upload', rawBody, function (req, res) {

        // upload expects 5000 @ chars

        if (/^@{5000}$/.test(req.rawBody)) {
            res.send(201);
        } else {
            res.send(500);
        }

    });

};
