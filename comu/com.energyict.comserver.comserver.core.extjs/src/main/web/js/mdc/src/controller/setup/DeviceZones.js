/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceZones', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.view.setup.devicezones.ZonesSetup',
        'Mdc.view.setup.devicezones.ZonesGrid',
        'Mdc.view.setup.devicezones.ZonesPreview',
        'Mdc.model.Device'

    ],

    views: [
        'Mdc.view.setup.devicezones.ZonesSetup',
        'Mdc.view.setup.devicezones.ZonesGrid'
    ],

    stores: [
        'Mdc.store.DeviceZones'
    ],

    refs: [
        {ref: 'deviceZonesGrid', selector: '#device-zones-grid'},
        {ref: 'deviceZonesSetup', selector: '#device-zones-setup'},
        {ref: 'deviceZonesPreview', selector: "#device-zones-preview"}
    ],

    init: function () {
        this.control({
                '#device-zones-grid': {
                    select: this.showDeviceZonePreview
                },

            }
        );
    },

    showDeviceZoneView: function (deviceId) {
        var me = this;
            viewport = Ext.ComponentQuery.query('viewport')[0],
            zonesStore = me.getStore('Mdc.store.DeviceZones');

        zonesStore.getProxy().setUrl(deviceId);
        viewport.setLoading();


        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                zonesStore.load({
                    callback: function (records) {

                        me.getStore('Mdc.store.DeviceZones').getProxy().setExtraParam('deviceId', device.get('name'));

                        var widget = Ext.widget('device-zones-setup', {device: device});

                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        viewport.setLoading(false);

                        //me.getDeviceZonesGrid().getSelectionModel().doSelect(0);
                    }
                })

            }
        });
    },

    showDeviceZonePreview: function(records,record) {
        Ext.suspendLayouts();
        this.getDeviceZonesPreview().setTitle(Ext.String.htmlEncode(record.get('zoneName')));
        this.getDeviceZonesPreview().down('form').loadRecord(record);
        Ext.resumeLayouts();
    }

});