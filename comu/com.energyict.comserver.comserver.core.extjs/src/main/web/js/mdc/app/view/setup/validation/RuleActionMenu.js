Ext.define('Mdc.view.setup.validation.RuleActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.validation-rule-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            itemId: 'viewRule',
            action: 'viewRule'
        }
    ]
});
