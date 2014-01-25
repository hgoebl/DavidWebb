"use strict";

var SIMPLE_ASCII = 'Hello/World & Co.?';

module.exports = function registerRoutes(app) {

    app.get('/headers/in', function (req, res) {
        var ok, s,
            date = Date.UTC(2013, 10, 24, 23, 59, 33),
            calHeader = new Date(req.header('x-test-calendar')),
            dateHeader = new Date(req.header('x-test-date'));

        ok = (req.header('x-test-string') === SIMPLE_ASCII);
        ok &= req.header('x-test-int') === '4711';
        ok &= calHeader.getTime() === date;
        ok &= dateHeader.getTime() === date;

        s = req.param('User-Agent');
        if (s) {
            ok &= req.header('user-agent') === s;
        }

        res.send(ok ? 200 : 403);
    });

    app.get('/headers/out', function (req, res) {

        res.header('x-test-string', SIMPLE_ASCII); // express/connect is not able to set € symbol
        res.header('x-test-int', 4711);
        res.header('x-test-datum', new Date().toUTCString());

        res.send(200);
    });

    app.get('/headers/expires', function (req, res) {
        var offset = parseInt(req.param('offset') || '3600000', 10);

        res.header('Expires', new Date(Date.now() + offset).toUTCString());
        res.send(200);
    });

    app.get('/headers/if-modified-since', function (req, res) {
        var lastModified,
            ifModifiedSince;

        lastModified = parseInt(req.param('lastModified'), 10);
        ifModifiedSince = new Date(req.header('if-modified-since')).getTime();

        if (lastModified > ifModifiedSince) {
            res.send(200, 'new content comes here!');
        } else {
            res.send(304);
        }
    });

    app.get('/headers/last-modified', function (req, res) {
        var lastModified = parseInt(req.param('lastModified'), 10);

        res.header('Last-Modified', new Date(lastModified).toUTCString());
        res.send(200);
    });

};
