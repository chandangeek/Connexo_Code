Ext.define('Isu.view.administration.datacollection.issueautomaticcreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issueautomaticcreationrules.List'
    ],
    alias: 'widget.issue-autocreation-rules-overview',
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
                    html: '<h1>Issue automatic creation rules</h1>',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'issues-autocreation-rules-list',
                    margin: '0 0 20 0'
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
            text: 'Issue automatic creation rules',
            pressed: true,
            href: '#/administration/datacollection/issueautomaticcreationrules',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});