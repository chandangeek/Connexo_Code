/**
 * @class Uni.controller.history.EventBus
 */
Ext.define('Uni.controller.history.EventBus', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.util.History'
    ],

    config: {
        defaultToken: '',
        previousPath: null,
        currentPath: null
    },

    onLaunch: function () {
        this.initHistory();
    },

    initHistory: function () {
        var me = this;

        crossroads.bypassed.add(function(request){
            crossroads.parse("/error/notfound");
        });

        Ext.util.History.init(function () {
            Ext.util.History.addListener('change', function (token) {
                me.onHistoryChange(token);
            });

            me.checkHistoryState();
        });
    },

    checkHistoryState: function () {
        var me = this,
            token = Ext.util.History.getToken();

        if (token === null || token === '') {
            token = me.getDefaultToken();
            Ext.util.History.add(token);
            Ext.util.History.currentToken = token;
        }

        me.onHistoryChange(token);
    },

    onHistoryChange: function (token) {
        var queryStringIndex = token.indexOf('?');
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }

        if (this.getCurrentPath() !== null) {
            this.setPreviousPath(this.getCurrentPath());
        }
        this.setCurrentPath(token);

        crossroads.parse(token);
    }
});