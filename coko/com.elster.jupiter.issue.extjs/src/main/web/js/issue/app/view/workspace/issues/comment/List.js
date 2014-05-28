Ext.define('Isu.view.workspace.issues.comment.List', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.Action',
        'Isu.store.IssueComments',
        'Isu.view.workspace.issues.comment.AddForm'
    ],

    itemId: 'Comment',
    alias: 'widget.issue-comments',
    title: 'Comments',
    emptyText: 'There are no comments yet on this issue',
    ui: 'medium',
    buttonAlign: 'left',
    items: [
        {
            itemId: 'dataview',
            xtype: 'dataview',
            title: 'User Images',
            emptyText: 'No comments to display',
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '<p><span class="isu-icon-USER"></span><b>{author.name}</b> added a comment - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',
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
            itemId: 'comment-add-form',
            xtype: 'comment-add-form',
            hidden: true
        }
    ],

    afterRender: function(){
        var queryStrings = Ext.util.History.getToken(),
            index = queryStrings.lastIndexOf("/"),
            urlStr = queryStrings.substring(index + 1);
        if (urlStr == 'addcomment') {
            this.child('toolbar').child('#Add').hide();
            this.child('#comment-add-form').show()
        }
        this.callParent(arguments)
    },

    buttons: [
        {
            itemId: 'Add',
            ui: 'action',
            text: 'Add comment',
            action: 'add'
        }
    ]
});