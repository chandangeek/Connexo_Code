Ext.define('InsightApp.Application', {
    extend: 'Ext.app.Application',

    controllers: [
        'InsightApp.controller.Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
