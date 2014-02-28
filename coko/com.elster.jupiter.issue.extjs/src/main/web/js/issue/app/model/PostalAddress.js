Ext.define('Mtr.model.PostalAddress', {
    extend: 'Ext.data.Model',
    fields: [
        'postalCode',
        'poBox'
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Mtr.model.StreetDetail',
            associationKey: 'streetDetail'
        },
        {
            type: 'hasOne',
            model: 'Mtr.model.TownDetail',
            associationKey: 'townDetail'
        }
    ]
});