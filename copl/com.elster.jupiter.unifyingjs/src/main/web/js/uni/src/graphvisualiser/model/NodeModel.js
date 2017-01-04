Ext.define('Uni.graphvisualiser.model.NodeModel', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'multiplier',
        'unit',
        'deviceType',
        'deviceConfiguration',
        'serialNumber',
        'alarms',
        'issues',
        'gateWay',
        'failedComTasks'
    ]
});