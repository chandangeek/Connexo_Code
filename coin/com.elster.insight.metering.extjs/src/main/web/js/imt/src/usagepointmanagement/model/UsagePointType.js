/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.UsagePointType', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.util.UsagePointType'
    ],
    fields: ['name', 'displayName', 'isSdp', 'isVirtual',
        {
            name: 'typeOfUsagePoint',
            persist: false,
            mapping: function () {
                return Imt.util.UsagePointType.mapping.apply(this, arguments);
            }
        }
    ],
    idProperty: 'typeOfUsagePoint'
});