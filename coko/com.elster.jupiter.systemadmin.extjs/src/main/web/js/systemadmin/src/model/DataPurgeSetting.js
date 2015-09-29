Ext.define('Sam.model.DataPurgeSetting', {
    extend: 'Ext.data.Model',
    idProperty: 'kind',
    fields: [
        'kind',
        'name',
        'retainedPartitionCount',
        'retention'
    ]
});