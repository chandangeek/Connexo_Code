Ext.define('Isu.view.administration.datacollection.licensing.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.licensing.List',
        'Isu.view.administration.datacollection.licensing.Details'
    ],
    alias: 'widget.administration-licensing-overview',

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
                    html: '<h1>Licenses</h1>',
                    margin: '0 0 10 0'
                },
                {
                    xtype: 'licensing-list',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'licensing-details'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        this.initMenu();
    },

    initMenu: function () {
        var menu = this.getSideMenuCmp();

        menu.add({
            text: 'Licensing',
            pressed: true,
            href: '#/administration/datacollection/licensing',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});
