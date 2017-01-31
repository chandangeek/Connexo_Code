/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.metrologyconfiguration.MeterRole', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name', 'required', 'meter', 'url',
        {
            name: 'activationTime',
            type: 'date',
            dateFormat: 'time'
        }
    ]
});
