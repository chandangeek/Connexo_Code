Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',

    requires: [
        'Isu.view.administration.datacollection.issuecreationrules.List',
        'Isu.view.administration.datacollection.issuecreationrules.Item',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('administration.issueCreationRules.title', 'ISU', 'Issue creation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        itemId: 'creation-rules-list',
                        xtype: 'issues-creation-rules-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('administration.issueCreationRules.empty.title', 'ISU', 'No issue creation rules found'),
                        reasons: [
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item1', 'ISU', 'No issue creation rules have been defined yet.'),
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item2', 'ISU', 'No issue creation rules comply to the filter.')
                        ],
                        stepItems: [
                            {
                                itemId: 'createRule',
                                text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISU', 'Add rule'),
                                href: '#/administration/creationrules/add',
                                action: 'create'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'issue-creation-rules-item'
                    }
                }
            ]
        }
    ]
});