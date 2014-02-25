Ext.define('Cfg.Application', {
    name: 'Cfg',

    extend: 'Ext.app.Application',

    views: [
    ],

    controllers: [
        'Main',
        'Validation',
        'history.Validation',
        'Administration',
        'history.Administration'
    ],

    stores: [
        'ValidationRuleSets'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});