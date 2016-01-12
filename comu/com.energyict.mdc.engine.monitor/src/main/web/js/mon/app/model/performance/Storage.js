Ext.define('CSMonitor.model.performance.Storage', {
    extend: 'Ext.data.Model',
    fields: [
        'time', 'load', 'threads', 'priority', 'capacity'
    ]
});