Ext.define('Isu.model.CommunicationTasksActions', {
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
        url: '/api/cts/comtasks/actions',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});