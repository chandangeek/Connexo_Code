Ext.define('Dxp.model.DataSource', {
    extend: 'Ext.data.Model',
    fields: [
        'mRID', 'status', 'serialNumber', 'readingType',
        {
            name: 'lastRun',
            mapping: function (data) {
                return moment(data.lastRun).format('ddd, DD MMM YYYY HH:mm:ss');
            }
        },
        {
            name: 'lastExportedData',
            mapping: function (data) {
                return moment(data.lastExportedData).format('ddd, DD MMM YYYY HH:mm:ss');
            }
        }
    ]
});
