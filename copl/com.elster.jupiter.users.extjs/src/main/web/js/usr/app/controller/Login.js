Ext.require('Uni.Loader');
Ext.require('Ext.Viewport');
Ext.require('Usr.controller.Base64');

Ext.Loader.setConfig({
    enabled: true,
    disableCaching: true, // For debug only.
    paths: {
        'Uni' : '../uni/src'
    }
});

Ext.define('Usr.controller.Login', {
    extend: 'Ext.app.Controller',

    views: [
        'Login'
    ],

    init: function(application) {
        this.control({
            "login #loginButton": {
                signin: this.signinuser
            }
        });
    },

    signinuser : function(button, username, password)    {
        var unencodedToken = username + ":" + password;
        var encodedToken = "Basic " + Usr.controller.Base64.encode(unencodedToken);
        var request = Ext.Ajax.request({
            url: '/apps/usr/index.html',
            method: 'GET',
            headers: {
                'Authorization' : encodedToken
            },
            scope: this,
            success: function(response, opt){
                this.loginOK(button);
            },
            failure: function(response, opt){
                this.loginNOK();
            }
        });
    },

    loginOK : function(button){
        var loader = Ext.create('Uni.Loader');
        loader.initI18n(['USM']);

        Uni.I18n.currencyFormatKey = 'mtr.playground.i18n.currencyformat';
        Uni.I18n.decimalSeparatorKey = 'mtr.playground.i18n.decimalseparator';
        Uni.I18n.thousandsSeparatorKey = 'mtr.playground.i18n.thousandsseparator';

        var me=this;
        loader.onReady(function () {
            Ext.getCmp('usm_elster_login').destroy();
            me.getApplication().destroy();

            Ext.application({
                name: 'Usr',

                extend: 'Usr.Application',

                autoCreateViewport: true
            });

        });
    },

    loginNOK : function(){
        console.log(this);
    }

});
