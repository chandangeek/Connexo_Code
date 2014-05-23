Ext.define('Isu.model.CommunicationTasksLogbooks', {
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
            name: 'obis',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mds/logbooktypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
