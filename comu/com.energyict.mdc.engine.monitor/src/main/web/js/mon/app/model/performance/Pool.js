Ext.define('CSMonitor.model.performance.Pool', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'delay', // in ms
        'threads',
        'ports'
    ]
});