Ext.define('Sam.view.licensing.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.menu.SideMenu',
        'Sam.view.licensing.List',
        'Sam.view.licensing.Details'
    ],
    alias: 'widget.licensing-overview',

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
        var me = this;
        me.side = [
            {
                ui: 'medium',
                items: {
                    xtype: 'uni-view-menu-side',
                    title: Uni.I18n.translate('licensing.sidemenu.title', 'SAM', 'Licensing'),
                    itemId: 'sideMenu',
                    menuItems: [
                        {
                            itemId: 'navEl',
                            text: Uni.I18n.translate('licensing.sidemenu.licenses', 'SAM', 'Licenses'),
                            href: '#/administration/licensing/licenses'
                        }
                    ]
                }}
        ];
        me.callParent(this);
    }

});

