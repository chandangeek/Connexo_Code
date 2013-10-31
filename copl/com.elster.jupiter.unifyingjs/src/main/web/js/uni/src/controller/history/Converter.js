Ext.define('Uni.controller.history.Converter', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus'
    ],

    config: {
        rootToken: null
    },

    rootToken: null, // Implemented by extending classes.

    init: function () {
        var me = this,
            eventBus = me.getController('Uni.controller.history.EventBus');

        eventBus.addTokenObserver(function (tokens) {
            me.doConversion(tokens);
        }, me.getRootToken());
    },

    doConversion: function (tokens) {
        // Implemented by extending classes.
    },

    tokenize: function (tokens, includeHash) {
        includeHash = includeHash !== undefined ? includeHash : true;

        var me = this,
            token = '',
            delimiter = Uni.controller.history.Settings.tokenDelimiter;

        for (var i = 0; i < tokens.length; i++) {
            token += delimiter + tokens[i];
        }

        if (includeHash) {
            token = '#' + token;
        }

        return token;
    },

    /**
     * Default tokenize method for an overview.
     * @returns String History token.
     */
    tokenizeShowOverview: function () {
        return this.tokenize([this.rootToken]);
    }
});