Ext.define('Dxp.model.Interval', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'time', type: 'integer', useNull: true}
    ]
});
