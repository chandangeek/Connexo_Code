Ext.define('Isu.model.CommunicationTasksRegisters', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number'},
        {name: 'name', type: 'string'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/dtc/registergroups',
        reader: {
            type: 'json',
            root: 'registerGroups'
        }
    }
});
