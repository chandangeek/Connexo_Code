Ext.define('Dxp.model.ReadingType', {
    extend: 'Ext.data.Model',

    fields: [
        'mRID',
        'aliasName',
        'name'
    ],

    associations: [
        {
            type: 'belongsTo',
            model: 'Dxp.model.DataExportTask',
            name: 'readingTypes'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/readingtypes',
        reader: {
            type: 'json',
            root: 'readingTypes'
        }
    }
});
