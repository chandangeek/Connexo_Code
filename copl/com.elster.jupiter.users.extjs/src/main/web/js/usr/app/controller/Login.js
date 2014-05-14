Ext.define('Usr.controller.Login', {
    extend: 'Ext.app.Controller',

    requires: [
        'Usr.controller.Base64'
    ],

    views: [
        'Usr.view.Login'
    ],

    refs: [
        {
            ref: 'loginViewport',
            selector: 'login'
        },
        {
            ref: 'loginForm',
            selector: 'login #login-form'
        }
    ],

    init: function(application) {
        this.control({
            "login #login-form [action=login]": {
                click: this.signinuser
            }
        });
    },

    showOverview: function() {
        var widget = Ext.widget("login");
    },

    signinuser : function()    {
        var me = this,
            form = this.getLoginForm(),
            data = form.getValues();
//        Ext.Ajax.suspendEvent('requestexception');
        this.hideLoginError();

        var unencodedToken = data.username + ":" + data.password;
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
                me.loginOK();
            },
            failure: function(response, opt){
                me.loginNOK();
            },
            callback: function() {
                myMask.hide();
            }
        });
    },

    loginOK : function(){
//        Ext.Ajax.resumeEvent('requestexception');

//        var loader = Ext.create('Uni.Loader');
//        loader.initI18n(['USM']);
//
//        Uni.I18n.currencyFormatKey = 'mtr.playground.i18n.currencyformat';
//        Uni.I18n.decimalSeparatorKey = 'mtr.playground.i18n.decimalseparator';
//        Uni.I18n.thousandsSeparatorKey = 'mtr.playground.i18n.thousandsseparator';
//
//        var me=this;
//        loader.onReady(function () {
//            Ext.getCmp('usm_elster_login').destroy();
//            me.getApplication().destroy();
//
//            Ext.application({
//                name: 'Usr',
//
//                extend: 'Usr.Application',
//
//                autoCreateViewport: true
//            });
//
//        });
        this.getLoginViewport().destroy();
    },

    loginNOK : function(){
        this.showLoginError();
    },

    hideLoginError: function () {
        var errorLabel =  this.getLoginForm().down('#errorLabel');
        errorLabel.hide();
    },

    showLoginError: function () {
        var errorLabel =  this.getLoginForm().down('#errorLabel');
        errorLabel.setValue('Failed to log in. Please contact your administrator if the problem persists.');
        errorLabel.show();
    }
});
