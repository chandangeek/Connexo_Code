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
        },
        {
            name: 'splittedComments',
            persist: false,
            mapping: function (data) {
                if (data.comment) {
                   return data.comment.split('\n');
                } else {
                    return null;
                }
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/idc/issue/{issue_id}/comments',
        reader: {
            type: 'json',
            root: 'comments'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});