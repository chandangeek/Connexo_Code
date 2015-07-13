Ext.define('InsightApp.Application', {
    extend: 'Ext.app.Application',

    controllers: [
        'InsightApp.controller.Main'
    ],
    models: [
             'InsightApp.model.UsagePointComplete'
         ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
