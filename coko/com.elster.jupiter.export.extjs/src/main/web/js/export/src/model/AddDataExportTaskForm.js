Ext.define('Dxp.model.AddDataExportTaskForm', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'name',
        'readingTypeDataSelector.value.endDeviceGroup',
        'readingTypeDataSelector.value.usagePointGroup',
        'dataProcessor',
        'readingTypeDataSelector.value.dataSelector',
        'readingTypeDataSelector.value.exportPeriod',
        'recurrence-type',
        'recurrence-number',
        'recurrence',
        'start-on',
        'validatedDataOption',
        'exportComplete',
        'exportUpdate',
        'updateWindow',
        'updateTimeFrame',
        'exportContinuousData',
        'updatedDataAndOrAdjacentData'
    ]
});
