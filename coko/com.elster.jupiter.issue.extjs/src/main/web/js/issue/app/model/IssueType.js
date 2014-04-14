Ext.define('Isu.model.IssueType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'text'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issue/types',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});