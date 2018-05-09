/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.about.About', {
    extend: 'Ext.app.Controller',

    stores: [
        'Sam.store.VersionInfo',
        'Sam.store.AvailableAndLicensedApplications'
    ],

    views: [
        'Sam.view.about.About'
    ],

    showAbout: function () {
        var me = this;

        me.getStore('Sam.store.VersionInfo').load();
        me.getStore('Sam.store.AvailableAndLicensedApplications').load();

        me.getApplication().fireEvent('changecontentevent', Ext.widget('about-version-info'), {itemId: 'about-version-info-page'});
        me.getApplication().fireEvent('changecontentevent', Ext.widget('about-info'), {itemId: 'about-info-page'});
    }
});