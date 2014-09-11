Ext.define('Isu.model.Actions', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'text'
        },
        {
            name: 'issueType',
            type: 'text'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/actions',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
