Ext.define('Isu.view.issues.CommentsList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.issues.AddCommentForm',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Isu.privileges.Issue'
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
            stepItems: [
                {
                    itemId: 'empty-message-add-comment-button',
                    text: Uni.I18n.translate('general.addComment','ISU','Add comment'),
                    privileges: Isu.privileges.Issue.comment,
                    action: 'add'
                }
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
                '<p><tpl for="splittedComments">',
                '{.}</br>',
                '</tpl></p>',
                '</tpl>',
                {
                    formatCreationDate: function (date) {
                        date = Ext.isDate(date) ? date : new Date(date);
                        return Uni.DateTime.formatDateTimeLong(date);
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
            text: Uni.I18n.translate('general.addComment','ISU','Add comment'),
            hidden: true,
            action: 'add'
        }
    ]
});