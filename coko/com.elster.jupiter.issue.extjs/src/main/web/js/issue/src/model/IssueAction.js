Ext.define('Isu.model.IssueAction', {
    extend: 'Isu.model.Action',
    proxy: {
        type: 'rest',
        reader: {
            type: 'json'
        }
    }
});
