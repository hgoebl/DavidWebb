"use strict";

module.exports = function registerRoutes(app) {

    app.all('/read-timeout', function (req, res) {
        setTimeout(function () {
            res.send(200, "long-running operations result");
        }, 500);
    });

};
