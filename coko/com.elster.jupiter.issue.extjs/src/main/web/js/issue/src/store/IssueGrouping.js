Ext.define('Isu.store.IssueGrouping', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'value',
            type: 'string'
        }
    ],
    data: [
        {
            id: 'none',
            value: 'None'
        },
        {
            id: 'reason',
            value: 'Reason'
        }
    ]
});