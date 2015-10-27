Ext.define('Apr.store.Tasks', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Task',
    autoLoad: false,
    data: {
        tasks: [
            {
                name: 'test',
                application: 'test',
                queue: 'test',
                queueStatus: 'test'
            }
        ]
    },


    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'tasks'
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