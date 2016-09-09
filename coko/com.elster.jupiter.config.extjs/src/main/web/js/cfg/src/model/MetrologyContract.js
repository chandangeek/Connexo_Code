Ext.define('Cfg.model.MetrologyContract', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'displayValue', type: 'string'}
    ]
});
