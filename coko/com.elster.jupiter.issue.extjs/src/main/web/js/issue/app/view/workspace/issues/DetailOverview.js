Ext.define('Isu.view.workspace.issues.DetailOverview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.workspace.issues.CommentsList'
    ],
    alias: 'widget.issue-detail-overview',
    title: 'Issue detail overview',
    ui: 'large',

    content: [
        {xtype: 'issue-form'},
//        {
//            xtype: 'component',
//            html: '<h3 class="isu-subheader"></h3>',
//            margin: '20 10 0 10'
//        },
        {
            xtype: 'issue-comments',
            store: 'Isu.store.IssueComments'
        }

//        {
//            xtype: 'container',
//            margin: '0 10',
//
//        },
//        {
//            xtype: 'form',
//            hidden: true,
//            bodyPadding: '0 15 15 15',
//            items: [
//                {
//                    xtype: 'component',
//                    html: '<h3 class="isu-subheader">Comment</h3>'
//                },
//                {
//                    xtype: 'container',
//                    layout : 'fit',
//                    items: [
//                        {
//                            xtype: 'textareafield',
//                            name: 'comment'
//                        }
//                    ]
//                },
//                {
//                    xtype: 'container',
//                    items: [
//                        {
//                            xtype: 'button',
//                            text: 'Add',
//                            action: 'send',
//                            disabled: true
//                        },
//                        {
//                            xtype: 'button',
//                            cls: 'isu-btn-link',
//                            text: 'Cancel',
//                            action: 'cancel'
//                        }
//                    ]
//                }
//            ]
//        }
    ]
});