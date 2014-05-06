Ext.define('Isu.model.CommunicationTasksCategories', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mds/commtasks/categories',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});