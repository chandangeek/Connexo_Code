Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issuecreationrules.List',
        'Isu.view.administration.datacollection.issuecreationrules.Item',
        'Uni.view.container.PreviewContainer'
    ],
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Issue creation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'issues-creation-rules-list'
                    },
                    emptyComponent: {
                        itemId: 'noRuleFound',
                        name: 'empty-text',
                        border: false,
                        hidden: true,
                        html: '<h3>No rule found</h3>',

                        items: [
                            {
                                xtype: 'label',
                                text: 'No issue creation rules have been created yet.'
                            },
                            {
                                xtype: 'label',
                                text: 'Possible steps:'
                            }
                        ],
                        bbar: {
                            padding: 0,
                            items: [
                                {
                                    itemId: 'createRule',
                                    text: 'Create rule',
                                    action: 'create'
                                }
                            ]
                        }
                    },
                    previewComponent: {
                        xtype: 'issue-creation-rules-item'
                    }
                }
            ]




            /*  {
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
             }*/
        }
    ]
});