/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.Application', {
    name: 'Mdc',

    extend: 'Ext.app.Application',

    requires: [
        'Mdc.PolyReader',
        'Mdc.PolyAssociation',
        'Mdc.Association',
        'Mdc.MdcProxy'
    ],

    views: [
        // TODO: add views here
    ],

    controllers: [
        'Mdc.controller.Main'
//        'setup.SetupOverview',
//        'setup.ComServers',
//        'setup.ComPortPools',
//        'history.Setup',
//        'setup.DeviceCommunicationProtocol',
//        'setup.LicensedProtocol',
//        'setup.DeviceTypes',
//        'setup.RegisterTypes',
//        'setup.RegisterMappings',
//        'setup.DeviceConfigurations'
    ],

    stores: [
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});
