Ext.define('Mdc.Application', {
    name: 'Mdc',
    requires: [
        'Mdc.PolyReader',
        'Mdc.PolyAssociation',
        'Mdc.Association'
    ],
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
        'setup.DeviceCommunicationProtocol'
    ],

    stores: [
        'ComServers',
        'LogLevels',
        'ComPortPools',
        'DeviceCommunicationProtocols'
    ]
});
