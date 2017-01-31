/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.Converter', {
    extend: 'CSMonitor.controller.history.Converter',

    rootToken: undefined,

    requires: [
        'CSMonitor.controller.history.Converter'
    ],

    refs: [
        {
            ref: 'mainContainer',
            selector: 'app-main'
        }
    ],

    doConversion: function (tokens) {
        if (tokens.length !== 0) {
            return;
        }
        this.getMainContainer().addEmptyTabPanel();
        this.getMainContainer().addTab('Status', 'status');
        this.getMainContainer().addTab('Performance', 'performance');
        this.getMainContainer().addTab('Logging', 'logging');
        this.getMainContainer().setActiveTab(0);
    }

});
