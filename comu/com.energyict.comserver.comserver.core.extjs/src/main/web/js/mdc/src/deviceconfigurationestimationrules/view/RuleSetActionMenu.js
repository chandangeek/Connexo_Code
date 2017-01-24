Ext.define('Mdc.deviceconfigurationestimationrules.view.RuleSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-configuration-estimation-rule-set-action-menu',
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'remove-action',
            action: 'remove'
        }
    ]
});