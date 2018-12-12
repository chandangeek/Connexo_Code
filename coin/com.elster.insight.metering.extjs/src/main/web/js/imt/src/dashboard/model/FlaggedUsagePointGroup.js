Ext.define('Imt.dashboard.model.FlaggedUsagePointGroup', {
    extend: 'Ext.data.Model',
    idProperty: 'name',
    fields: [
        'id',
        'name',
        'dynamic',
        'flaggedDate',
        'comment',
        'favorite',
        {
            name: 'parent',
            type: 'auto',
            defaultValue: null
        }
    ]
});