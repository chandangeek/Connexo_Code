Ext.define('Isu.view.workspace.issues.DetailOverview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.workspace.issues.comment.List'
    ],
    alias: 'widget.issue-detail-overview',
    title: 'Issue detail overview',

    content: [
        {
            ui: 'large',
            items: [
                {   itemId: 'issue-form',
                    xtype: 'issue-form'},
                {   itemId: 'issue-comments',
                    xtype: 'issue-comments'}
            ]
        }
    ]
});