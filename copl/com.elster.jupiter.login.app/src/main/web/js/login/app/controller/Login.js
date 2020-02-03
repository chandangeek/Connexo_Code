/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        if (typeof params.logout !== 'undefined') {
            window.localStorage.clear();
        }

        if (typeof params.expired !== 'undefined') {
            window.localStorage.clear();
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
                if(token){
                    Ext.Ajax.defaultHeaders = {
                        'Authorization': 'Bearer ' + token
                    };
                }

                    me.loginOK();


            },
            failure: function (response, opt) {
                me.loginNOK((response.responseText));
            },
            callback: function () {
                if (typeof me.getLoginViewport() !== 'undefined') {
                    me.getLoginViewport().unmask();
                }
            }
        });
    },

    loginOK: function () {
        var me = this,
            params = Ext.urlDecode(location.search.substring(1)),
            page = params.page,
            token = Ext.History.getToken(),
            referrer = document.referrer;

        if (token) {
            page += '#' + token;
        }

        if (page) {
            window.location.replace(page);
            me.getLoginViewport().destroy();
        } else {
            Uni.store.Apps.load(function (apps) {
                if (typeof apps !== 'undefined' && apps.length > 0) {
                    var iterator = 0, internal = undefined, external = undefined, useReferrer = false;

                    if (referrer) {
                        apps.forEach(function (app) {
                            if (app.data.url && referrer.indexOf(app.data.url.replace('#', ''))!=-1){
                                window.location.href = encodeURI(referrer);
                                useReferrer = true;
                                me.getLoginViewport().destroy();
                                return;
                            }
                        });
                    }

                    if (useReferrer == false) {
                        while (internal == undefined && iterator < apps.length) {
                            if (apps[iterator].data.isExternal) {
                                if (external == undefined) {
                                    external = apps[iterator];
                                }
                            }
                            else {
                                internal = apps[iterator];
                            }
                            iterator++;
                        }
                        window.location.replace((internal == undefined) ? external.data.url : internal.data.url);
                        me.getLoginViewport().destroy();
                    }
                }
                else {
                    me.loginNOK(response.responseText);
                }
            });
        }
    },

    loginNOK: function (message) {
        var form = this.getLoginForm(),
            password = form.down('#password'),
            error = form.down('#errorLabel');
        window.localStorage.removeItem('X-AUTH-TOKEN');
        form.suspendLayouts();
        password.reset();
        password.focus(false, 200);
        if (message == 'AccountLocked')
            error.setValue('Account locked. Please contact your administrator.');
        else
            error.setValue('Login failed. Please contact your administrator.');
        error.show();
        form.resumeLayouts(true);
    }
});
