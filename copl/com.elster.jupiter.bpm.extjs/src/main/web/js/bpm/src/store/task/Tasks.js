Ext.define('Bpm.store.task.Tasks', {
    extend: 'Uni.data.store.Filterable',
    model: 'Bpm.model.task.Task',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/tasks',
        reader: {
            type: 'json',
            root: 'tasks'
        }
    },
    listeners : {
        beforeload: function (store, options) {
            var me = this,
                queryString = Uni.util.QueryString.getQueryStringValues(false),
                params = {};

            options.params = options.params || {};
            if (queryString.sort) {
                params.sort = queryString.sort;
            }
            Ext.apply(options.params, params);
        }
    }
});
