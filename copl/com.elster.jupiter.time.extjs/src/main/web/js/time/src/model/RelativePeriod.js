Ext.define('Tme.model.RelativePeriod', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'categories', type: 'string', useNull: true},
        'from',
        'to',
        'categories'

    ],
    idProperty: 'id',
    associations: [
        {
            name: 'from'
            //TODO define
        },
        {
            name: 'to'
            //TODO define
        },
        {name: 'categories', type: 'hasMany', model: 'Mdc.model.Categories', associationKey: 'categories',
            getterName: 'getCategories', setterName: 'setCategories', foreignKey: 'relativePeriodCategories'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/tmr/relativeperiods'
    }
});
