Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issuecreationrules.List',
        'Isu.view.administration.datacollection.issuecreationrules.Item'
    ],
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',
    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    itemId: 'pageTitle',
                    title: 'Issue creation rules',
                    ui: 'large',
                    margin: '0 0 20 0'
                },
                {
                    itemId: 'creation-rules-list',
                    xtype: 'issues-creation-rules-list',
                    margin: '0 15 20 0'
                },
                {
                    itemId: 'creation-rules-item',
                    xtype: 'issue-creation-rules-item',
                    margin: '0 15 0 0'
                }
            ]
        }
    ]
});

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
            title: Uni.I18n.translate('administration.issueCreationRules.title', 'ISE', 'Issue creation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        itemId: 'creation-rules-list',
                        xtype: 'issues-creation-rules-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('administration.issueCreationRules.empty.title', 'ISE', 'No issue creation rules found'),
                        reasons: [
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item1', 'ISE', 'No issue creation rules have been defined yet.'),
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item2', 'ISE', 'No issue creation rules comply to the filter.')
                        ],
                        stepItems: [
                            {
                                itemId: 'createRule',
                                text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISE', 'Create rule'),
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