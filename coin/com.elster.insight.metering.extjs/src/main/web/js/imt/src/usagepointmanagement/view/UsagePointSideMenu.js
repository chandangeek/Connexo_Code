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
            usagePoint = me.usagePoint,
            purposes = usagePoint.get('purposes'),
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
                        privileges: Imt.privileges.UsagePoint.view,
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
                if (purpose.get('active')) {
                    var validationInfo = purpose.get('validationInfo'),
                        status = purpose.get('status'),
                        icon = '';

                    switch (validationInfo.allDataValidated) {
                        case true:
                            if (!validationInfo.hasSuspects) {
                                icon = '<span class="icon-checkmark-circle" style="color: #33CC33; margin-left: 10px" data-qtip="'
                                    + Uni.I18n.translate('usagepoint.purpose.validation.icon.noSuspects.qtip', 'IMT', 'Validated, no suspects')
                                    + '"></span>';
                            } else {
                                icon = '<span class="icon-warning" style="color: #eb5642; margin-left: 10px" data-qtip="'
                                    + Uni.I18n.translate('usagepoint.purpose.validation.icon.hasSuspects.qtip', 'IMT', 'Suspects')
                                    + '"></span>';
                            }
                            break;
                        case false:
                            icon = '<span class="icon-question4" style="color: #686868; margin-left: 10px" data-qtip="'
                                + Uni.I18n.translate('usagepoint.purpose.validation.icon.notValidated.qtip', 'IMT', 'Not validated')
                                + '"></span>';
                            break;

                    }

                    items.push({
                        text: purpose.get('name') + icon,
                        privileges: Imt.privileges.MetrologyConfig.view,
                        htmlEncode: false,
                        itemId: 'usage-point-pupose-' + purpose.getId(),
                        href: me.router.getRoute('usagepoints/view/purpose').buildUrl({purposeId: purpose.getId()})
                    });
                }
            });

            if (items.length) {
                me.menuItems.push({
                    title: Uni.I18n.translate('usagepoint.menu.data', 'IMT', 'Data'),
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