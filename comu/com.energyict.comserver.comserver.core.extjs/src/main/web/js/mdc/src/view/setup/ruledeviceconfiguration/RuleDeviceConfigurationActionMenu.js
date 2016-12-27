Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.rule-device-configuration-action-menu',
    itemId: 'device-configuration-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.view', 'MDC', 'View'),
                action: 'view',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});
