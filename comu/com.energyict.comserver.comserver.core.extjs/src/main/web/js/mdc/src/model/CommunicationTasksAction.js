Ext.define('Mdc.model.CommunicationTasksAction', {
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
