Ext.define('Mdc.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),

    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                xtype: 'menu',
                items: [
                    {
                        text: me.mRID,
                        itemId: 'usage-point-overview-link',
                        href: me.router.getRoute('usagepoints/usagepoint').buildUrl({mRID: me.mRID})
                    },
                    {
                        text: Uni.I18n.translate('general.history', 'MDC', 'History'),
                        itemId: 'usage-point-history-link',
                        privileges: Mdc.privileges.UsagePoint.canView(),
                        href: me.router.getRoute('usagepoints/usagepoint/history').buildUrl()
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
