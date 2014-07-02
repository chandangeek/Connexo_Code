Ext.define('Mdc.view.setup.loadprofileconfiguration.RulesForLoadProfileConfigActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'rules-for-loadprofileconfig-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            itemId: 'viewRuleForLoadProfileConfig',
            action: 'viewRuleForLoadProfileConfig'
        }
    ]
});
