Ext.define('Mdc.usagepointmanagement.model.UsagePoint', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'serviceCategory', type: 'string'},
        {name: 'serviceDeliveryRemark', type: 'string'},
        {name: 'amiBillingReady', type: 'string'},
        {name: 'connectionState', type: 'string'},
        {name: 'readCycle', type: 'string'},
        {name: 'serviceLocationID', type: 'number', useNull: true},
        {name: 'checkBilling', type: 'boolean'},
        {name: 'isSdp', type: 'boolean'},
        {name: 'isVirtual', type: 'boolean'},
        {name: 'minimalUsageExpected', type: 'boolean'},
        {name: 'version', type: 'number', useNull: true},
        {
            name: 'created',
            persist: false,
            mapping: function (data) {
                return Uni.DateTime.formatDateTimeLong(new Date(data.createTime));
            }
        },
        {
            name: 'updated',
            persist: false,
            mapping: function (data) {
                return Uni.DateTime.formatDateTimeLong(new Date(data.modTime));
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }
});
