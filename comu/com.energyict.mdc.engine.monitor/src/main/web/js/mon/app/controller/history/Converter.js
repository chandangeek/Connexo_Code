/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.history.Converter', {
    extend: 'Ext.app.Controller',

    requires: [
        'CSMonitor.controller.history.EventBus'
    ],

    rootToken: null, // Implemented by extending classes.

    init: function () {
        var me = this,
            eventBus = me.getController('history.EventBus');

        eventBus.addTokenObserver(function (tokens) {
            me.doConversion(tokens);
        }, me.rootToken);
    },

    doConversion: function (tokens) {
        // Implemented by extending classes.
    },

    tokenize: function (tokens, includeHash) {
        includeHash = includeHash !== undefined ? includeHash : true;

        var me = this,
            i,
            token = '',
            delimiter = CSMonitor.controller.history.Settings.tokenDelimiter;

        for (i = 0; i < tokens.length; i += 1) {
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