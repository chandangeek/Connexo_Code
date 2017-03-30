/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.history.EventBus', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.util.History',
        'CSMonitor.controller.history.Settings'
    ],

    config: {
        defaultToken: '',
        // Observers that want to listen to every history change:
        rootObservers: [],
        // Only supports 1 observer for a token:
        observers: []
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
        var tokens = this.tokenize(token);
        if (tokens.length === 0) {
            tokens = this.tokenize(this.getDefaultToken());
        }

        this.notifyRootObservers(tokens);
        this.notifyObserversIfNecessary(tokens);
    },

    /**
     * Adds an observer for a specific token change.
     * @param callback  Function to call when the token changed.
     * @param token     What token to be active for, if no token is given the callback is called
     *                  for every history change.
     */
    addTokenObserver: function (callback, token) {
        if (token === undefined) {
            this.getRootObservers().push(callback);
        } else {
            this.getObservers()[token] = callback;
        }
    },

    tokenize: function (token) {
        var tokens = [];

        if (token !== undefined) {
            var i, uncheckedTokens = token.split(CSMonitor.controller.history.Settings.tokenDelimiter);

            for (i = 0; i < uncheckedTokens.length; i += 1) {
                if (uncheckedTokens[i] !== '' && uncheckedTokens[i] !== '#') { // Remove the invalid values.
                    tokens.push(uncheckedTokens[i]);
                }
            }
        }

        return tokens;
    },

    notifyRootObservers: function (tokens) {
        var i;
        for (i = 0; i < this.getRootObservers().length; i += 1) {
            var callback = this.getRootObservers()[i];
            callback(tokens, CSMonitor.controller.history.Settings.tokenDelimiter);
        }
    },

    notifyObserversIfNecessary: function (tokens) {
        var callback = this.getObservers()[tokens[0]];

        if (callback !== undefined && callback !== null) {
            callback(tokens, CSMonitor.controller.history.Settings.tokenDelimiter);
        }
    }
});