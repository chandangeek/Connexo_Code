Ext.define('Mtr.model.Status', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'dateTime', mapping: 'dateTime.ms' },
        'reason',
        'remark',
        'value'
    ],
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