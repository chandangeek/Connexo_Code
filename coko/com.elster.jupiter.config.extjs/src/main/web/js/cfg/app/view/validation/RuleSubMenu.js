Ext.define('Cfg.view.validation.RuleSubMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.ruleSubMenu',
    ruleSetId: null,
    ruleId: null,
    title: null,
    toggle: null,
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                pressed: false,
                itemId: 'ruleSetOverviewLink',
                href: '#/administration/validation/rulesets/validationrules/' + this.ruleSetId + '/ruleoverview/' + this.ruleId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
                pressed: false,
                itemId: 'rulesLink',
                href: '#/administration/validation/rulesets/validationrules/' + this.ruleSetId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('validation.deviceConfigurations', 'CFG', 'Device configurations'),
                pressed: false
                /* itemId: 'rulesLink',
                 href: '#/administration/validation/rulesets/validationrules/' + this.ruleSetId,
                 hrefTarget: '_self'*/
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});



