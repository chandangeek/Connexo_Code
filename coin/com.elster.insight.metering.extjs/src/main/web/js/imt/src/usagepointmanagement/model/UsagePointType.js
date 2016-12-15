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