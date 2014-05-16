
Ext.onReady(function () {
    //Ext.require('Uni.Loader');
    //var loader = Ext.create('Uni.Loader');
    Ext.application( {
        name: 'Usr',
        extend: 'Ext.app.Application',
        controllers: [
            'Usr.controller.Login'
        ],

        launch: function () {
            this.callParent(arguments);
            var login = Ext.create("widget.login");

            login.show();
        }
    });
});


