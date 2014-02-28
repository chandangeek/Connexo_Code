Ext.define('Mtr.model.UsagePoint', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'mRID',
        'serviceCategory',
        'serviceLocationId',
        'aliasName',
        'description',
        'name',
        'amiBillingReady',
        {name: 'checkBilling', type: 'boolean'},
        'connectionState',
        {name: 'estimatedLoad', type: 'quantity'},
        'grounded',
        'isSdp',
        'isVirtual',
        'minimalUsageExpected',
        {name: 'nominalServiceVoltage', type: 'quantity'},
        'outageRegion',
        'phaseCode',
        {name: 'ratedCurrent', type: 'quantity'},
        {name: 'ratedPower', type: 'quantity'},
        'readCycle',
        'readRoute',
        'serviceDeliveryRemark',
        'servicePriority',
        'serviceLocation',
        'version',
        { name: 'createTime', type: 'date', dateFormat: 'time'},
        { name: 'modTime', type: 'date', dateFormat: 'time'},
        { name: 'street', mapping: 'serviceLocation.mainAddress.streetDetail.name'},
        { name: 'number', mapping: 'serviceLocation.mainAddress.streetDetail.number'},
        { name: 'town', mapping: 'serviceLocation.mainAddress.townDetail.name'},
        { name: 'zip', mapping: 'serviceLocation.mainAddress.townDetail.code'},
        { name: 'state', mapping: 'serviceLocation.mainAddress.townDetail.stateOrProvince'},
        { name: 'country', mapping: 'serviceLocation.mainAddress.townDetail.country'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints',
        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }
});
