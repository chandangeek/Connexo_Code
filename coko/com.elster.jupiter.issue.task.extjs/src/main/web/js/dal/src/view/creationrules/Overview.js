/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.creationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',

    requires: [
        'Itk.view.creationrules.List',
        'Itk.view.creationrules.Item',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.issueCreationRules', 'ITK', 'Issue creation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        itemId: 'creation-rules-list',
                        xtype: 'issues-creation-rules-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('administration.issueCreationRules.empty.title', 'ITK', 'No issue creation rules found'),
                        reasons: [
                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item1', 'ITK', 'No issue creation rules have been defined yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'createRule',
                                text: Uni.I18n.translate('administration.issueCreationRules.add', 'ITK', 'Add rule'),
                                privileges:Itk.privileges.Issue.createIssueRule,
                                href: '#/administration/issuecreationrules/add',
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