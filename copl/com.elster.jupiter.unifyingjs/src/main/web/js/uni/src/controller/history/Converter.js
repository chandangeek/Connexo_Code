/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.controller.history.Converter
 */
Ext.define('Uni.controller.history.Converter', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus'
    ],

    rootToken: null, // Implemented by extending classes.

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
        this.callParent(arguments);
    },

    tokenize: function (tokens, includeHash) {
        includeHash = includeHash !== undefined ? includeHash : true;

        var token = '',
            delimiter = Uni.controller.history.Settings.tokenDelimiter;

        for (var i = 0; i < tokens.length; i++) {
            token += delimiter + tokens[i];
        }

        if (includeHash) {
            token = '#' + token;
        }

        return token;
    },

    tokenizePath: function (path, includeHash) {
        includeHash = includeHash !== undefined ? includeHash : true;
        if (includeHash) {
            path = '#' + path;
        }
        return path;
    },

    /**
     * Default tokenize method for an overview.
     * @returns String History token.
     */
    tokenizeShowOverview: function () {
        return this.tokenize([this.rootToken]);
    }
});