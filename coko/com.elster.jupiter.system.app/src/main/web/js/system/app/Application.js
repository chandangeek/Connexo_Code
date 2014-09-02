Ext.define('SystemApp.Application', {
    name: 'SystemApp',

    extend: 'Ext.app.Application',

    controllers: [
        'Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
