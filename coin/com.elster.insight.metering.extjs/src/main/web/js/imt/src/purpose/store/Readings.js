/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.Readings', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.Reading',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/channelData',
        reader: {
            type: 'json',
            root: 'channelData'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});