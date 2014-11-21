Ext.define('Dxp.store.DataSources', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DataSource',
    data: [
        {mRID: 'GT656956', status: 'Active', serialNumber: '1856542-123', readingType: 'A+ all phases (kWh)', lastRun: '1206561250', lastExportedData: '1416111250'}
    ]

   /* proxy: {
        type: 'rest',
        url: '/api/export/datasources',
        reader: {
            type: 'json',
            root: 'dataSources'
        }
    }*/
});

