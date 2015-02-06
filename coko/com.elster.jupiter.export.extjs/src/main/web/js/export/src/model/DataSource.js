Ext.define('Dxp.model.DataSource', {
    extend: 'Ext.data.Model',
    fields: [
        'mRID', 'active', 'serialNumber', 'readingType', 'occurrenceId',
        {
            name: 'lastRun',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'lastExportedDate',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});
