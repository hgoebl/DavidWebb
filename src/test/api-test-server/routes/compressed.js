"use strict";

var express = require('express'),
    SIMPLE_ASCII = 'Hello/World & Co.?';

module.exports = function registerRoutes(app) {

    app.get('/compressed.json', express.compress(), function (req, res) {
        var i, out = [],
            acceptEncoding = req.header('Accept-Encoding');

        if (!(acceptEncoding &&
            (acceptEncoding.indexOf('gzip') >= 0 ||
                acceptEncoding.indexOf('deflate') >= 0 ||
                acceptEncoding.indexOf('unknown') >= 0))) {

            res.send(400, 'resource must be requested with gzip/deflate accepted as encoding');
            return;
        }

        for (i = 0; i < 500; ++i) {
            out.push(SIMPLE_ASCII);
        }

        if (acceptEncoding.indexOf('unknown') >= 0) {
            // of course this is not supported by express
            res.header('Content-Encoding', 'unknown');
            res.send(200, new Buffer(JSON.stringify(out)));
            return;
        }
        res.send(200, out);
    });

};
