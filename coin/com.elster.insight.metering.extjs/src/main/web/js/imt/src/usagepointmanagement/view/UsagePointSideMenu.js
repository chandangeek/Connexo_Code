Ext.define('Imt.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('usagePointManagement.usagePointLabel', 'IMT', 'Usage point'),
    
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('usagePointManagement.overview', 'IMT', 'Overview'),
                itemId: 'usage-point-overview-link',
                href: me.router.getRoute('administration/usagepoint').buildUrl({mRID: me.mRID})
            },
            {
                title: 'Data sources',
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('usagePointManagement.channelList', 'IMT', 'Channel List'),
                        itemId: 'usage-point-channels-link',
                        href: me.router.getRoute('administration/usagepoint/channels').buildUrl({mRID: me.mRID})
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
