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
                {xtype: 'issue-form'},
                {xtype: 'issue-comments'}
            ]
        }
    ]
});