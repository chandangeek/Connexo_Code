/**
 * @class Uni.controller.history.EventBus
 */
Ext.define('Uni.controller.history.EventBus', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.util.History',
        'Uni.store.MenuItems',
        'Uni.store.PortalItems'
    ],

    config: {
        defaultToken: '',
        previousPath: null,
        currentPath: null,
        previousQueryString: null,
        currentQueryString: null
    },

    onLaunch: function () {
        this.initHistory();
        this.initListeners();
    },

    initHistory: function () {
        var me = this;

        crossroads.bypassed.add(function (request) {
            crossroads.parse("/error/notfound");
        });

        Ext.util.History.init(function () {
            Ext.util.History.addListener('change', function (token) {
                me.onHistoryChange(token);
            });

            me.checkHistoryState();
        });
    },

    initListeners: function () {
        Uni.store.MenuItems.on({
            add: this.checkHistoryState,
            load: this.checkHistoryState,
            update: this.checkHistoryState,
            remove: this.checkHistoryState,
            bulkremove: this.checkHistoryState,
            scope: this
        });
    },

    checkHistoryState: function () {
        var me = this,
            token = Ext.util.History.getToken();

        if (token === null || token === '') {
            token = me.getDefaultToken();
            // checking hash after set produces execution of "onHistoryChange" twice. @see JP-8559
            Ext.util.History.setHash(token);
        } else {
            me.onHistoryChange(token);
        }
    },

    onHistoryChange: function (token) {
        var queryString;
        var queryStringIndex = token.indexOf('?');

        if (typeof token === 'undefined' || token === null || token === '') {
            token = this.getDefaultToken();
            Ext.util.History.add(token);
        }
        queryString = token.substring(queryStringIndex, token.length);
        if(queryString !== this.getCurrentQueryString()){
            Uni.util.QueryString.changed();
        }
        if(this.getCurrentQueryString() !== this.getPreviousQueryString() || (this.getCurrentQueryString()===null && this.getPreviousQueryString()===null)) {
            this.setPreviousQueryString(this.getCurrentQueryString());
            this.setCurrentQueryString(queryString);
        }
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }
        if(this.getCurrentPath() !== this.getPreviousPath() || (this.getCurrentPath()===null && this.getPreviousPath()===null)) {
            this.setPreviousPath(this.getCurrentPath());
            this.setCurrentPath(token);
            crossroads.parse(token);
        }

    }
});