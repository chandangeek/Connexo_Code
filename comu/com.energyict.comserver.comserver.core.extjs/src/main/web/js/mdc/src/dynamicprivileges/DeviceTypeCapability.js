/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.dynamicprivileges.DeviceTypeCapability', {
    singleton: true,

    requires: [
        'Uni.DynamicPrivileges'
    ],

    supportsFileManagement: 'devicetype.supports.filemanagement'
});
