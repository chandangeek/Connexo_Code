/*
    This file is generated and updated by Sencha Cmd. You can edit this file as
    needed for your application, but these edits will have to be merged by
    Sencha Cmd when upgrading.
*/


//Ext.require('Dvi.Application');

Ext.onReady(function () {
    Ext.require('Uni.Loader');
    var loader = Ext.create('Uni.Loader');
    Ext.application( {
        name: 'Usr',
        extend: 'Ext.app.Application',
        controllers: [
            'Usr.controller.Login'
        ],

        launch: function () {
            // Removes the loading indicator.
            Ext.fly('appLoadingWrapper').destroy();

            this.callParent(arguments);
            var login = Ext.create("widget.login");

            // Show window
            login.show();
        }
    });
});


