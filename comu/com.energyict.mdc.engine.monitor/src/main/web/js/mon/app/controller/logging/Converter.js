/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.logging.Converter', {
    extend: 'CSMonitor.controller.history.Converter',

    rootToken: 'logging',

    requires: [
        'CSMonitor.controller.history.Converter'
    ],

    doConversion: function (tokens) {
        if (tokens[0] !== this.rootToken) {
            return;
        }

        var generalLoggingController = this.getController('logging.general.Text'),
            dataLoggingController = this.getController('logging.data.Text'),
            communicationLoggingController = this.getController('logging.communication.Text');

        generalLoggingController.onTokens(tokens);
        dataLoggingController.onTokens(tokens);
        communicationLoggingController.onTokens(tokens);
    }

});
