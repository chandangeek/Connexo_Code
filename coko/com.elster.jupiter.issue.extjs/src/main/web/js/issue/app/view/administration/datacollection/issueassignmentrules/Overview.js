Ext.define('Isu.view.administration.datacollection.issueassignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issueassignmentrules.List'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    side: [
        {
            itemId: 'sideMenu',
            xtype: 'navigationSubMenu'
        }
    ],

    content: {
        itemId: 'title',
        xtype: 'panel',
        ui: 'large',
        title: 'Issue assignment rules',
        items: {
            itemId: 'issues-rules-list',
            xtype: 'issues-assignment-rules-list'
        }
    },
});