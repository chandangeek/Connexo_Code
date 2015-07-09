Ext.define('InsightApp.usagepointmanagement.model.MeterActivations', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'start', type: 'number', useNull: true},
        {name: 'end', type: 'number', useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'meter', type: 'auto'}
    ]
});