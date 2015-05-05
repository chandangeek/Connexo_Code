Ext.define('Mdc.deviceconfigurationestimationrules.view.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-configuration-estimation-rule-set-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'remove-action',
            action: 'remove'
        }
    ]
});