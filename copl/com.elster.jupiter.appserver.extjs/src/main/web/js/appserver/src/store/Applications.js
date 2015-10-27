Ext.define('Apr.store.Applications', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Application',
    autoLoad: false,
    data: {
        applications: [
            {
                application: 'app1'
            },
            {
                application: 'app2'
            }
        ]
    },


    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'applications'
        }
    }

    //proxy: {
    //    type: 'rest',
    //    url: '/api/apr/importdirs',
    //    reader: {
    //        type: 'json',
    //        root: 'directories'
    //    }
    //}
});