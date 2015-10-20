Ext.define('Sam.model.DataPurgeSetting', {
    extend: 'Uni.model.Version',
    idProperty: 'kind',
    fields: [
        'kind',
        'name',
        'retainedPartitionCount',
        'retention'
    ]
});