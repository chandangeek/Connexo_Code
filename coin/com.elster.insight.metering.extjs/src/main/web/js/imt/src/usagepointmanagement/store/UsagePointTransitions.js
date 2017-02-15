/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.UsagePointTransitions', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.usagepointmanagement.model.UsagePointTransition'
    ],
    model: 'Imt.usagepointmanagement.model.UsagePointTransition',

    proxy: {
        type: 'rest',
        url: '/api/upl/usagepoint/{usagePointId}/transitions',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'transitions'
        }
    }
});