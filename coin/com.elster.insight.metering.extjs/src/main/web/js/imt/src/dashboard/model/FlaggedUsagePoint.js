Ext.define('Imt.dashboard.model.FlaggedUsagePoint', {
    extend: 'Ext.data.Model',
    idProperty: 'name',
    fields: [
        'name',
        'displayServiceCategory',
        'displayMetrologyConfiguration',
        'displayType',
        'displayConnectionState',
        'creationDate',
        'flaggedDate',
        'comment',
        'state',
        'favorite',
        {
            name: 'parent',
            type: 'auto',
            defaultValue: null
        }
    ]
});