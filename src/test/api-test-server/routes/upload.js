"use strict";

var zlib = require('zlib');

module.exports = function registerRoutes(app) {

    // not a good example for general use - implies fixed UTF-8 encoding and ignoring real encoding!
    function rawTextBody(req, res, next) {
        var data = '';
        req.setEncoding('utf8');
        req.on('data', function (chunk) {
            data += chunk;
        });
        req.on('end', function () {
            req.rawTextBody = data;
            next();
        });
        req.on('error', function (err) {
            console.log(err);
            res.status(500);
        })
    }

    app.post('/upload', rawTextBody, function (req, res) {

        // upload expects 5000 @ chars

        if (/^@{5000}$/.test(req.rawTextBody)) {
            res.send(201);
        } else {
            res.send(500);
        }

    });

    function rawBody(req, res, next) {
        var chunks = [];

        req.on('data', function(chunk) {
            chunks.push(chunk);
        });

        req.on('end', function() {
            var buffer = Buffer.concat(chunks);
            var encoding = req.header('content-encoding');

            req.bodyLength = buffer.length;

            if (encoding && encoding.toLowerCase() === 'gzip') {
                zlib.gunzip(buffer, function(err, decoded) {
                    if (err) {
                        res.send(406);
                        return;
                    }
                    req.rawBody = decoded;
                    next();
                });
            } else {
                req.rawBody = buffer;
                next();
            }
        });

        req.on('error', function (err) {
            console.log(err);
            res.status(500);
        })
    }


    app.post('/upload-compressed', rawBody, function (req, res) {

        // upload expects 5000 @ chars

        if (/^@{5000}$/.test(req.rawBody.toString()) && req.bodyLength < 1000) {
            res.send(201);
        } else {
            res.send(500);
        }

    });

};
