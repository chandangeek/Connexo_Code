/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.onReady(function () {
    Ext.History.init();

    Ext.application({
        name: 'Login',

        extend: 'Login.Application',

        autoCreateViewport: true
    });

    Ext.Ajax.on("requestcomplete", function(conn, response){

        if(response.getResponseHeader('X-CSRF-TOKEN')){
            Ext.util.Cookies.set('X-CSRF-TOKEN', response.getResponseHeader('X-CSRF-TOKEN'));
        }
    });
});