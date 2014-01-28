"use strict";

module.exports = function registerRoutes(app) {

    app.all('/redirect/301', function (req, res) {
        res.redirect(301, 'redirect/target');
    });

    app.post('/redirect/303', function (req, res) {
        res.redirect(303, 'redirect/target');
    });

    app.get('/redirect/target', function (req, res) {
        res.send(200, 'redirected to target');
    });

};
