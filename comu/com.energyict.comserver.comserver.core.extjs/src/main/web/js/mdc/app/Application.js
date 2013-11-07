Ext.define('Mdc.Application', {
    name: 'Mdc',

    extend: 'Ext.app.Application',

    views: [
        // TODO: add views here
    ],

    controllers: [
        'Main',
        'Setup',
        'setup.SetupOverview',
        'setup.ComServers',
        'history.Setup',
               
        'DeviceCommunicationProtocol',
        'history.DeviceCommunicationProtocol'
    ],

    stores: [
        'ComServers'
    ]
});
