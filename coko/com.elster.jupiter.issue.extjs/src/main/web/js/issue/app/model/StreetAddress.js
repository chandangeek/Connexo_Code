Ext.define('Mtr.model.StreetAddress', {
    extend: 'Ext.data.Model',
    associations: [
        {
            type: 'hasOne',
            model: 'Mtr.model.Status',
            associationKey: 'status'
        },
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