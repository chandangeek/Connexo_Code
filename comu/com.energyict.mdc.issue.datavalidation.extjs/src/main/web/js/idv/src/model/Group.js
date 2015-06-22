Ext.define('Idv.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
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
        url: '/api/isu/issues/groupedlist',
        reader: {
            type: 'json',
            root: 'issueGroups',
            totalProperty: 'totalCount'
        }
    }
});