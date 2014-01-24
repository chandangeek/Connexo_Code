/**
 * @class Uni.controller.history.EventBus
 */
Ext.define('Uni.controller.history.EventBus', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.util.History',
        'Uni.controller.history.Settings',
        'Uni.controller.Error'
    ],

    config: {
        defaultToken: '',
        // Observers that want to listen to every history change.
        rootObservers: [],
        // Only supports 1 observer for a token.
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
        if (typeof token === 'undefined') {
            this.getRootObservers().push(callback);
        } else {
            this.getObservers()[token] = callback;
        }
    },

    tokenize: function (token) {
        var tokens = [];

        if (typeof token !== 'undefined') {
            var queryStringIndex = token.indexOf('?');
            if (queryStringIndex > 0) {
                token = token.substring(0, queryStringIndex);
            }

            var uncheckedTokens = token.split(Uni.controller.history.Settings.tokenDelimiter);

            for (var i = 0; i < uncheckedTokens.length; i++) {
                if (uncheckedTokens[i] != '' && uncheckedTokens[i] != '#') { // Remove the invalid values.
                    tokens.push(uncheckedTokens[i]);
                }
            }
        }

        return tokens;
    },

    notifyRootObservers: function (tokens) {
        for (var i = 0; i < this.getRootObservers().length; i++) {
            var callback = this.getRootObservers()[i];
            callback(tokens, Uni.controller.history.Settings.tokenDelimiter);
        }
    },

    notifyObserversIfNecessary: function (tokens) {
        var errorController = this.getController('Uni.controller.Error'),
            callback = this.getObservers()[tokens[0]];

        if (typeof callback !== 'undefined' && callback != null) {
            callback(tokens, Uni.controller.history.Settings.tokenDelimiter);
        } else {
            // TODO Design the basic error controller.
//            errorController.showHttp404();
        }
    }
});