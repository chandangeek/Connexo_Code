/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceValidation', {
    extend: 'Ext.data.Model',
    fields: [
        'allDataValidated',
        'hasValidation',
        'isActive',
        'loadProfileSuspectCount',
        'registerSuspectCount',
        {name: 'lastChecked', dateFormat: 'time', type: 'date'}
    ]
});
