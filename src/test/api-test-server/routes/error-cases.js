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

};
