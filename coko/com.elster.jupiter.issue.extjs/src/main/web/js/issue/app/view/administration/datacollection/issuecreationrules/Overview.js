Ext.define('Isu.view.administration.datacollection.issuecreationrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issuecreationrules.List'
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
                    html: '<h1>Issue creation rules</h1>',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'issues-creation-rules-list',
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
            text: 'Issue creation rules',
            pressed: true,
            href: '#/administration/datacollection/issuecreationrules',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});