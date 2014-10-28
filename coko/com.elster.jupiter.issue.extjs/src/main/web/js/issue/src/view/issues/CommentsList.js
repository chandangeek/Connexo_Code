Ext.define('Isu.view.issues.CommentsList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.issues.AddCommentForm',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.issue-comments',
    title: 'Comments',
    ui: 'medium',
    buttonAlign: 'left',
    items: [
        {
            xtype: 'no-items-found-panel',
            itemId: 'no-issue-comments',
            title: 'No comments found',
            reasons: [
                'No comments created yet on this issue'
            ],
            hidden: true
        },
        {
            xtype: 'dataview',
            itemId: 'issue-comments-view',
            title: 'User Images',
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '{[xindex > 1 ? "<hr>" : ""]}',
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
            xtype: 'issue-add-comment-form',
            itemId: 'issue-add-comment-form',
            hidden: true
        }
    ],

    buttons: [
        {
            itemId: 'issue-comments-add-comment-button',
            ui: 'action',
            text: 'Add comment',
            action: 'add'
        }
    ]
});