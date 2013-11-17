"use strict";

module.exports = {
    app: {
        port: process.env.PORT || 3003
    },
    express: {
        'json spaces': 2, // TODO handle settings
        'x-powered-by': false,
        'etag': false
    }
};
