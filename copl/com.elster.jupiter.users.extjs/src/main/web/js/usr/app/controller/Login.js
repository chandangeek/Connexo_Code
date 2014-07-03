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

    init: function (application) {
        this.getApplication().on('sessionexpired', this.sessionExpired);

        this.control({
            'login #login-form [action=login]': {
                click: this.signinuser
            },
            'viewport menuitem[action=logout]': {
                click: this.signout
            },
            'login #login-form #password': {
                specialkey: this.onPasswordKey
            },
            'login #login-form #username': {
                specialkey: this.onUsernameKey
            }
        });
    },

    showOverview: function (error) {
        Ext.widget("login");

        var params = Ext.urlDecode(location.search.substring(1));
        if (params.expired != undefined) {
            this.getLoginForm().down('#errorLabel').setValue("Session expired.");
            this.showLoginError();
        }
    },

    sessionExpired: function () {
        window.location = '/apps/usr/login.html?expired';
        //window.location = '/apps/usr/login.html?expired' + window.location.pathname + window.location.hash;
    },

    onPasswordKey: function (field, event, options) {
        if (event.getKey() == event.ENTER) {
            this.signinuser();
        }
    },

    onUsernameKey: function (field, event, options) {
        if (event.getKey() == event.ENTER) {
            field.nextSibling().focus(false, 200);
        }
    },

    signinuser: function () {
        var me = this,
            form = this.getLoginForm(),
            data = form.getValues();

        this.hideLoginError();

        var unencodedToken = data.username + ":" + data.password;
        var encodedToken = "Basic " + Usr.controller.Base64.encode(unencodedToken);

        var loginMask = new Ext.LoadMask(Ext.getBody(), {msg: "Verifying credentials..."});
        loginMask.show();

        var request = Ext.Ajax.request({
            url: '/apps/usr/login.html',
            method: 'GET',
            headers: {
                'Authorization': encodedToken
            },
            scope: this,
            success: function (response, opt) {
                me.loginOK();
            },
            failure: function (response, opt) {
                me.loginNOK();
            },
            callback: function () {
                loginMask.hide();
            }
        });
    },

    loginOK: function () {
        var params = Ext.urlDecode(location.search.substring(1));
        if (params.page) {
            if(location.hash == ""){
                location.hash = "#/workspace"
            }
            window.location.replace(params.page + location.hash);
        }
        else {
            window.location.replace("/apps/master/index.html#/workspace");
        }
        this.getLoginViewport().destroy();
    },

    loginNOK: function () {
        this.getLoginForm().down('#errorLabel').setValue("Login failed. Please contact your administrator.");
        this.showLoginError();
    },

    hideLoginError: function () {
        var errorLabel = this.getLoginForm().down('#errorLabel');
        errorLabel.hide();
    },

    showLoginError: function () {
        var errorLabel = this.getLoginForm().down('#errorLabel');
        errorLabel.show();
    },

    signout: function () {
        var request = Ext.Ajax.request({
            url: '/apps/usr/login.html',
            method: 'GET',
            params: {
                logout: 'true'
            },
            scope: this,
            success: function () {
                window.location.replace('/apps/usr/login.html');
            }
        });
    }
});
