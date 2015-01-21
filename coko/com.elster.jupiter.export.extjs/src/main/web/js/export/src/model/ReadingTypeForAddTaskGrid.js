Ext.define('Dxp.model.ReadingTypeForAddTaskGrid', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'readingType'}
    ],

    associations: [
        {name: 'readingType', type: 'hasOne', model: 'Dxp.model.ReadingTypeFullData', associationKey: 'readingType',
            getterName: 'getReadingType', setterName: 'setReadingType', foreignKey: 'readingType'}
    ]
});