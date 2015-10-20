Ext.define('Isu.model.IssueAction', {
    extend: 'Isu.model.Action',
    fields: [
        {name: 'issue', defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        reader: {
            type: 'json'
        },
        timeout: 300000
    }
});
