Ext.define('Est.estimationtasks.model.MetrologyPurpose', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'displayValue', type: 'string'}
    ]
});
