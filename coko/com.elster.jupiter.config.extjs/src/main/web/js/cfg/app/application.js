Ext.define('Cfg.Application', {
    name: 'Cfg',

    extend: 'Ext.app.Application',

    views: [
    ],

    controllers: [
        'Main',
        'EventType',
        'history.EventType'
    ],

    stores: [
        'EventTypes'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});