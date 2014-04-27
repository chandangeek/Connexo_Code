Ext.define('Isu.view.administration.datacollection.issueassignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issueassignmentrules.List'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'sideMenu'
        }
    ],

    content: {
        xtype: 'panel',
        ui: 'large',
        title: 'Issue assignment rules',
        items: {
            xtype: 'issues-assignment-rules-list'
        }
    },

    initComponent: function () {
        this.callParent(this);

        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            menu = this.getSideMenuCmp();

        menu.add({
            text: 'Issue assignment rules',
            pressed: true,
            href: '#/issue-administration/datacollection/issueassignmentrules',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});