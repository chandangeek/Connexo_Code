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
                title: Uni.I18n.translate('validation.deviceConfigurations', 'CFG', 'Device configurations'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'rule-device-configuration-grid',
                            ruleSetId: me.ruleSetId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('validation.empty.deviceconfiguration.title', 'CFG', 'No device configurations found'),
                            reasons: [
                                Uni.I18n.translate('validation.empty.deviceconfiguration.list.item1', 'CFG', 'No device configurations have been added yet.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('validation.deviceconfiguration.add', 'CFG', 'Add device configuration'),
                                    privileges: ['privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
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
                title: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: me.ruleSetId,
                        toggle: 2
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

