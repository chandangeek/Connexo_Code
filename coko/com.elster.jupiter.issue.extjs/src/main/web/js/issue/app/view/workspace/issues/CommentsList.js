Ext.define('Isu.view.workspace.issues.CommentsList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.Action',
        'Isu.store.IssueComments'
    ],

    alias: 'widget.issue-comments',
    title: 'Comments',
    emptyText: 'There are no comments yet on this issue',
    ui: 'medium',

    items: [
        {
            xtype: 'dataview',
            title: 'User Images',
            emptyText: 'No comments to display',
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '<p><span class="isu-icon-USER"></span><b>{comment}</b> added a comment - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',
                '<p>{comment}</p>',
                '</tpl>',
                {
                    formatCreationDate: function (date) {
                        date = Ext.isDate(date) ? date : new Date(date);
                        return Ext.Date.format(date, 'M d, Y (H:i)');
                    }
                }
            ),
            header: 'Name',
            dataIndex: 'name'
        },
        {
            hidden: true,
            title: 'Comment',
            layout: 'fit',
            items: {
                xtype: 'textareafield',
                name: 'comment'
            },
            buttons: [
                {
                    text: 'Add',
                    action: 'send',
                    disabled: true
                },
                {
                    text: 'Cancel',
                    action: 'cancel'
                }
            ]
        }
    ],

    buttons: [{
        text: 'Add comment',
        action: 'add'
    }]
});