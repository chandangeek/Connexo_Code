/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.ExcludedDeviceGroup', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'ruleId',
            type: 'int',
        },
        {
            name: 'ruleName',
            type: 'string'
        },
        {
            name: 'deviceGroupId',
            type: 'int'
        },
        {
            name: 'deviceGroupName',
            type: 'string'
        },
        {
            name: 'isGroupDynamic',
            type: 'boolean'
        }
    ]
});
