Ext.define('Sam.view.licensing.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Sam.view.licensing.List',
        'Sam.view.licensing.Details'
    ],
    alias: 'widget.licensing-overview',

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
                    title: 'Licenses',
                    ui: 'large'
                },
                {
                    itemId: 'licenses-list',
                    xtype: 'licensing-list',
                    cls: 'license-overview-list'
                },
                {
                    itemId: 'licenses-details',
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
            itemId: 'navEl',
            text: 'Licenses',
            pressed: true,
            href: '#/administration/licensing/licenses',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});

