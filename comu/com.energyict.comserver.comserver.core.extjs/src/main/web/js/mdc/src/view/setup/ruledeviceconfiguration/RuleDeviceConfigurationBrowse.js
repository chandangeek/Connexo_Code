Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ruleDeviceConfigurationBrowse',
    requires: [
        'Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationGrid',
        'Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.view.validation.RuleSetSubMenu'
    ],
    ruleSetId: null,

    initComponent: function () {
        var me = this;
        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.deviceConfigurations', 'MDC', 'Device configurations'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'rule-device-configuration-grid',
                            ruleSetId: me.ruleSetId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('validation.empty.deviceconfiguration.title', 'MDC', 'No device configurations found'),
                            reasons: [
                                Uni.I18n.translate('validation.empty.deviceconfiguration.list.item1', 'MDC', 'No device configurations have been added yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('validation.deviceconfiguration.add', 'MDC', 'Add device configuration'),
                                    privileges : Cfg.privileges.Validation.deviceConfiguration,
                                    itemId: 'addDeviceConfiguration',
                                    listeners: {
                                        click: {
                                            fn: function () {
                                                window.location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations/add';
                                            }
                                        }
                                    }
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'rule-device-configuration-preview'
                        }
                    }
                ]
            }
        ];
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: this.ruleSetId
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

