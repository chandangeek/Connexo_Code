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
});