/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.OutputChannelMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.output-channel-main',
    itemId: 'tabbedDeviceChannelsView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Imt.purpose.view.OutputSpecificationsForm',
        'Imt.purpose.view.OutputReadings',
        'Cfg.configuration.view.RuleWithAttributesForm'
    ],
    validationConfigurationStore: undefined,
    estimationConfigurationStore: undefined,

    initComponent: function () {
        var me = this,
            router = me.router,
            dataStore;

        switch (me.output.get('outputType')) {
            case 'channel':
                dataStore = 'Imt.purpose.store.Readings';
                break;
            case 'register':
                dataStore = 'Imt.purpose.store.RegisterReadings';
                break;
        }

        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                title: router.getRoute().getTitle(),
                itemId: 'channelTabPanel',
                activeTab: 'output-' + me.tab,
                listeners: {
                    tabchange: function(){
                        var toolbar = this.down('previous-next-navigation-toolbar');
                        Ext.suspendLayouts();
                        toolbar.removeAll();
                        toolbar.initToolbar(Ext.getStore(toolbar.store));
                        Ext.resumeLayouts(true);
                    }
                },
                items: [
                    {
                        title: Uni.I18n.translate('deviceloadprofiles.specifications', 'IMT', 'Specifications'),
                        itemId: 'output-specifications',
                        items: {
                            xtype: 'output-specifications-form',
                            router: me.router
                        },
                        listeners: {
                            activate: me.controller.showSpecificationsTab,
                            scope: me.controller
                        }
                    },
                    {
                        title:  Uni.I18n.translate('deviceloadprofiles.readings', 'IMT', 'Readings'),
                        itemId: 'output-readings',
                        items: {
                            xtype: 'output-readings',
                            interval: me.interval,
                            purpose: me.purpose,
                            output: me.output,
                            router: me.router,
                            store: dataStore
                        },
                        listeners: {
                            activate: me.controller.showReadingsTab,
                            scope: me.controller
                        },
                        usagePoint: me.usagePoint,
                        purpose: me.purpose,
                        output: me.output

                    }
                ],
                tabBar: {
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'previous-next-navigation-toolbar',
                            itemId: 'tabbed-device-channels-view-previous-next-navigation-toolbar',
                            store: 'Imt.purpose.store.Outputs',
                            router: me.router,
                            routerIdArgument: 'outputId',
                            itemsName: me.prevNextListLink,
                            indexLocation: 'arguments',
                            isFullTotalCount: true
                        }
                    ]
                }
            }
        ];

        if (me.validationConfigurationStore.getCount() && Imt.privileges.UsagePoint.canViewValidationConfiguration()) {
            me.content[0].items.push({
                title: Uni.I18n.translate('general.validationConfiguration', 'IMT', 'Validation configuration'),
                itemId: 'output-validation',
                items: {
                    xtype: 'rule-with-attributes-form',
                    itemId: 'rule-with-attributes-validation-form',
                    router: me.router,
                    records: me.validationConfigurationStore.getRange(),
                    type: 'validation',
                    application: me.controller.getApplication(),
                    hasAdministerPrivileges: Imt.privileges.UsagePoint.canAdministerValidationConfiguration()
                },
                listeners: {
                    activate: me.controller.showValidationTab,
                    scope: me.controller
                }
            });
        }

        if (me.output.get('outputType') === 'channel' && me.estimationConfigurationStore.getCount() && Imt.privileges.UsagePoint.canViewEstimationConfiguration()) {
            me.content[0].items.push({
                title: Uni.I18n.translate('general.estimationConfiguration', 'IMT', 'Estimation configuration'),
                itemId: 'output-estimation',
                items: {
                    xtype: 'rule-with-attributes-form',
                    itemId: 'rule-with-attributes-estimation-form',
                    router: me.router,
                    records: me.estimationConfigurationStore.getRange(),
                    type: 'estimation',
                    application: me.controller.getApplication(),
                    hasAdministerPrivileges: Imt.privileges.UsagePoint.canAdministerEstimationConfiguration()
                },
                listeners: {
                    activate: me.controller.showEstimationTab,
                    scope: me.controller
                }
            });
        }

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
