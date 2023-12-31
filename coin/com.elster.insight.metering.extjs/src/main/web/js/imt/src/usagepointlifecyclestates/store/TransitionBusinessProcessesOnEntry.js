/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecyclestates.store.TransitionBusinessProcessesOnEntry', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.usagepointlifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Imt.usagepointlifecyclestates.model.TransitionBusinessProcess',
    modelId: -1,
    removeAll: function () {
        this.callParent(arguments);
        this.modelId = -1;
    }
});
