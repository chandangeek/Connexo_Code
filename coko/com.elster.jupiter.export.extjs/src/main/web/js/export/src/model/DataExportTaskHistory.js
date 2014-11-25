Ext.define('Dxp.model.DataExportTaskHistory', {
    extend: 'Dxp.model.DataExportTask',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'exportPeriodFrom', type: 'number'},
        {name: 'exportPeriodTo', type: 'number'}
    ]
});