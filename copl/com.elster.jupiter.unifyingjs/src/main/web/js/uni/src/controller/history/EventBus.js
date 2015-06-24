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
        previousQueryString: null,
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
        var queryString,
            queryStringIndex = token.indexOf('?'),
            queryStringChanged = false,
            pathChanged = false;

        if (typeof token === 'undefined' || token === null || token === '') {
            token = this.getDefaultToken();
            Ext.util.History.add(token);
        }
        queryString = queryStringIndex===-1 ? null : token.substring(queryStringIndex+1, token.length);
        if(queryString !== this.getPreviousQueryString()){
            this.setPreviousQueryString(queryString);
            if (!Uni.util.History.isSuspended()) {
                queryStringChanged = true;
            }
        }
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }
        if (this.getPreviousPath()===null || token !== this.getPreviousPath()) {
            this.setPreviousPath(token);
            if (!Uni.util.History.isSuspended()) {
                pathChanged = true;
            }
        }
        if (Uni.util.History.isParsePath() || pathChanged) {
            crossroads.parse(token);
            this.setPreviousQueryString(null);
        } else if (queryStringChanged) {
            Uni.util.QueryString.changed(queryString);
        }

        if (Uni.util.History.isSuspended()) {
            Uni.util.History.setSuspended(false);
        }
        if (!Uni.util.History.isParsePath()) {
            Uni.util.History.setParsePath(true);
        }
    }
});