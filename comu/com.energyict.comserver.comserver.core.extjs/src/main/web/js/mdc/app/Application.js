Ext.define('Mdc.Application', {
    name: 'Mdc',

    extend: 'Ext.app.Application',

    views: [
        // TODO: add views here
    ],

    controllers: [
        'Main',
        'setup.SetupOverview',
        'history.Setup',
        'setup.ComServers'
    ],

    stores: [
        'ComServers',
        'LogLevels'
    ]
});
