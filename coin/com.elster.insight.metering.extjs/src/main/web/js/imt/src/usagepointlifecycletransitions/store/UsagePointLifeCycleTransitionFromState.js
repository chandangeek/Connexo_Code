Ext.define('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionFromState', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransitionState',
    proxy: {
        type: 'rest',
        urlTpl: '/api/upl/lifecycle/{id}/states',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'states'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.usagePointLifeCycleId);
        }
    }
});