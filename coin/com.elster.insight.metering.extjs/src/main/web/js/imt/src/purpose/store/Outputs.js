/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.Outputs', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.Output',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs',
        reader: {
            type: 'json',
            root: 'outputs'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});