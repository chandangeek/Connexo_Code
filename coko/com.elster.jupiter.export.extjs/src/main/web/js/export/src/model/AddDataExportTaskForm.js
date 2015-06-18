Ext.define('Dxp.model.AddDataExportTaskForm', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'name',
        'readingTypeDataSelector.value.endDeviceGroup',
        'dataProcessor',
        'readingTypeDataSelector.value.dataSelector',
        'readingTypeDataSelector.value.exportPeriod',
        'recurrence-type',
        'recurrence-number',
        'recurrence',
        'start-on'
    ]
});
