Ext.define('Cfg.store.UsagePointGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.UsagePointGroup'
    ],
    model: 'Cfg.model.UsagePointGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/val/usagepointgroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'usagepointgroups'
        }
    }
});
