/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.metrologyconfiguration.Purpose', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name', 'required', 'active', 'status', 'meterRoles', 'description'
    ]
});
