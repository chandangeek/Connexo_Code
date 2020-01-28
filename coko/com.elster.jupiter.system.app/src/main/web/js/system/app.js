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

    Ext.Ajax.on("beforerequest", function(conn, options){
        var xAuthToken = localStorage.getItem('X-AUTH-TOKEN');
        conn.defaultHeaders.Authorization =  xAuthToken != null ? 'Bearer '.concat(xAuthToken.substr(xAuthToken.lastIndexOf(" ")+1)) : xAuthToken;

        if (!options.headers) {
            options.headers = {};
        }
        if (options.method === 'PUT' || options.method === 'POST' || options.method === 'DELETE') {
            options.headers['X-CSRF-TOKEN'] = Ext.util.Cookies.get('X-CSRF-TOKEN') || "";

        }

    });
    Ext.Ajax.on("requestcomplete", function(conn, response){
        if(response.request && JSON.stringify(response.request.headers).match('"X-Requested-With":"XMLHttpRequest"'))
            localStorage.setItem('X-AUTH-TOKEN', response.getResponseHeader('X-AUTH-TOKEN'));

        if(response.getResponseHeader('X-CSRF-TOKEN')){
            Ext.util.Cookies.set('X-CSRF-TOKEN', response.getResponseHeader('X-CSRF-TOKEN'));
        }
    });

    loader.onReady(function () {

        var onDependenciesLoad = function () {
            Ext.application({
                name: 'SystemApp',
                extend: 'SystemApp.Application',
                autoCreateViewport: true
            });
        };

        if(localStorage.getItem('X-AUTH-TOKEN')){
            Ext.Ajax.defaultHeaders = {
                'X-CONNEXO-APPLICATION-NAME': 'SYS', // a function that return the main application
                'Authorization': 'Bearer ' + localStorage.getItem('X-AUTH-TOKEN')
            };
        }else{
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
    });
});

