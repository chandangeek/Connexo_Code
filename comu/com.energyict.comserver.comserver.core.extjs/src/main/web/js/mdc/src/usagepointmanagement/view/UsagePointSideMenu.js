Ext.define('Mdc.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),

    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                itemId: 'usage-point-overview-link',
                href: me.router.getRoute('usagepoints/usagepoint').buildUrl({mRID: me.mRID})
            }
        ];
        me.callParent(arguments);
    }
});
