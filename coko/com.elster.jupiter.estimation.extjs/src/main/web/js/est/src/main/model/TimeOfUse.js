Ext.define('Est.main.model.TimeOfUse', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'tou', type: 'integer', useNull: true}
    ]
});