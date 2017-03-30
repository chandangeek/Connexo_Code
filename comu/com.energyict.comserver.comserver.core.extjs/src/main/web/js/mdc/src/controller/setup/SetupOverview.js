/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.SetupOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.comserver.ComServersGrid',
        'setup.comserver.ComServersSetup',
        'setup.comportpool.ComPortPoolsGrid',
        'setup.comportpool.ComPortPoolsSetup',
        'setup.devicecommunicationprotocol.DeviceCommunicationProtocolSetup',
        'setup.licensedprotocol.List',
        'setup.devicetype.DeviceTypesSetup',
        'setup.register.RegisterMappingsSetup',
        'setup.registertype.RegisterTypeSetup',
        'setup.registerconfig.RegisterConfigSetup',
        'setup.deviceconnectionmethod.DeviceConnectionMethodSetup'
    ],

    stores: [
        'Mdc.store.LogLevels',
        'Mdc.store.TimeUnitsWithoutMilliseconds',
        'Mdc.store.DeviceTypePurposes',
        'Mdc.store.ComServers'
    ],

    init: function () {

    },

    showOverview: function () {
        var widget = Ext.widget('setupBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showComServers: function () {
        var me = this,
            widget = Ext.widget('comServersSetup'),
            logLevelsStore = me.getStore('Mdc.store.LogLevels'),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnitsWithoutMilliseconds'),
            comServerStore = me.getStore('Mdc.store.ComServers'),
            counter = 0,
            callback = function() {
                counter += 1;
                if (counter === 3) {
                    me.getApplication().fireEvent('changecontentevent', widget);
                }
            };

        logLevelsStore.load(callback);
        timeUnitsStore.load(callback);
        comServerStore.load(callback);
    },
    showDeviceCommunicationProtocols: function () {
        var widget = Ext.widget('deviceCommunicationProtocolSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showLicensedProtocols: function () {
        var widget = Ext.widget('setupLicensedProtocols');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showComPortPools: function () {
        this.getStore('Mdc.store.ComPortPools').getProxy().extraParams = {};
        this.getApplication().fireEvent('changecontentevent', Ext.widget('comPortPoolsSetup'));
    },
    showRegisterMappings: function () {
        var widget = Ext.widget('registerMappingsSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showDeviceTypes: function () {
        var me = this,
            widget,
            purposeStore = Ext.getStore('Mdc.store.DeviceTypePurposes') || Ext.create('Mdc.store.DeviceTypePurposes'),
            onStoreLoad = function() {
                widget = Ext.widget('deviceTypesSetup', {
                    purposeStore: purposeStore
                });
                me.getApplication().fireEvent('changecontentevent', widget);
            };

        purposeStore.load({
            scope: me,
            callback: onStoreLoad
        });
    },
    showRegisterTypes: function () {
        var widget = Ext.widget('registerTypeSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showRegisterConfigs: function () {
        var widget = Ext.widget('registerConfigSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showLogbookTypes: function () {
        var widget = Ext.widget('logbookTypeSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showDeviceConnectionMethods: function () {
        var widget = Ext.widget('deviceConnectionMethodSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    }

});
