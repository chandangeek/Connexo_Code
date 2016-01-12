Ext.define('Uni.controller.Session', {
    extend: 'Ext.app.Controller',
    runner: null,
    body: null,
    initialized: false,
    lastResetTime: null,
    maxInactive: null,
    verbose:false,
    init: function () {
        this.runner = new Ext.util.TaskRunner();
        this.body = Ext.getBody();
        this.initialized = true;
        this.lastResetTime = new Date();
        this.getTimeOutFromserver();
        this.initListeners();
    },

    getTimeOutFromserver: function(){
        var me = this;
        Ext.Ajax.request({
            url: '/api/apps/session/timeout',
            method: 'GET',
            async: false,
            success: function (response) {
                var backEndTimeout = JSON.parse(response.responseText).sessionTimeOut;
                me.maxInactive = (backEndTimeout-backEndTimeout/10)*1000; // 10% earlier
                me.log("Inactivity timeout " + me.maxInactive);
            },
            failure: function () {
                //window.location.replace('/apps/login/index.html');
            }
        });
    },

    initListeners: function () {
        this.body.on('mousedown', this.checkForExpiration, this);
        Ext.Ajax.on('beforerequest', this.checkRequestAndResetActivity, this);
    },

    checkRequestAndResetActivity: function (con, options) {
        if (options.url !== '/api/apps/session/timeout') {
            this.resetActivity();
        }
    },

    checkForExpiration: function(){
        var me = this;
        me.log("Check for expiration  ...");
        var currentTime = new Date();
        if (currentTime - this.lastResetTime > this.maxInactive) {
            // if there was no user activity in specified period check to see if the session is
            // still available due activities from different tabs.
            me.log("Inactivity timeout is already expired... Checking backend... ");
            this.keepBackEndAlive();
        }
        else{
            me.log(" not yet.");
        }
    },
    resetActivity: function () {
        this.log("Reset Activity  ....");
        this.lastResetTime = new Date();
    },

    logout: function () {
        var me = this;
        me.log('Logout requested ...');
        Ext.Ajax.request({
            url: '/api/apps/apps/logout',
            method: 'POST',
            disableCaching: true,
            scope: this,
            callback: function () {
                me.log('Redirecting to login page ....');
                window.location.replace('/apps/login/index.html');
            }
        });
    },

    keepBackEndAlive: function () {
        var me = this;
        me.log('Check backend ...');
        Ext.Ajax.request({
            url: '/api/apps/session/timeout',
            method: 'GET',
            async: false,
            success: function (response) {
                me.log('Session is OK');
                me.resetActivity();
            },
            failure: function () {
                me.log('Session might be expired ...');
                me.logout();
            }
        });
    },
    log : function(msg) {
        if (this.verbose && window.console) {
            window.console.log("ACTIVITY MONITOR::" + msg);
        }
    }


});
