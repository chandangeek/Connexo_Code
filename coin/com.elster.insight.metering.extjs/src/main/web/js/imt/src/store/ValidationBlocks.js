/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.store.ValidationBlocks', {
    extend: 'Ext.data.Store',
    model: 'Imt.model.ValidationBlock',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/channels/{channelId}/datavalidationissues/{issueId}/validationblocks',
        timeout: 240000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'validationBlocks',
            idProperty: 'startTime'
        }
    }
});
