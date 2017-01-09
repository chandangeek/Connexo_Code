Ext.define('Imt.metrologyconfiguration.model.ReadingTypeDeliverable', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.model.ReadingType',
        'Imt.metrologyconfiguration.model.Formula'
    ],
    fields: ['id',
        'name',
        {name: 'metrologyContract', persist: false},
        {name: 'metrologyContractIsMandatory', persist: false},
        'readingType'
    ],

    associations: [
        {
            name: 'readingType',
            type: 'hasOne',
            model: 'Imt.model.ReadingType',
            associationKey: 'readingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType'
        },
        {
            name: 'formula',
            type: 'hasOne',
            model: 'Imt.metrologyconfiguration.model.Formula',
            associationKey: 'formula',
            getterName: 'getFormula',
            setterName: 'setFormula'
        }
    ]
});