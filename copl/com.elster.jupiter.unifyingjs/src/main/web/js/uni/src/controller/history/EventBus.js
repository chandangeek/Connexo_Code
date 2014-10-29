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
        }

        me.onHistoryChange(token);
    },

    onHistoryChange: function (token) {
        var queryStringIndex = token.indexOf('?');


        var current = token;
        if (queryStringIndex > 0) {
            current = current.substring(0, queryStringIndex);
        }

        var previous = this.getCurrentPath();

        if (this.getCurrentPath() !== null) {
            this.setPreviousPath(this.getCurrentPath());
        }
        this.setCurrentPath(token);


        if (current !== previous) {
            crossroads.parse(token);
        }
    }
});