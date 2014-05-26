/**
 * @class Uni.controller.history.Converter
 */
Ext.define('Uni.controller.history.Converter', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus'
    ],

//    rootToken: null, // Implemented by extending classes.
//
//    init: function () {
//        var me = this,
//            eventBus = me.getController('Uni.controller.history.EventBus');
//
////        eventBus.addTokenObserver(function (tokens,token) {
////            me.doConversion(tokens,token);
////        }, me.rootToken);
//    },

//    doConversion: function (tokens, token) {
//        //now has tokens and token (which is you complete path)
//
//        var queryStringIndex = token.indexOf('?');
//        if (queryStringIndex > 0) {
//            token = token.substring(0, queryStringIndex);
//        }
//        if (this.currentPath !== null) {
//            this.previousPath = this.currentPath;
//        }
//        this.currentPath = token;
//        crossroads.parse(token);
//    },

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

    tokenizePath: function(path,includeHash) {
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