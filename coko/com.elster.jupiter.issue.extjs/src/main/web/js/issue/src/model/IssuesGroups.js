Ext.define('Isu.model.IssuesGroups', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'reason',
            type: 'text'
        },
        {
            name: 'number',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issue/groupedlist',
        reader: {
            type: 'json',
            root: 'data',
            totalProperty: 'totalCount'
        }
    }
});