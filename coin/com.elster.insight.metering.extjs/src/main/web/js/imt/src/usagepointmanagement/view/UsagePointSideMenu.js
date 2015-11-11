Ext.define('Imt.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('usagepoint.label.usagepoint', 'IMT', 'Usage point'),
    
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('general.label.overview', 'IMT', 'Overview'),
                itemId: 'usage-point-overview-link',
                href: me.router.getRoute('usagepoints/view').buildUrl({mRID: me.mRID})
            },
            {
                title: 'Data sources',
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('usagepoint.label.channel.list', 'IMT', 'Channels'),
                        itemId: 'usage-point-channels-link',
                        href: me.router.getRoute('usagepoints/view/channels').buildUrl({mRID: me.mRID})
                    },
                    {
                        text: Uni.I18n.translate('usagepoint.label.register.list', 'IMT', 'Registers'),
                        itemId: 'usage-point-registers-link',
                        href: me.router.getRoute('usagepoints/view/registers').buildUrl({mRID: me.mRID})
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
