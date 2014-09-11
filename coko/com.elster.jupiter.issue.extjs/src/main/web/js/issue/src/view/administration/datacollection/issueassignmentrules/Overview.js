Ext.define('Isu.view.administration.datacollection.issueassignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issueassignmentrules.List',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    content: {
        itemId: 'title',
        xtype: 'panel',
        ui: 'large',
        title: Uni.I18n.translate('issue.administration.assignment', 'ISE', 'Issue assignment rules'),
        items: {
            itemId: 'issues-rules-list',
            xtype: 'preview-container',
            grid: {
                xtype: 'issues-assignment-rules-list'
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('issueAssignment.empty.title', 'ISE', 'No issue assignment rules found'),
                reasons: [
                    Uni.I18n.translate('issueAssignment.empty.list.item', 'ISE', 'No issue assignment rules have been defined yet.')
                ]
            },
            previewComponent: null
        }
    }
});