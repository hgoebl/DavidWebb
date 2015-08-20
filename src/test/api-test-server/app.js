var express = require('express'),
    http = require('http'),
    app = express(),
    config = require('./app-config.js');

"use strict";

// all environments
app.use(express.logger('dev'));

if (config.logHeaders) {
    app.use(function (req, res, next) {
        if (config.logHeaders.in) {
            console.log(req.headers);
        }
        if (config.logHeaders.out) {
            res.once('finish', function () {
                console.log(res._header);
            });
        }
        next();
    });
}
app.use(express.json());
app.use(express.urlencoded());
app.use(app.router);
app.use(express.static(__dirname + '/static'));
// app.use(express.compress());
// app.use(express.static(__dirname + '/static-compressed'));

// set options like 'x-powered-by', ...
Object.keys(config.express).forEach(function (option) {
    app.set(option, config.express[option]);
});

// development only
if ('development' === app.get('env')) {
    app.use(express.errorHandler({
            dumpExceptions: true,
            showStack: true
        }));
}

require('./routes')(app);

http.createServer(app).listen(config.app.port, function () {
    console.log('Express server listening on port %d', config.app.port);
});

// ---- https server ----
// see http://blog.matoski.com/articles/node-express-generate-ssl/

var https = require('https'),
    fs = require('fs');

https.createServer({
    key: fs.readFileSync('./ssl/server.key'),
    cert: fs.readFileSync('./ssl/server.crt'),
    ca: fs.readFileSync('./ssl/ca.crt'),
    requestCert: true,
    rejectUnauthorized: false
}, app).listen(config.app.sslPort, function () {
    console.log('Secure Express server on port %d', config.app.sslPort);
});
