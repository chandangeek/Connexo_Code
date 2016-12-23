Ext.define('Imt.usagepointlifecycle.store.UsagePointLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointlifecycle.model.UsagePointLifeCycle',
    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle',        
        reader: {
            type: 'json',
            root: 'lifeCycles'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});