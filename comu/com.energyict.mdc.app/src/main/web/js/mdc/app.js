/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');

    // <debug>
    var packages = [
        {
            name: 'Sam',
            path: '../../apps/sam/src'
        },
        {
            name: 'Cfg',
            path: '../../apps/cfg/src'
        },
        {
            name: 'Tme',
            path: '../../apps/tme/src'
        },
        {
            name: 'Mdc',
            path: '../../apps/mdc/src'
        },
        {
            name: 'Isu',
            path: '../../apps/isu/src'
        },
        {
            name: 'Idc',
            path: '../../apps/idc/src'
        },
        {
            name: 'Idv',
            path: '../../apps/idv/src'
        },
        {
            name: 'Idl',
            path: '../../apps/idl/src'
        },
        {
            name: 'Dal',
            path: '../../apps/dal/src'
        },
        {
            name: 'Ddv',
            path: '../../apps/ddv/src'
        },
        {
            name: 'Dsh',
            path: '../../apps/dsh/src'
        },
        {
            name: 'Yfn',
            path: '../../apps/yfn/src'
        },
        {
            name: 'Fwc',
            path: '../../apps/fwc/src'
        },
        {
            name: 'Dlc',
            path: '../../apps/dlc/src'
        },
        {
            name: 'Dxp',
            path: '../../apps/dxp/src'
        },
        {
            name: 'Est',
            path: '../../apps/est/src'
        },
        {
            name: 'Fim',
            path: '../../apps/fim/src'
        },
        {
            name: 'Bpm',
            path: '../../apps/bpm/src'
        },
        {
            name: 'Dbp',
            path: '../../apps/dbp/src'
        },
        {
            name: 'Scs',
            path: '../../apps/scs/src'
        },
        {
            name: 'Apr',
            path: '../../apps/apr/src'
        },
        {
            name: 'Tou',
            path: '../../apps/tou/src'
        },
        {
            name: 'Itk',
            path: '../../apps/itk/src'
        },
        {
            name: 'Isc',
            path: '../../apps/isc/src'
        },
        {
            name: 'Wss',
            path: '../../apps/wss/src'
        },
        {
            name: 'Iws',
            path: '../../apps/iws/src'
        }
    ];

    loader.initPackages(packages);
    // </debug>
    Ext.Ajax.on("beforerequest", function (conn, options) {
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
        var xAuthToken = localStorage.getItem('X-AUTH-TOKEN');
        conn.defaultHeaders.Authorization = xAuthToken != null ? 'Bearer '.concat(xAuthToken.substr(xAuthToken.lastIndexOf(" ") + 1)) : xAuthToken;
    });

    Ext.Ajax.on("requestcomplete", function (conn, response) {
        if (response.request && JSON.stringify(response.request.headers).match('"X-Requested-With":"XMLHttpRequest"'))
            localStorage.setItem('X-AUTH-TOKEN', response.getResponseHeader('X-AUTH-TOKEN'));

    });

    loader.onReady(function () {
        var dependenciesCounter = 2,
            onDependenciesLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    Ext.application({
                        name: 'MdcApp',
                        extend: 'MdcApp.Application',
                        autoCreateViewport: true
                    });
                }
            };

        if (localStorage.getItem('X-AUTH-TOKEN')) {
            Ext.Ajax.defaultHeaders = {
                'X-CONNEXO-APPLICATION-NAME': 'MDC', // a function that return the main application
                'Authorization': 'Bearer ' + localStorage.getItem('X-AUTH-TOKEN')
            };
        } else {
            Ext.Ajax.defaultHeaders = {
                'X-CONNEXO-APPLICATION-NAME': 'MDC'
            };
        }

        Ext.Loader.setConfig({
            // <debug>
            enabled: true,
            // </debug>

            paths: {
                'Ext.ux.form': '../uni/packages/Ext.ux.form',
                'Ext.ux.Rixo': '../uni/packages/Ext.ux.Rixo',
                'Ext.ux.window': '../uni/packages/Ext.ux.window.Notification'
            }
        });

        Uni.store.Apps.load(onDependenciesLoad);
        Uni.util.CheckAppStatus.checkInsightAppStatus(onDependenciesLoad);
        Ext.Ajax.request({
            url: '/api/sys/fields/timeout',
            method: 'GET',

            success: function (response) {
                Ext.Ajax.timeout = parseInt(response.responseText);
            }
        });
    });
});


