Ext.define('Uni.controller.Session', {
    extend: 'Ext.app.Controller',
    runner: null,
    body: null,
    initialized: false,
    lastResetTime: null,
    maxInactive: null,
    verbose:true,
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
    checkRequestAndResetActivity: function (con, options) {
        if (options.url !== '/api/apps/session/timeout') {
            this.resetActivity();
        }
    },

    checkActivity : function(){
        var me = this;
        var currentTime = new Date();
        me.log("Check Activity  ....");
        if (currentTime - this.lastResetTime > this.maxInactive) {
            // don't do anything since it possible to have active sessions in different tabs.
            // other Connection sessions like opened reports, or sessions with Reports Designer
            me.log("Inactivity timeout expired.");
        }
        else{
            me.log("OK.");
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
                window.location.replace('/apps/login/index.html?expired&page='
                    + window.location.pathname
                    + window.location.hash);
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
