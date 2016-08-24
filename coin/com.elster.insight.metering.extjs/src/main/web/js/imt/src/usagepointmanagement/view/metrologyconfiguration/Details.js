Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-metrology-configuration-details',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Uni.util.FormEmptyMessage',
        'Imt.usagepointmanagement.store.metrologyconfiguration.MeterRoles',
        'Imt.usagepointmanagement.store.metrologyconfiguration.Purposes',
        'Imt.usagepointmanagement.view.metrologyconfiguration.MeterRolesGrid',
        'Imt.usagepointmanagement.view.metrologyconfiguration.PurposesGrid',
        'Imt.usagepointmanagement.view.metrologyconfiguration.PurposesPreview'
    ],
    router: null,
    usagePoint: null,
    meterRolesAvailable: false,
    initComponent: function () {
        var me = this,
            meterRolesStore = Ext.create('Imt.usagepointmanagement.store.metrologyconfiguration.MeterRoles', {data: me.usagePoint.get('metrologyConfiguration_meterRoles')}),
            purposesStore = Ext.create('Imt.usagepointmanagement.store.metrologyconfiguration.Purposes', {data: me.usagePoint.get('metrologyConfiguration_purposes')});

        me.content = [
            {
                title: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                ui: 'large',
                flex: 1,
                itemId: 'metrology-configuration-details-main-panel',
                tools: [
                    {
                        xtype: 'uni-button-action',
                        margin: '5 0 0 0',
                        itemId: 'metrology-configuration-details-top-actions-button',
                        privileges: Imt.privileges.MetrologyConfig.canAdministrate,
                        menu: {
                            xtype: 'menu',
                            itemId: 'metrology-configuration-details-actions-menu',
                            router: me.router
                        },
                        hidden: true
                    }
                ],
                items: [
                    {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('general.noMetrologyConfiguration', 'IMT', 'No metrology configuration'),
                        reasons: [
                            Uni.I18n.translate('usagePoint.metrologyConfiguration.empty.reason', 'IMT', 'No metrology configuration has been defined for this usage point yet')
                        ],
                        itemId: 'no-metrology-configuration-panel',
                        stepItems: [
                            {
                                text: Uni.I18n.translate('usagePoint.metrologyConfiguration.define', 'IMT', 'Define metrology configuration'),
                                privileges: Imt.privileges.UsagePoint.canAdministrate,
                                href: me.router.getRoute('usagepoints/view/definemetrology').buildUrl(),
                                action: 'define',
                                itemId: 'define-metrology-configuration'
                            }
                        ],
                        hidden: me.usagePoint.get('metrologyConfiguration')
                    },
                    {
                        xtype: 'form',
                        itemId: 'metrology-configuration-details-form',
                        hidden: !me.usagePoint.get('metrologyConfiguration'),
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                name: 'metrologyConfiguration_status',
                                fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                                htmlEncode: false,
                                itemId: 'metrology-configuration-details-status',
                                renderer: function () {
                                    var status = me.usagePoint.get('metrologyConfiguration_status'),
                                        icon = '&nbsp;&nbsp;<i class="icon ' + (status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle2') + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                                            + status.name
                                            + '"></i>';
                                    return status.name + icon;
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                                itemId: 'metrology-configuration-details-name',
                                renderer: function () {
                                    var url = '',
                                        result = '',
                                        name = me.usagePoint.get('metrologyConfiguration_name'),
                                        from = Uni.I18n.translate('metrologyConfigurationDetails.from', 'IMT', 'from'),
                                        activationTime = Uni.DateTime.formatDateTimeLong(me.usagePoint.get('metrologyConfiguration_activationTime'));

                                    if (Imt.privileges.MetrologyConfig.canView()) {
                                        url = me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: me.usagePoint.get('metrologyConfiguration_id')});
                                        result = '<a href="' + url + '">' + Ext.String.htmlEncode(name) + '</a> ' + from + ' ' + activationTime;
                                    } else {
                                        result = Ext.String.htmlEncode(name) + ' ' + Uni.I18n.translate('metrologyConfigurationDetails.from', 'IMT', 'from') + ' ' + activationTime;
                                    }

                                    return result;
                                }
                            },
                            {
                                ui: 'medium',
                                xtype: 'panel',
                                title: Uni.I18n.translate('general.purposes', 'IMT', 'Purposes'),
                                itemId: 'metrology-configuration-purposes-panel',
                                items: [
                                    {
                                        xtype: 'preview-container',
                                        grid: {
                                            xtype: 'purposes-grid',
                                            store: purposesStore,
                                            itemId: 'metrology-configuration-purposes-grid'
                                        },
                                        emptyComponent: {
                                            xtype: 'uni-form-empty-message',
                                            itemId: 'metrology-configuration-purposes-no-items-found-panel',
                                            text: Uni.I18n.translate('metrologyConfigurationDetails.empty.list.item1', 'IMT', 'No purposes have been defined yet.')
                                        },
                                        previewComponent: {
                                            xtype: 'purposes-preview',
                                            itemId: 'metrology-configuration-purposes-preview',
                                            router: me.router
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
        me.side = [
            {
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint
                    }
                ]
            }
        ];
        me.callParent(arguments);
        purposesStore.fireEvent('load');
        if (!Ext.isEmpty(me.meterRolesAvailable)) {
            me.down('#metrology-configuration-details-form').insert(2, {
                xtype: 'meter-roles-grid',
                store: meterRolesStore,
                router: me.router,
                itemId: 'metrology-configuration-meter-roles-grid'
            });
        }
    }
});
