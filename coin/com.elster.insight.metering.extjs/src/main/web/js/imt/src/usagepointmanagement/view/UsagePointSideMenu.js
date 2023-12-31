/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.UsagePointSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    requires: [
        'Imt.util.IconsMap',
        'Imt.util.ServiceCategoryTranslations'
    ],
    alias: 'widget.usage-point-management-side-menu',
    uniqueMenuId: 'usage-point-management-side-menu',
    router: null,
    usagePoint: null,
    objectType: Uni.I18n.translate('general.usage point', 'IMT', 'Usage point'),

    initComponent: function () {
        var me = this,
            usagePoint = me.usagePoint,
            purposes = usagePoint.get('metrologyConfiguration_purposes'),
            metrologyConfiguration = usagePoint.get('metrologyConfiguration'),
            iconStyle,
            serviceCategory,
            connectionState;

        me.title = me.usagePoint ? me.usagePoint.get('name') : me.router.arguments.usagePointId;

        me.menuItems = [
            {
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('general.overview', 'IMT', 'Overview'),
                        itemId: 'usage-point-overview-link',
                        href: me.router.getRoute('usagepoints/view').buildUrl()
                    },
                    {
                        text: Uni.I18n.translate('general.usagePointAttributes', 'IMT', 'Usage point attributes'),
                        itemId: 'usage-point-attributes-link',
                        href: me.router.getRoute('usagepoints/view/attributes').buildUrl()
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
                    },
                    {
                        text: Uni.I18n.translate('general.servicecalls', 'IMT', 'Service calls'),
                        itemId: 'usage-point-service-calls-link',
                        privileges: Scs.privileges.ServiceCall.view,
                        href: me.router.getRoute('usagepoints/view/servicecalls').buildUrl()
                    }
                ]
            },
            {
                title: Uni.I18n.translate('general.configuration', 'IMT', 'Configuration'),
                items: [
                    {
                        text: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                        itemId: 'usage-point-metrology-configuration-link',                        
                        href: me.router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl()
                    },
                    {
                        text: Uni.I18n.translate('general.label.calendars', 'IMT', 'Calendars'),
                        itemId: 'usage-point-calendar-configuration-link',
                        //privileges: Imt.privileges.UsagePoint.adminCalendars,
                        href: me.router.getRoute('usagepoints/view/calendars').buildUrl()
                    }
                ]
            }
        ];

        if (purposes && purposes.length) {
            var items = [];
            purposes.map(function(purpose) {
                if (purpose.active) {
                    items.push({
                        text: purpose.name,
                        privileges: Imt.privileges.MetrologyConfig.view,
                        htmlEncode: false,
                        itemId: 'usage-point-pupose-' + purpose.id,
                        href: me.router.getRoute('usagepoints/view/purpose').buildUrl({purposeId: purpose.id})
                    });
                }
            });

            if (items.length) {
                me.menuItems.push({
                    title: Uni.I18n.translate('usagepoint.menu.purposes', 'IMT', 'Purposes'),
                    privileges: Imt.privileges.MetrologyConfig.view,
                    items: items
                })
            }
        }

        if (usagePoint) {
            iconStyle = "color: #686868;font-size: 16px;";
            serviceCategory = usagePoint.get('serviceCategory');
            connectionState = usagePoint.get('connectionState');
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