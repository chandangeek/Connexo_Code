/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.onReady(function () {
    var run_loader = function () {
        var loader = Ext.create('Uni.Loader');

        var packages = [
            {
                name: 'Yfn',
                path: '../../apps/yfn/src'
            }
        ];
        loader.initPackages(packages);

        Ext.Ajax.on("beforerequest", function (conn, options) {
            Ext.Ajax.timeout = 90000;
            var xAuthToken = localStorage.getItem('X-AUTH-TOKEN');
            delete conn.defaultHeaders['X-CSRF-TOKEN'];

            if (options.method === 'PUT' || options.method === 'POST' || options.method === 'DELETE') {
                Ext.Ajax.request({
                    url: '../../api/usr/csrf/token',
                    async: false,
                    method: 'GET',
                    success: function (data) {
                        conn.token = data.responseText;
                    }
                });
                if(options.headers &&
                    options.headers['Content-type'] === 'multipart/form-data' && options.url ){

                    options.url = options.url.indexOf('X-CSRF-TOKEN') > 0 ? options.url :
                       options.url + '?X-CSRF-TOKEN=' + conn.token;
                }
                conn.defaultHeaders['X-CSRF-TOKEN'] = unescape(conn.token);
            }
            conn.defaultHeaders.Authorization = xAuthToken != null ? 'Bearer '.concat(xAuthToken.substr(xAuthToken.lastIndexOf(" ") + 1)) : xAuthToken;


        });
        Ext.Ajax.on("requestcomplete", function (conn, response) {
            localStorage.setItem('X-AUTH-TOKEN', response.getResponseHeader('X-AUTH-TOKEN'));

        });

        loader.onReady(function () {
            if (localStorage.getItem('X-AUTH-TOKEN')) {
                Ext.Ajax.defaultHeaders = {
                    'X-CONNEXO-APPLICATION-NAME': 'YFN', // a function that return the main application
                    'Authorization': 'Bearer ' + localStorage.getItem('X-AUTH-TOKEN')
                };
            } else {
                Ext.Ajax.defaultHeaders = {
                    'X-CONNEXO-APPLICATION-NAME': 'YFN'
                };
            }

            Ext.Loader.setConfig({
                // <debug>
                enabled: true,
                // </debug>

                paths: {}
            });

            Ext.application({
                name: 'YellowfinApp',
                extend: 'YellowfinApp.Application',
                autoCreateViewport: true
            });
        });
    };


    Ext.Ajax.request({
        url: '/api/yfn/user/url',
        method: 'GET',
        async: false,
        success: function (response) {
            var data = Ext.JSON.decode(response.responseText);
            window.location = data.url;
        },
        failure: function (response) {
            run_loader();
        }
    });

});

