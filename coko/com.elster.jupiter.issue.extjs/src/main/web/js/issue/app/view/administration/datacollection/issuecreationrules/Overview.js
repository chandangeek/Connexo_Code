Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issuecreationrules.List',
        'Isu.view.administration.datacollection.issuecreationrules.Item'
    ],
    alias: 'widget.issue-creation-rules-overview',
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
                    title: 'Issue creation rules',
                    ui: 'large',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'issues-creation-rules-list',
                    margin: '0 15 20 0'
                },
                {
                    xtype: 'issue-creation-rules-item',
                    margin: '0 15 0 0'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);

        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            menu = this.getSideMenuCmp();

        menu.add({
            text: 'Issue creation rules',
            pressed: true,
            href: '#/issue-administration/issuecreationrules',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});