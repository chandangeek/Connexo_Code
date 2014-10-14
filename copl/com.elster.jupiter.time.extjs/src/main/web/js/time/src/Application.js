Ext.define('Tme.Application', {
    name: 'Tme',

    extend: 'Ext.app.Application',

    requires: [
        'Tme.controller.Main'
    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Tme.controller.Main'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }
});