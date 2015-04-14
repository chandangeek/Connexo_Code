Ext.define('Cfg.model.Interval', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'time', type: 'integer', useNull: true},
        {name: 'macro', type: 'integer', useNull: true}
    ]
});