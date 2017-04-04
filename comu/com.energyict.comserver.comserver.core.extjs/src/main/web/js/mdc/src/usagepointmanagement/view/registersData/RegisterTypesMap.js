/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.RegisterTypesMap', {
    singleton: true,

    addPreview: {
        event: 'register-data-event-preview',
        сumulative: 'register-data-сumulative-preview',
        noCumulative: 'register-data-noCumulative-preview'
    },
    addGrid: {
        event: 'register-data-event-grid',
        сumulative: 'register-data-сumulative-grid',
        noCumulative: 'register-data-noCumulative-grid'
    },

    getAddPreview: function (key) {
        return this.addPreview[key] ? this.addPreview[key] : this.addPreview['сumulative'];
    },

    getAddGrid: function (key) {
        return this.addGrid[key] ? this.addGrid[key] : this.addGrid['сumulative'];
    }

});
