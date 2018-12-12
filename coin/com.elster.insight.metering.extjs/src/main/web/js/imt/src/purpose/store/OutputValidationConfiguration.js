/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.OutputValidationConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.purpose.model.OutputValidationConfiguration'
    ],
    model: 'Imt.purpose.model.OutputValidationConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/validation',
        reader: {
            type: 'json',
            root: 'validation'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
