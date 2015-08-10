Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.rule-device-configuration-preview',
    frame: true,
    requires: [
        'Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            privileges: Cfg.privileges.Validation.deviceConfiguration,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'rule-device-configuration-action-menu'
            }
        }
    ],
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('validation.deviceType', 'MDC', 'Device type'),
            name: 'deviceType_name'
        },
        {
            fieldLabel: Uni.I18n.translate('validation.deviceConfiguration', 'MDC', 'Device configuration'),
            name: 'config_name'
        },
        {
            name: 'config_active',
            fieldLabel: Uni.I18n.translate('validation.deviceconfiguration.configurationStatus', 'MDC', 'Configuration status')
        },
        {
            name: 'config_registerCount',
            fieldLabel: Uni.I18n.translate('validation.deviceconfiguration.dataSources', 'MDC', 'Data sources'),
            htmlEncode: false
        },
        {
            name: 'config_loadProfileCount',
            fieldLabel: '',
            hideEmptyLabel: false,
            htmlEncode: false
        }
    ]
});
