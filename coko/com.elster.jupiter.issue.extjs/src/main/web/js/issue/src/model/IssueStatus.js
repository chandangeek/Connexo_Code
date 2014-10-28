Ext.define('Isu.model.IssueStatus', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'auto'},
        {name: 'name', type: 'string'},
        {name: 'allowForClosing', type: 'boolean'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/statuses',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});