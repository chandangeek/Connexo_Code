Ext.define('Imt.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('usagepoint.label.usagepoint', 'IMT', 'Usage point'),
    
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: me.router.arguments.mRID,
                itemId: 'usage-point-overview-link',
                href: me.router.getRoute('usagepoints/view').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.history', 'IMT', 'History'),
                itemId: 'usage-point-history-link',
                href: me.router.getRoute('usagepoints/view/history').buildUrl()
            },
            {
                text: Uni.I18n.translate('usagepoint.processes', 'IMT', 'Processes'),
                privileges: Dbp.privileges.DeviceProcesses.allPrivileges,
                itemId: 'usage-point-processes-link',
                href: me.router.getRoute('usagepoints/view/processes').buildUrl()
            }
        ];
        me.callParent(arguments);
    }
});