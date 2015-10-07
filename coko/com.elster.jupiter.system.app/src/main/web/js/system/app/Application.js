Ext.define('SystemApp.Application', {
    extend: 'Ext.app.Application',
    requires:[
        'Usr.privileges.Users',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Apr.privileges.AppServer',
        'Yfn.privileges.Yellowfin',
        'Tme.privileges.Period',
        'Fim.privileges.DataImport'
    ],

    controllers: [
        'SystemApp.controller.Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
