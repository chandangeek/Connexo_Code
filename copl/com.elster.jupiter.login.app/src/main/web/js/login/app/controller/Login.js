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

    init: function () {
        this.control({
            'viewport': {
                beforerender: this.showOverview
            },
            'login #login-form [action=login]': {
                click: this.signinuser
            },
            'login #login-form #password': {
                specialkey: this.onPasswordKey
            },
            'login #login-form #username': {
                specialkey: this.onUsernameKey
            }
        });
    },

    showOverview: function () {
        var params = Ext.urlDecode(location.search.substring(1));
        if (typeof params.expired !== 'undefined') {
            var error = this.getLoginForm().down('#errorLabel');
            error.setValue('Your session has expired.');
            error.show();
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

        form.down('#errorLabel').hide();

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
                var token = response.getResponseHeader('X-AUTH-TOKEN');
                localStorage.setItem('X-AUTH-TOKEN', token);

                Ext.Ajax.defaultHeaders = {
                    'Authorization': 'Bearer ' + token
                };

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
                    var iterator = 0, internal = undefined, external = undefined;

                    while(internal == undefined && iterator < apps.length){
                        if(apps[iterator].data.isExternal){
                            if(external == undefined){
                                external = apps[iterator];
                            }
                        }
                        else{
                            internal = apps[iterator];
                        }
                        iterator++;
                    }
                    window.location.replace((internal==undefined)?external.data.url:internal.data.url);
                }
            });
        }
        this.getLoginViewport().destroy();
    },

    loginNOK: function () {
        var form = this.getLoginForm(),
            password = form.down('#password'),
            error = form.down('#errorLabel');

        form.suspendLayouts();
        password.reset();
        password.focus(false, 200)
        error.setValue('Login failed. Please contact your administrator.');
        error.show();
        form.resumeLayouts(true);
    }
});
