Ext.define('Est.estimationtasks.model.UsagePointGroup', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'displayValue', type: 'string'}
    ]
});
