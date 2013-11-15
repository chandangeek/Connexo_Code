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
        'setup.ComPorts',
        'history.Setup',
        'setup.DeviceCommunicationProtocol'
    ],

    stores: [
        'ComServers',
        'LogLevels',
        'DeviceCommunicationProtocols'
    ]
});
