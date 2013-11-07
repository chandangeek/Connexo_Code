Ext.define('Mdc.Application', {
    name: 'Mdc',

    extend: 'Ext.app.Application',

    views: [
        // TODO: add views here
    ],

    controllers: [
        'Main',
        'Setup',
        'DeviceCommunicationProtocol',

        'history.Setup',
        'history.DeviceCommunicationProtocol'
    ],

    stores: [
        // TODO: add stores here
    ]
});
