/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dynamicprivileges.Stores', {
    singleton: true,

    required: [
        'Imt.usagepointmanagement.store.UsagePointPrivileges'
    ],

    usagePointStore: [
        'Imt.usagepointmanagement.store.UsagePointPrivileges'
    ],

    all: [
        'Imt.usagepointmanagement.store.UsagePointPrivileges'
    ]
});