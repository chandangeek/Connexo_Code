Ext.define('Imt.dashboard.store.FlaggedUsagePoints', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.dashboard.model.FlaggedUsagePoint'
    ],
    model: 'Imt.dashboard.model.FlaggedUsagePoint',
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/udr/favorites/usagepoints',
        pageParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }
});