Ext.define('Cfg.Application', {
    name: 'Cfg',

    extend: 'Ext.app.Application',

    views: [
    ],

    controllers: [
        'Main',
        'EventType',
        'Validation',
        'history.EventType',
        'history.Validation'
    ],

    stores: [
        'EventTypes',
        'ValidationRuleSets'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});