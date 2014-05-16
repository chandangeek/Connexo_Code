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

    refs: [
        { ref: 'loginPage', selector: '#loginPage'}
    ],

    init: function(application) {
        this.control({
            "login #loginButton": {
                signin: this.signinuser
            },
            'viewport menuitem[action=logout]': {
                click: this.signout
            }
        });
    },

    signinuser : function(button, username, password)    {
        Ext.Ajax.suspendEvent('requestexception');
        this.hideLoginError();

        var unencodedToken = username + ":" + password;
        var encodedToken = "Basic " + Usr.controller.Base64.encode(unencodedToken);

        var loginMask = new Ext.LoadMask(Ext.getBody(), {msg:"Verifying credentials..."});
        loginMask.show();

        var request = Ext.Ajax.request({
            url: '/apps/usr/index.html',
            method: 'GET',
            headers: {
                'Authorization' : encodedToken
            },
            scope: this,
            success: function(response, opt){
                loginMask.hide();
                this.loginOK(button);
            },
            failure: function(response, opt){
                loginMask.hide();
                this.loginNOK();
            }
        });
    },

    loginOK : function(button){
        window.location.replace("http://localhost:8080/apps/master/login-dev.html");
        /*Ext.Ajax.resumeEvent('requestexception');

        var loader = Ext.create('Uni.Loader');
        loader.initI18n(['USM']);

        Uni.I18n.currencyFormatKey = 'mtr.playground.i18n.currencyformat';
        Uni.I18n.decimalSeparatorKey = 'mtr.playground.i18n.decimalseparator';
        Uni.I18n.thousandsSeparatorKey = 'mtr.playground.i18n.thousandsseparator';

        var me=this;
        loader.onReady(function () {
            this.getLoginPage().destroy();
            me.getApplication().destroy();

            Ext.application({
                name: 'Usr',

                extend: 'Usr.Application',

                autoCreateViewport: true
            });

        });*/
    },

    loginNOK : function(){
        this.showLoginError();
    },

    hideLoginError: function () {
        var widget =  this.getLoginPage().down('#contentPanel'),
            errorLabel = widget.down('#errorLabel');

        widget.setHeight(250);
        widget.down('#errorIcon').setHeight(0);

        errorLabel.setHeight(0);

        widget.down('#errorContainer').hide();
        widget.doLayout();
    },

    showLoginError: function () {
        var widget =  this.getLoginPage().down('#contentPanel'),
            errorLabel = widget.down('#errorLabel');

        widget.setHeight(300);
        widget.down('#errorIcon').setHeight(50);

        errorLabel.setHeight(50);
        errorLabel.setValue('Failed to log in. Please contact your administrator if the problem persists.');

        widget.down('#errorContainer').show();
        widget.doLayout();
    },

    signout: function () {
        var request = Ext.Ajax.request({
            url: '/apps/usr/login.html',
            method: 'GET',
            params: {
                logout: 'true'
            },
            scope: this,
            success: function(){
                window.location.replace('/apps/usr/login.html');
            }
        });

    }
});
