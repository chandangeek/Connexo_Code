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
        'Imt.usagepointmanagement.view.metrologyconfiguration.PurposesPreview',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.container.EmptyGridContainer'
    ],
    router: null,
    usagePoint: null,
    initComponent: function () {
        var me = this,
            meterRolesAvailable = me.usagePoint.get('metrologyConfiguration_meterRoles'),
            meterRoles = me.usagePoint.get('metrologyConfiguration_meterRoles'),
            meterRolesStore = Ext.create('Imt.usagepointmanagement.store.metrologyconfiguration.MeterRoles', {
                data: meterRoles || [],
                totalCount: !Ext.isEmpty(meterRoles) ? meterRoles.length : 0
            }),
            purposes = me.usagePoint.get('metrologyConfiguration_purposes'),
            purposesStore = Ext.create('Imt.usagepointmanagement.store.metrologyconfiguration.Purposes', {
                data: purposes || [],
                totalCount: !Ext.isEmpty(purposes) ? purposes.length : 0
            }),
            remoteMeterRolesStore = Ext.getStore('Imt.usagepointmanagement.store.MeterRoles'),
            mcIsLinked = !!me.usagePoint.get('metrologyConfiguration'),
            canModify = me.usagePoint.get('state').stage === 'PRE_OPERATIONAL';

        me.content = [
            {
                title: Uni.I18n.translate('general.label.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                ui: 'large',
                flex: 1,
                itemId: 'metrology-configuration-details-main-panel',
                tools: [
                    {
                        xtype: 'button',
                        itemId: 'unlink-metrology-configuration-button',
                        text: Uni.I18n.translate('usagePoint.metrologyConfiguration.unlink', 'IMT', 'Unlink metrology configuration'),
                        privileges: mcIsLinked && canModify,
                        usagePoint: me.usagePoint
                    }
                ],
                items: [
                    {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('general.noMetrologyConfiguration', 'IMT', 'No metrology configuration'),
                        reasons: [
                            Uni.I18n.translate('usagePoint.metrologyConfiguration.empty.reason', 'IMT', 'Metrology configuration has not been linked to this usage point yet')
                        ],
                        itemId: 'no-metrology-configuration-panel',
                        stepItems: [
                            {
                                text: Uni.I18n.translate('usagePoint.metrologyConfiguration.link', 'IMT', 'Link metrology configuration'),
                                privileges: Imt.privileges.UsagePoint.canAdministrate,
                                href: me.router.getRoute('usagepoints/view/definemetrology').buildUrl(),
                                action: 'define',
                                itemId: 'define-metrology-configuration'
                            }
                        ],
                        privileges: !mcIsLinked
                    },
                    {
                        title: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
                        ui: 'medium',
                        style: 'padding-left: 0; padding-right: 0; padding-bottom: 0',
                        privileges: !mcIsLinked
                    },
                    {
                        xtype: 'emptygridcontainer',
                        privileges: !mcIsLinked,
                        grid: {
                            xtype: 'meter-roles-grid',
                            itemId: 'metrology-configuration-meter-roles-grid',
                            store: 'Imt.usagepointmanagement.store.MeterRoles',
                            router: me.router,
                            style: 'padding-left: 0; padding-right: 0',
                            hasLinkMetersButton: false
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'meter-roles-empty-message',
                            title: Uni.I18n.translate('usagePoint.meterRoles.empty.title', 'IMT', 'No meter roles'),
                            reasons: [
                                Uni.I18n.translate('usagePoint.meterRoles.empty.reason', 'IMT', 'Meter roles have not been linked to this usage point yet')
                            ],
                            style: 'margin-top: 15px'
                        }
                    },
                    {
                        xtype: 'form',
                        itemId: 'metrology-configuration-details-form',
                        privileges: mcIsLinked,
                        defaults: {
                            xtype: 'displayfield',
                            padding: 0,
                            margin: '0 10 16 0',
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
                                        icon = '&nbsp;&nbsp;<i class="icon ' + (status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle') + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
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
                                xtype: 'meter-roles-grid',
                                style: 'padding-left: 0; padding-right: 0',
                                store: meterRolesStore,
                                router: me.router,
                                itemId: 'metrology-configuration-meter-roles-grid',
                                ui: 'medium',
                                title: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
                                maxHeight: 408,
                                privileges: !Ext.isEmpty(meterRolesAvailable),
                                hasLinkMetersButton: canModify
                            },
                            {
                                ui: 'medium',
                                xtype: 'panel',
                                style: 'padding-left: 0; padding-right: 0',
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
        if (!mcIsLinked) {
            remoteMeterRolesStore.getProxy().setExtraParam('usagePointId', me.usagePoint.get('name'));
            remoteMeterRolesStore.load();
        } else {
            meterRolesStore.fireEvent('load', meterRolesStore.getRange());
        }
    }
});
