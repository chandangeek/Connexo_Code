Ext.define('Dxp.model.DataExportTaskHistory', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'startedOn', type: 'date', dateFormat: 'timestamp'},
        {name: 'finishedOn', type: 'date', dateFormat: 'timestamp'},
        {name: 'duration', type: 'int'},
        {name: 'status', type: 'string'},
        {name: 'exportPeriodFrom', type: 'date', dateFormat: 'timestamp'},
        {name: 'exportPeriodTo', type: 'date', dateFormat: 'timestamp'}
    ]
});