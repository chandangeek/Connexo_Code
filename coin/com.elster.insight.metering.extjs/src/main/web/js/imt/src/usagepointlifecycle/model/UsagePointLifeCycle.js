Ext.define('Imt.usagepointlifecycle.model.UsagePointLifeCycle', {
    extend: 'Uni.model.Version',
    fields: [
        'id',
        'name',
        'isDefault',
        {
            name: 'states',
            persist: false
        },
        {
            name: 'transitionsCount',
            persist: false
        },
        {
            name: 'obsolete',
            persist: false
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle',
        reader: {
            type: 'json'
        }
    }
});
