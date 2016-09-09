Ext.define('Cfg.model.ReadingTypeForGrid', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'readingType'}
    ],

    associations: [
        {name: 'readingType', type: 'hasOne', model: 'Cfg.model.ReadingType', associationKey: 'readingType',
            getterName: 'getReadingType', setterName: 'setReadingType', foreignKey: 'readingType'}
    ]
});