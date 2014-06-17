Ext.define('Mdc.view.setup.validation.AddRuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'validation-add-ruleset-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            itemId: 'activateDeviceconfigurationMenuItem',
            action: 'viewRuleSetActionMenu'
        }
    ]
});
