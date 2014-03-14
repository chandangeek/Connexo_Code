Ext.define('Isu.view.workspace.issues.DetailOverview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.workspace.issues.Detail',
        'Isu.view.workspace.issues.CommentsList'
    ],
    alias: 'widget.issue-detail-overview',
    title: 'Issue detail overview',

    content: [
        {
            xtype: 'issue-detail',
            margin: 10
        },
        {
            xtype: 'component',
            html: '<h3 class="isu-subheader">Comments</h3>',
            margin: '20 10 0 10'
        },
        {
            xtype: 'issue-comments',
            store: 'Isu.store.IssueComments',
            margin: 10
        },
        {
            xtype: 'container',
            margin: '0 10',
            items: [
                {
                    xtype: 'button',
                    text: 'Add comment',
                    action: 'addcomment'
                }
            ]
        },
        {
            xtype: 'form',
            hidden: true,
            bodyPadding: '0 15 15 15',
            items: [
                {
                    xtype: 'component',
                    html: '<h3 class="isu-subheader">Comment</h3>'
                },
                {
                    xtype: 'container',
                    layout : 'fit',
                    items: [
                        {
                            xtype: 'textareafield',
                            name: 'comment',
                            allowBlank: false,
                            msgTarget: 'under'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'button',
                            text: 'Add',
                            action: 'send'
                        },
                        {
                            xtype: 'button',
                            cls: 'isu-btn-link',
                            text: 'Cancel',
                            action: 'cancel'
                        }
                    ]
                }
            ]
        }
    ]
});