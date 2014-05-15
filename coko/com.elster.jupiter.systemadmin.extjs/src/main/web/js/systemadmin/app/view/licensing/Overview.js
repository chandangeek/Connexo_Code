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
                    title: 'Licenses',
                    ui: 'large'
                },
                {
                    xtype: 'licensing-list',
                    cls: 'license-overview-list'
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
            text: 'Licenses',
            pressed: true,
            href: '#/sysadministration/licensing/licenses',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});

