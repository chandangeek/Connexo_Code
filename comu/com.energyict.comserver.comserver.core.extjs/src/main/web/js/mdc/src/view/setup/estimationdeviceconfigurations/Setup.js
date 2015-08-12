Ext.define('Mdc.view.setup.estimationdeviceconfigurations.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.estimation-deviceconfigurations-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.estimationdeviceconfigurations.Grid',
        'Mdc.view.setup.estimationdeviceconfigurations.Preview'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            items: [
                {
                    xtype: 'uni-view-menu-side',
                    itemId: 'estimation-rule-set-side-menu',
                    router: me.router,
                    title: Uni.I18n.translate('estimationrulesets.estimationruleset', 'MDC', 'Estimation rule set'),
                    menuItems: [
                        {
                            text: Uni.I18n.translate('estimationrulesets.estimationruleset', 'MDC', 'Estimation rule set'),
                            itemId: 'estimation-rule-set-link',
                            href: me.router.getRoute('administration/estimationrulesets/estimationruleset').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('general.estimationRules', 'MDC', 'Estimation rules'),
                            itemId: 'estimation-rules-link',
                            href: me.router.getRoute('administration/estimationrulesets/estimationruleset/rules').buildUrl()
                        },
                        {
                            text: Uni.I18n.translate('estimationDeviceConfigurations.deviceConfigurations', 'MDC', 'Device configurations'),
                            itemId: 'estimation-device-configurations-link',
                            href: me.router.getRoute('administration/estimationrulesets/estimationruleset/deviceconfigurations').buildUrl()
                        }
                    ]
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('estimationDeviceConfigurations.deviceConfigurations', 'MDC', 'Device configurations'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'estimation-deviceconfigurations-grid',
                        itemId: 'estimation-deviceconfigurations-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-items-found-panel',
                        title: Uni.I18n.translate('estimationDeviceConfigurations.empty.title', 'MDC', 'No device configurations found'),
                        reasons: [
                            Uni.I18n.translate('estimationDeviceConfigurations.empty.list.item1', 'MDC', 'No device configurations have been defined yet')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('estimationDeviceConfigurations.addDeviceConfigurations', 'MDC', 'Add device configurations'),
                                href: me.router.getRoute('administration/estimationrulesets/estimationruleset/deviceconfigurations/add').buildUrl(),
                                itemId: 'add-device-configurations-button',
                                privileges: Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfiguration
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'estimation-deviceconfigurations-preview',
                        itemId: 'estimation-deviceconfigurations-preview',
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

