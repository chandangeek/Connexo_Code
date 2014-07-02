Ext.define('Cfg.view.deviceconfiguration.RuleDeviceConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.rule-device-configuration-preview',
    hidden: false,
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
    items: {
        xtype: 'panel',
        itemId: 'comtaskPreviewFieldsPanel',
        layout: 'column',
        ui: 'medium',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 1
        },
        items: [
            {
                xtype: 'displayfield',
                itemId: 'comtaskName',
                fieldLabel: Uni.I18n.translate('comtask.name', 'MDC', 'Name'),
                name: 'name'
            },
            {
                xtype: 'displayfield',
                itemId: 'comtaskCommands',
                fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands'),
                name: 'commandsString'
            }
        ]
    }
});
