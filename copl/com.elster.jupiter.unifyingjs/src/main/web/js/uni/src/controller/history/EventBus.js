/**
 * @class Uni.controller.history.EventBus
 */
Ext.define('Uni.controller.history.EventBus', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.util.History'
    ],

    config: {
        defaultToken: ''
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

            var token = Ext.util.History.getToken();
            if (token === null || token === '') {
                token = me.getDefaultToken();
                Ext.util.History.add(token);
            }
            me.onHistoryChange(token);
        });
    },

    onHistoryChange: function (token) {
        var queryStringIndex = token.indexOf('?');
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }

        crossroads.parse(token);
    }
});