Ext.define('Dxp.model.AddDataExportTaskForm', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'name', 'deviceGroup', 'dataProcessor', 'dataSelector', 'exportPeriod', 'recurrence-type', 'recurrence-number', 'recurrence', 'start-on'
    ]
});
