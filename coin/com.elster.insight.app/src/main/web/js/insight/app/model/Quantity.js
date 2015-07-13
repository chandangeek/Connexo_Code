Ext.define('InsightApp.model.Quantity', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'value', type: 'number', useNull: true},
        {name: 'unit', type: 'string'},
        {name: 'multiplier', type: 'number'}
    ]
});
