Ext.define('Isu.model.IssueComments', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'author',
            type: 'auto'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/isu/issue/{issue_id}/comments',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});