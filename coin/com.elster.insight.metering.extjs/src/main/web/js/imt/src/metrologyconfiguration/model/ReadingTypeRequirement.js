Ext.define('Imt.metrologyconfiguration.model.ReadingTypeRequirement', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.model.ReadingType'
    ],
    fields: ['type', 'meterRole', 'readingTypePattern'],

    associations: [
        {
            name: 'readingType',
            type: 'hasOne',
            model: 'Imt.model.ReadingType',
            associationKey: 'readingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType'
        }
    ]
});