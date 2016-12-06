Ext.define('Imt.usagepointlifecycle.store.Stages', {
    extend: 'Ext.data.Store',
    model: 'Imt.model.IdWithName',
    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle/stages',
        reader: {
            type: 'json',
            root: 'stages'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});