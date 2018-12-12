/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class YelowfinApp.controller.Main
 */
Ext.define('YellowfinApp.controller.Main', {
    extend: 'Uni.controller.AppController',
    applicationTitle: 'Connexo Facts',
    privileges: [],
    defaultToken: "/error/launch",

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(2, this.defaultToken.length),
            route: ''
        });
        this.callParent(arguments);
    }
});
