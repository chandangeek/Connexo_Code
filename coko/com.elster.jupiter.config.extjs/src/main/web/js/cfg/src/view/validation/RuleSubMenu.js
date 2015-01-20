Ext.define('Cfg.view.validation.RuleSubMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.ruleSubMenu',

    ruleSetId: null,
    ruleId: null,

    title: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                itemId: 'ruleSetOverviewLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId + '/rules/' + me.ruleId
            }
        ];

        me.callParent(arguments);
    }
});