Ext.define('Dcs.Application', {
    name: 'Dcs',

    extend: 'Ext.app.Application',

    views: [
    ],

    controllers: [
        'Dcs.controller.Main',
        'Dcs.controller.Schedule',
        'Dcs.controller.history.Schedule',
        'Dcs.controller.Administration'
    ],

    stores: [
        'DataCollectionSchedules'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});