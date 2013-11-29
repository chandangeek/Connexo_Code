Ext.define('Mdc.Application', {
    name: 'Mdc',

    extend: 'Ext.app.Application',

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
        'setup.LicensedProtocol'
    ],

    stores: [
        'ComServers',
        'LogLevels',
        'DeviceCommunicationProtocols',
        'LicensedProtocols',
    ]
});
