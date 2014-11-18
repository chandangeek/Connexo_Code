Ext.define('Login.controller.Login', {
    extend: 'Ext.app.Controller',

    requires: [
        'Login.controller.Base64',
        'Uni.store.Apps',
        'Ext.History'
    ],

    views: [
        'Login.view.Viewport'
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
        var params = Ext.urlDecode(location.search.substring(1));
        if (typeof params.expired !== 'undefined') {
            this.getLoginForm().down('#errorLabel').setValue('Session expired.');
            this.showLoginError();
        }
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

        me.hideLoginError();

        var unencodedToken = data.username + ':' + data.password;
        var encodedToken = 'Basic ' + Login.controller.Base64.encode(unencodedToken);

        me.getLoginViewport().mask('Verifying credentials...');

        Ext.Ajax.request({
            url: '/apps/login/index.html',
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
                if (typeof me.getLoginViewport() !== 'undefined') {
                    me.getLoginViewport().unmask();
                }
            }
        });
    },

    loginOK: function () {
        var params = Ext.urlDecode(location.search.substring(1)),
            page = params.page,
            token = Ext.History.getToken(),
            referrer = document.referrer;

        if (token) {
            page += '#' + token;
        }

        if (page) {
            window.location.replace(page);
        } else if (referrer) {
            location.href = referrer;
        } else {
            Uni.store.Apps.load(function (apps) {
                if (typeof apps !== 'undefined' && apps.length > 0) {
                    apps.forEach(function (app) {
                        var url = app.data.url || '';

                        if (url.indexOf('http://') !== 0 && url.indexOf('https://') !== 0) {
                            window.location.replace(url);
                        }
                    });
                }
            });
        }

        this.getLoginViewport().destroy();
    },

    loginNOK: function () {
        this.getLoginForm().down('#errorLabel').setValue('Login failed. Please contact your administrator.');
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
        Ext.Ajax.request({
            url: '/apps/login/index.html',
            method: 'GET',
            params: {
                logout: 'true'
            },
            scope: this,
            success: function () {
                window.location.replace('/apps/login/index.html');
            }
        });
    }
});
