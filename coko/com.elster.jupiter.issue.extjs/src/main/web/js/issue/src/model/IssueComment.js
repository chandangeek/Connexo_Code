Ext.define('Isu.model.IssueComment', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    fields: [
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
        url: '/api/idc/issue/{issue_id}/comments',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});