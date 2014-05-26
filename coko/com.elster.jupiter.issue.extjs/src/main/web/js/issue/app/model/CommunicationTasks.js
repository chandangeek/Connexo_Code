Ext.define('Isu.model.CommunicationTasks', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'commands',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/cts/comtasks',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});