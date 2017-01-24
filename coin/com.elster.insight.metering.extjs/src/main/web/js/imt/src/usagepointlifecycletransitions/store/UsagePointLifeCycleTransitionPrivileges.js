Ext.define('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionPrivileges', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransitionPrivilege',
    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle/privileges',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});
