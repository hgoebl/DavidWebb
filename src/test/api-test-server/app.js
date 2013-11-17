var express = require('express'),
    http = require('http'),
    app = express(),
    config = require('./app-config.js');

"use strict";

// all environments
app.set('port', config.app.port);
app.use(express.logger('dev'));
app.use(express.json());
app.use(express.urlencoded());
app.use(app.router);

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

http.createServer(app).listen(app.get('port'), function () {
    console.log('Express server listening on port %d', app.get('port'));
});

