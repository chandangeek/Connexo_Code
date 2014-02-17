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
        'Main',
        'setup.SetupOverview',
        'setup.ComServers',
        'setup.ComPortPools',
        'history.Setup',
        'setup.DeviceCommunicationProtocol',
        'setup.LicensedProtocol',
        'setup.DeviceTypes',
        'setup.RegisterMappings',
        'setup.DeviceConfigurations'
    ],

    stores: [
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});
