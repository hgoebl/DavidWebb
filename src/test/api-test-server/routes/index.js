/*
 * routes for REST API
 */

"use strict";

function ping(req, res) {
    res.setHeader('Cache-Control', 'no-cache, must-revalidate');
    res.send('pong');
}

module.exports = function registerRoutes(app) {

    app.get('/ping', ping);

    require('./simple.js')(app);
    require('./headers.js')(app);
};