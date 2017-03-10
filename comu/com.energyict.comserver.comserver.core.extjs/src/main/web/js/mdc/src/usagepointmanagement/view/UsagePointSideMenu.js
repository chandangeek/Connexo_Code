/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    objectType: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
    usagePointId: null,

    initComponent: function () {
        var me = this;
        me.title = me.usagePointId ||  Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point');
        me.menuItems = [
            {
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                        itemId: 'usage-point-overview-link',
                        href: me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: encodeURIComponent(me.usagePointId)})
                    },
                    {
                        text: Uni.I18n.translate('general.history', 'MDC', 'History'),
                        itemId: 'usage-point-history-link',
                        privileges: Mdc.privileges.UsagePoint.canView(),
                        href: me.router.getRoute('usagepoints/usagepoint/history').buildUrl()
                    },
                    {
                        text: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
                        itemId: 'usage-point-channels-link',
                        href: me.router.getRoute('usagepoints/usagepoint/channels').buildUrl()
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
