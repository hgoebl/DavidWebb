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
    require('./error-cases.js')(app);
    require('./timeouts.js')(app);
    require('./headers.js')(app);
    require('./redirect.js')(app);
    require('./upload.js')(app);
    require('./compressed.js')(app);
};