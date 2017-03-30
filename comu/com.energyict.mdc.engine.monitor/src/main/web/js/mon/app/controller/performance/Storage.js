/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.controller.performance.Storage', {
    extend: 'Ext.app.Controller',

    views: ['performance.Storage'],

    refs: [
        {
            ref: 'viewPanel', // To be able to use further on 'getViewPanel()' to get the corresponding view instance (being a panel)
            selector: 'storage'
        }
    ],

    setPriority: function(priority) {
        if (this.getViewPanel()) {
            this.getViewPanel().setPriority(priority);
        }
    }
});