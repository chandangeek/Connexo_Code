Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.rule-device-configuration-action-menu',
    plain: true,
    border: false,
    itemId: 'device-configuration-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            action: 'view'
        }
    ]
});
