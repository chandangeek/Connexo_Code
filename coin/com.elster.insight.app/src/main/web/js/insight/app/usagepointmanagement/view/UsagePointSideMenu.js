Ext.define('InsightApp.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('usagePointManagement.usagePointLabel', 'MDC', 'Usage point'),
    
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('usagePointManagement.overview', 'MDC', 'Overview'),
                itemId: 'usage-point-overview-link',
                href: me.router.getRoute('insight/viewusagepoints').buildUrl({mRID: me.mRID})
            }
        ];
        me.callParent(arguments);
    }
});
