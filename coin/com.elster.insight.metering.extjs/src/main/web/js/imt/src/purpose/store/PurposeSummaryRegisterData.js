/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.store.PurposeSummaryRegisterData', {
    extend: 'Uni.data.store.Filterable',
    model: 'Imt.purpose.model.PurposeRegisterReading',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/registerData',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
