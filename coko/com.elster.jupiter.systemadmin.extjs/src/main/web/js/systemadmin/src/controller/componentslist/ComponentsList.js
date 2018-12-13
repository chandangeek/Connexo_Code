/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.componentslist.ComponentsList', {
    extend: 'Ext.app.Controller',

    stores: [
        'Sam.store.SystemComponents',
        'Sam.store.AvailableAndLicensedApplications',
        'Sam.store.BundleTypes',
        'Sam.store.ComponentStatuses'
    ],

    views: [
        'Sam.view.componentslist.Overview'
    ],

    showComponentsList: function () {
        var me = this;

        me.getApplication().fireEvent('changecontentevent', Ext.widget('components-overview'), {itemId: 'components-overview-page'});
        me.getStore('Sam.store.SystemComponents').load();
    }
});