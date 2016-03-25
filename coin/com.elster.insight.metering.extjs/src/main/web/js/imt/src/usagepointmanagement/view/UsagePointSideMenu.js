Ext.define('Imt.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    requires: [
        'Imt.util.IconsMap',
        'Imt.util.ServiceCategoryTranslations'
    ],
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    usagePoint: null,
    
    initComponent: function () {
        var me = this,
            iconStyle,
            serviceCategory,
            connectionState;

        me.title = Ext.htmlEncode(me.usagePoint ? me.usagePoint.get('mRID') : me.router.arguments.mRID);

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
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

        if (me.usagePoint) {
            iconStyle = "color: #686868;font-size: 16px;";
            serviceCategory = me.usagePoint.get('serviceCategory');
            connectionState = me.usagePoint.get('connectionState');
            me.tools = [];
            if (serviceCategory) {
                me.tools.push({
                    xtype: 'component',
                    html: '<span class="'
                    + Imt.util.IconsMap.getCls(serviceCategory)
                    + '" style="' + iconStyle + '" data-qtip="'
                    + Imt.util.ServiceCategoryTranslations.getTranslation(serviceCategory)
                    + '"></span>'
                });
            }
            if (Ext.isObject(connectionState)) {
                me.tools.push({
                    xtype: 'component',
                    html: '<span class="'
                    + Imt.util.IconsMap.getCls(connectionState.id)
                    + '" style="' + iconStyle + '" data-qtip="'
                    + connectionState.name
                    + '"></span>'
                });
            }
        }

        me.callParent(arguments);
    }
});