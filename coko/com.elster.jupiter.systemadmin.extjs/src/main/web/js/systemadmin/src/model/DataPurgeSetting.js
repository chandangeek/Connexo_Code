Ext.define('Sam.model.DataPurgeSetting', {
    extend: 'Ext.data.Model',
    alias: 'widget.lic-model',
    idProperty: 'kind',
    fields: [
        'kind',
        'name',
        'retainedPartitionCount',
        'retention'
    ]
});