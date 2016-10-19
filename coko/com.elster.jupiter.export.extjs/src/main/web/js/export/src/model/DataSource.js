Ext.define('Dxp.model.DataSource', {
    extend: 'Ext.data.Model',
    fields: [
        'mRID', 'active', 'serialNumber', 'readingType', 'occurrenceId', 'connectionState',
        {
            name: 'lastExportedDate',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});
