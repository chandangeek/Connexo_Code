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
        'Uni.view.container.PreviewContainer'
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
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        minHeight: 20,
                        items: [
                            {
                                xtype: 'image',
                                margin: '0 10 0 0',
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('administration.issueCreationRules.empty.title', 'ISE', 'No issue creation rules found') + '</b><br>' +
                                            Uni.I18n.translate('administration.issueCreationRules.empty.detail', 'ISE', 'There are no issue creation rules. This could be because:') + '<lv><li>' +
                                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item1', 'ISE', 'No issue creation rules have been defined yet.') + '</li><li>' +
                                            Uni.I18n.translate('administration.issueCreationRules.empty.list.item2', 'ISE', 'No issue creation rules comply to the filter.') + '</li></lv><br>' +
                                            Uni.I18n.translate('administration.issueCreationRules.empty.steps', 'ISE', 'Possible steps:')
                                    },
                                    {
                                        itemId: 'createRule',
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISE', 'Create rule'),
                                        href: '#/administration/issue/creationrules/create',
                                        action: 'create'
                                    }
                                ]
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