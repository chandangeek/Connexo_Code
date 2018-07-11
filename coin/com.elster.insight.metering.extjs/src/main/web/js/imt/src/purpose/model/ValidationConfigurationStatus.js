/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.ValidationConfigurationStatus', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'allDataValidated', type: 'boolean'},
        {name: 'hasSuspects', type: 'boolean'},
        {name: 'informativeReason'},
        {name: 'suspectReason'},
        {name: 'validationActive', type: 'boolean'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/validationrulesets/validationstatus',
        timeout: 60000,
        reader: {
            type: 'json',
            totalProperty: 'total'
        }
    }
});
