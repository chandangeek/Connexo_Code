Ext.define('Imt.usagepointlifecyclestates.store.UsagePointLifeCycleStates', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointlifecyclestates.model.UsagePointLifeCycleState',
    proxy: {
        type: 'rest',
        urlTpl: '/api/upl/lifecycle/{id}/states',
        reader: {
            type: 'json',
            root: 'states'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.usagePointLifeCycleId);
        }
    }
});