Ext.define('Isu.view.workspace.issues.DetailOverview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.workspace.issues.Form',
        'Isu.view.workspace.issues.comment.List'
    ],
    alias: 'widget.issue-detail-overview',
    title: 'Issue detail overview',

    content: [
        {
            ui: 'large',
            items: [
                {
                    itemId: 'detailedPage',
                    xtype: 'issue-form'
                },
                {
                    itemId: 'issue-comments',
                    xtype: 'issue-comments'
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'top',
                    defaultButtonUI: 'link',
                    rtl: false,
                    items: [
                        {
                            xtype: 'tbfill'
                        },
                        {
                            text: 'Previous',
                            action: 'prev'
                        },
                        {
                            text: 'Next',
                            action: 'next'
                        }
                    ]
                }
            ]
        }
    ]
});