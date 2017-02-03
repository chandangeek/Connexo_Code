/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.RegisterReadings', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.RegisterReading',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/registerData',
        reader: {
            type: 'json',
            root: 'registerData'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});