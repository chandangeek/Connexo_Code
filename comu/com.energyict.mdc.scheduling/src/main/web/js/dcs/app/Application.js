Ext.define('Cfg.Application', {
    name: 'Cfg',

    extend: 'Ext.app.Application',

    views: [
    ],

    controllers: [
        'Cfg.controller.Main',
        'Cfg.controller.Schedule',
        'Cfg.controller.history.Schedule',
        'Cfg.controller.Administration'
    ],

    stores: [
        'DataCollectionSchedules'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});