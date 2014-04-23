Ext.define('Isu.store.IssueGrouping', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueGrouping',
    data: [
        {
            value: 'none',
            display: 'None'
        },
        {
            value: 'reason',
            display: 'Reason'
        }
    ]
});