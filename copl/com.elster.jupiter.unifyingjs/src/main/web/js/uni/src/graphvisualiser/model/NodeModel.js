Ext.define('Uni.graphvisualiser.model.NodeModel', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'multiplier',
        'unit',
        'deviceType',
        'deviceLifecycleStatus',
        'deviceConfiguration',
        'serialNumber',
        'alarms',
        'issues',
        'gateway',
        'failedCommunications',
        'failedComTasks',
        'deviceCoordinates',
        'hasCoordonates'
    ]
});