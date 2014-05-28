Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issuecreationrules.List',
        'Isu.view.administration.datacollection.issuecreationrules.Item'
    ],
    alias: 'widget.issue-creation-rules-overview',
    itemId: 'creation-rules-overview',
    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'sideMenu'
        }
    ],
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