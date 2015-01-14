Ext.define('Uni.controller.Session', {
    extend: 'Ext.app.Controller',
    runner: null,
    body: null,
    initialized: false,
    lastResetTime: null,
    maxInactive: null,
    init: function () {
        this.runner = new Ext.util.TaskRunner();
        this.body = Ext.getBody();
        this.initialized = true;
        this.lastResetTime = new Date();
        this.getTimeOutFromserver();
        this.initListeners();
        this.start();
    },

    getTimeOutFromserver: function(){
        var me = this;
        Ext.Ajax.request({
            url: '/api/apps/session/timeout',
            method: 'GET',
            async: false,
            success: function (response) {
                var backEndTimeout = JSON.parse(response.responseText).sessionTimeOut
                me.maxInactive = (backEndTimeout-backEndTimeout/10)*1000;
            },
            failure: function () {
                window.location.replace('/apps/login/index.html');
            }
        });
    },

    initListeners: function () {
        this.body.on('mousedown', this.resetActivity, this);
        Ext.Ajax.on('beforerequest', this.checkRequestAndResetActivity, this);
    },

    start: function () {
        if (this.initialized) {
            this.runner.start(
                {
                    run: this.checkActivity,
                    interval: 1000 * 60,
                    scope: this
                }
            )
        }
    },

    checkActivity: function () {
        var currentTime = new Date();
        if (currentTime - this.lastResetTime > this.maxInactive) {
            this.logout();
        } else {
            this.keepBackEndAlive();
        }
    },

    resetActivity: function () {
        this.lastResetTime = new Date();
    },

    checkRequestAndResetActivity: function (con, options) {
        if (options.url !== '/api/apps/session/timeout') {
            this.resetActivity();
        }
    },

    logout: function () {
        Ext.Ajax.request({
            url: '/api/apps/apps/logout',
            method: 'POST',
            disableCaching: true,
            scope: this,
            success: function () {
                window.location.replace('/apps/login/index.html?expired&page='
                    + window.location.pathname
                    + window.location.hash);
            }
        });
    },

    keepBackEndAlive: function () {
        Ext.Ajax.request({
            url: '/api/apps/session/timeout',
            method: 'GET',
            async: false,
            success: function (response) {
                //do nothing
            },
            failure: function () {
                window.location.replace('/apps/login/index.html');
            }
        });
    }


});
