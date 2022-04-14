/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');

    // <debug>
    // Used only during development to point to hosted files.
    var packages = [
        {
            name: 'Usr',
            path: '../../apps/usr/src'
        },
        {
            name: 'Sam',
            path: '../../apps/sam/src'
        },
        {
            name: 'Tme',
            path: '../../apps/tme/src'
        },
        {
            name: 'Yfn',
            path: '../../apps/yfn/src'
        },
        {
            name: 'Apr',
            path: '../../apps/apr/src'
        },
        {
            name: 'Fim',
            path: '../../apps/fim/src'
        },
        {
            name: 'Cps',
            path: '../../apps/cps/src'
        },
        {
            name: 'Mtr',
            path: '../../apps/mtr/src'
        },
        {
            name: 'Sct',
            path: '../../apps/sct/src'
        },
        {
            name: 'Bpm',
            path: '../../apps/bpm/src'
        },
        {
            name: 'Cal',
            path: '../../apps/cal/src'
        },
        {
            name: 'Wss',
            path: '../../apps/wss/src'
        },
        {
            name: 'Pkj',
            path: '../../apps/pkj/src'
        }
    ];

    loader.initPackages(packages);
    // </debug>

    Ext.Ajax.on("beforerequest", function (conn, options) {
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
            if (options.headers &&
                options.headers['Content-type'] === 'multipart/form-data' && options.url) {

                options.url = options.url.indexOf('X-CSRF-TOKEN') > 0 ? options.url :
                    options.url + '?X-CSRF-TOKEN=' + conn.token;
            }
            conn.defaultHeaders['X-CSRF-TOKEN'] = unescape(conn.token);
        }
        conn.defaultHeaders.Authorization = xAuthToken != null ? 'Bearer '.concat(xAuthToken.substr(xAuthToken.lastIndexOf(" ") + 1)) : xAuthToken;
    });
    Ext.Ajax.on("requestcomplete", function (conn, response) {
        if (response.request && JSON.stringify(response.request.headers).match('"X-Requested-With":"XMLHttpRequest"'))
            localStorage.setItem('X-AUTH-TOKEN', response.getResponseHeader('X-AUTH-TOKEN'));
    });

    loader.onReady(function () {

        var onDependenciesLoad = function () {
            Ext.application({
                name: 'SystemApp',
                extend: 'SystemApp.Application',
                autoCreateViewport: true
            });
        };

        if (localStorage.getItem('X-AUTH-TOKEN')) {
            Ext.Ajax.defaultHeaders = {
                'X-CONNEXO-APPLICATION-NAME': 'SYS', // a function that return the main application
                'Authorization': 'Bearer ' + localStorage.getItem('X-AUTH-TOKEN')
            };
        } else {
            Ext.Ajax.defaultHeaders = {
                'X-CONNEXO-APPLICATION-NAME': 'SYS'
            };
        }
        // <debug>
        Ext.Loader.setConfig({
            enabled: true
        });
        // </debug>
        Uni.store.Apps.load(onDependenciesLoad);
        Ext.Ajax.request({
            url: '/api/sys/fields/timeout',
            method: 'GET',

            success: function (response) {
                Ext.Ajax.timeout = parseInt(response.responseText);
            }
        });
    });
});

