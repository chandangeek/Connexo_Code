Ext.define('Dxp.model.StandardDataSelector', {
    extend: 'Ext.data.Model',


    fields: [
        'deviceGroup',
        'exportPeriod',
        'exportComplete',
        'exportUpdate',
        'updateWindow',
        'updatePeriod',
        'exportWindow',
        'validatedDataOption',
        'readingTypes',
        'exportContinuousData'
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