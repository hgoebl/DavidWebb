"use strict";

var COMPLEX_UTF8 = 'München 1 Maß 10 €';

module.exports = function registerRoutes(app) {

    app.get('/headers/in', function (req, res) {
        var ok, s;

        ok = (req.header('x-test-string') === COMPLEX_UTF8);
        ok &= req.header('x-test-int') === '4711';

        s = req.param('User-Agent');
        if (s) {
            ok &= req.header('user-agent') === s;
        }

        res.status(ok ? 200 : 403).end();
    });
};
