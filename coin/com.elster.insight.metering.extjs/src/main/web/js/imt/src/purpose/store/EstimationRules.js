/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.EstimationRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.purpose.model.EstimationRule'
    ],
    model: 'Imt.purpose.model.EstimationRule',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/channelData/estimateWithRule',
        reader: {
            type: 'json',
            root: 'rules',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
