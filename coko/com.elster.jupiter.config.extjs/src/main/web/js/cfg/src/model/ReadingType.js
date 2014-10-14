Ext.define('Cfg.model.ReadingType', {
    extend: 'Ext.data.Model',

    fields: [
        'mRID',
        'aliasName',
        'name'
    ],

    associations: [
        {
            type: 'belongsTo',
            model: 'Cfg.model.ValidationRule',
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
