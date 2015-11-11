Ext.define('Imt.devicemanagement.view.DeviceSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.device-management-side-menu',
    router: null,
    title: Uni.I18n.translate('general.label.device', 'IMT', 'Device'),
    
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('general.label.overview', 'IMT', 'Overview'),
                itemId: 'device-overview-link',
                href: me.router.getRoute('usagepoints/device').buildUrl({mRID: me.mRID})
            }
        ];
        me.callParent(arguments);
    }
});
