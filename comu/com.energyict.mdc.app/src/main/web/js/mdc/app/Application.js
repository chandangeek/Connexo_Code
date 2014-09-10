Ext.define('MdcApp.Application', {
    extend: 'Ext.app.Application',

    controllers: [
        'MdcApp.controller.Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
