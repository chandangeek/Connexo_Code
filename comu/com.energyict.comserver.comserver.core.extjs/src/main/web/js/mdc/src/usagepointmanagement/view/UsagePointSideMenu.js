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
            },
            {
                text: Uni.I18n.translate('devicemenu.processes', 'MDC', 'Processes'),
                privileges: Mdc.privileges.Device.deviceProcesses,
                itemId: 'usage-point-processes-link',
                href: me.router.getRoute('usagepoints/usagepoint/processes').buildUrl({mRID: me.mRID})
            }
        ];
        me.callParent(arguments);
    }
});
