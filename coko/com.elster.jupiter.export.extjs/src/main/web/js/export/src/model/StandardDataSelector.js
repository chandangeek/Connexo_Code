Ext.define('Dxp.model.StandardDataSelector', {
    extend: 'Ext.data.Model',


    fields: [
        'deviceGroup', 'exportPeriod', 'exportContinuousData', 'exportUpdate', 'updatePeriod', 'validatedDataOption', 'readingTypes'
    ],

    idProperty: 'name',
    associations: [
        {
            type: 'hasMany',
            model: 'Dxp.model.ReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ]
});