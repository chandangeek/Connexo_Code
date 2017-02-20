Ext.define('Imt.dashboard.store.FlaggedUsagePointGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.dashboard.model.FlaggedUsagePointGroup'
    ],
    model: 'Imt.dashboard.model.FlaggedUsagePointGroup',
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/udr/favorites/usagepointgroups',
        pageParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            root: 'usagePointGroups'
        }
    }
});