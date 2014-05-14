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
        'Usr.view.Login'
    ],

    init: function(application) {
        this.control({
            "login #loginButton": {
                signin: this.signinuser
            }
        });
    },

    showOverview: function() {
        var widget = Ext.widget("login");
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    signinuser : function(button, username, password)    {
        Ext.Ajax.suspendEvent('requestexception');
        this.hideLoginError();

        var unencodedToken = username + ":" + password;
        var encodedToken = "Basic " + Usr.controller.Base64.encode(unencodedToken);

        //var widget =  Ext.getCmp('usm_elster_login').down('#contentPanel');
        //widget.setLoading(true);
        var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"Verifying credentials..."});
        myMask.show();

        var request = Ext.Ajax.request({
            url: '/apps/usr/index.html',
            method: 'GET',
            headers: {
                'Authorization' : encodedToken
            },
            scope: this,
            success: function(response, opt){
                //widget.setLoading(false);
                myMask.hide();
                this.loginOK(button);
            },
            failure: function(response, opt){
                //widget.setLoading(false);
                myMask.hide();
                this.loginNOK();
            }
        });
    },

    loginOK : function(button){
        Ext.Ajax.resumeEvent('requestexception');

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
        this.showLoginError();
    },

    hideLoginError: function () {
        var widget =  Ext.getCmp('usm_elster_login').down('#contentPanel'),
            errorLabel = widget.down('#errorLabel');

        widget.setHeight(250);
        widget.down('#errorIcon').setHeight(0);

        errorLabel.setHeight(0);

        widget.down('#errorContainer').hide();
        widget.doLayout();
    },

    showLoginError: function () {
        var widget =  Ext.getCmp('usm_elster_login').down('#contentPanel'),
            errorLabel = widget.down('#errorLabel');

        widget.setHeight(300);
        widget.down('#errorIcon').setHeight(50);

        errorLabel.setHeight(50);
        errorLabel.setValue('Failed to log in. Please contact your administrator if the problem persists.');

        widget.down('#errorContainer').show();
        widget.doLayout();
    }

});
