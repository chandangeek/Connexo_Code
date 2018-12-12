/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.OutputEstimationConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.purpose.model.OutputEstimationConfiguration'
    ],
    model: 'Imt.purpose.model.OutputEstimationConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/estimation',
        reader: {
            type: 'json',
            root: 'estimation'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
