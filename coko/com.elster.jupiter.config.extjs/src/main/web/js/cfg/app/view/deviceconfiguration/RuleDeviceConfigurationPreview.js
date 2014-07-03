Ext.define('Cfg.view.deviceconfiguration.RuleDeviceConfigurationPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.rule-device-configuration-preview',
    frame: true,
    requires: [
        'Cfg.view.deviceconfiguration.RuleDeviceConfigurationActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
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
            fieldLabel: Uni.I18n.translate('validation.deviceConfiguration', 'CFG', 'Device configuration'),
            name: 'config_name_link'
        },
        {
            fieldLabel: Uni.I18n.translate('validation.deviceType', 'CFG', 'Device type'),
            name: 'deviceType_name'
        },
        {
            name: 'config_active',
            fieldLabel: Uni.I18n.translate('validation.deviceconfiguration.configurationStatus', 'CFG', 'Configuration status')
        },
        {
            name: 'config_registerCount',
            fieldLabel: Uni.I18n.translate('validation.deviceconfiguration.dataSources', 'CFG', 'Data sources')
        },
        {
            name: 'config_loadProfileCount',
            fieldLabel: '',
            hideEmptyLabel: false
        },
        {
            name: 'config_logBookCount',
            fieldLabel: '',
            hideEmptyLabel: false
        }
    ]
});
