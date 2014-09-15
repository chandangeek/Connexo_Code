Ext.define('SystemApp.Application', {
    extend: 'Ext.app.Application',

    controllers: [
        'SystemApp.controller.Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
