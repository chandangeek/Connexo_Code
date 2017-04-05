/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.RegisterTypesMap', {
    singleton: true,

    addPreview: {
        CUMULATIVE_VALUE: 'register-data-cumulative-preview',
        CUMULATIVE_BILLING_VALUE: 'register-data-cumulative-preview',
        CUMULATIVE_EVENT_BILLING_VALUE: 'register-data-cumulative-preview',
        NOT_CUMULATIVE_VALUE: 'register-data-noCumulative-preview',
        NOT_CUMULATIVE_BILLING_VALUE: 'register-data-noCumulative-preview',
        EVENT_VALUE: 'register-data-event-preview',
        EVENT_BILLING_VALUE: 'register-data-event-preview'
    },
    addGrid: {
        CUMULATIVE_VALUE: 'register-data-cumulative-grid',
        CUMULATIVE_BILLING_VALUE: 'register-data-cumulative-grid',
        CUMULATIVE_EVENT_BILLING_VALUE: 'register-data-cumulative-grid',
        NOT_CUMULATIVE_VALUE: 'register-data-noCumulative-grid',
        NOT_CUMULATIVE_BILLING_VALUE: 'register-data-noCumulative-grid',
        EVENT_VALUE: 'register-data-event-grid',
        EVENT_BILLING_VALUE: 'register-data-event-grid'
    },

    getAddPreview: function (key) {
        return this.addPreview[key] ? this.addPreview[key] : this.addPreview['CUMULATIVE_VALUE'];
    },

    getAddGrid: function (key) {
        return this.addGrid[key] ? this.addGrid[key] : this.addGrid['CUMULATIVE_VALUE'];
    }

});
