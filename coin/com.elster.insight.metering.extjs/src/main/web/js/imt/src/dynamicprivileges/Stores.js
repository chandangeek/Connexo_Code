/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dynamicprivileges.Stores', {
    singleton: true,

    required: [
        'Imt.usagepointmanagement.store.UsagePointPrivileges',
        'Imt.usagepointmanagement.store.MetrologyConfigurationDefinePrivileges'
    ],

    usagePointStore: [
        'Imt.usagepointmanagement.store.UsagePointPrivileges'
    ],

    definePrivileges: [
        'Imt.usagepointmanagement.store.MetrologyConfigurationDefinePrivileges'
    ],

    all: [
        'Imt.usagepointmanagement.store.UsagePointPrivileges'
    ]
});