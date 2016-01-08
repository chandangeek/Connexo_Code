Ext.define('Imt.metrologyconfiguration.view.validation.RuleActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.validation-rule-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'IMT', 'View'),
            itemId: 'viewRule',
            action: 'viewRule'
        }
    ]
});
