Ext.define('Imt.dashboard.store.FavoriteUsagePointGroups', {
    extend: 'Ext.data.Store',
    storeId: 'FavoriteUsagePointGroups',
    requires: ['Imt.dashboard.model.UsagePointGroup'],
    model: 'Imt.dashboard.model.UsagePointGroup',

    proxy: {
        type: 'ajax',
        url: '../../api/udr/favorites/usagepointgroups?includeAllGroups=true',
        reader: {
            type: 'json',
            root: 'usagePointGroups'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});