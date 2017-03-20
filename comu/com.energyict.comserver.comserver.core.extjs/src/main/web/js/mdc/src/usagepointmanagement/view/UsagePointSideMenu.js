/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usage-point-management-side-menu',
    router: null,
    title: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
    usagePointId: null,

    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                xtype: 'menu',
                items: [
                    {
                        text: me.usagePointId,
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
                        title: Uni.I18n.translate('device.dataSources', 'MDC', 'Data sources'),
                        xtype: 'menu',
                        expanded: true,
                        items: [
                            {
                                text: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
                                itemId: 'usage-point-channels-link',
                                href: me.router.getRoute('usagepoints/usagepoint/channels').buildUrl()
                            },
                            {
                                text: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                                itemId: 'registersLink',
                                href: me.router.getRoute('usagepoints/usagepoint/registers').buildUrl()
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
