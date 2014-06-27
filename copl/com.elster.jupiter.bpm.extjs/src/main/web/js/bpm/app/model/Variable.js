Ext.define('Bpm.model.Variable', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'variableInstanceId', type: 'string', useNull: true},
        {name: 'value', type: 'string', useNull: true},
        {name: 'date', type: 'string', useNull: true}
    ]
});
