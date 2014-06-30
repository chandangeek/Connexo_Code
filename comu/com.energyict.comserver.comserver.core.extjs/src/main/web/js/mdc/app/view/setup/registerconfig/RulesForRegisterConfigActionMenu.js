Ext.define('Mdc.view.setup.registerconfig.RulesForRegisterConfigActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'rules-for-registerconfig-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            itemId: 'viewRuleForRegisterConfig',
            action: 'viewRuleForRegisterConfig'
        }
    ]
});
