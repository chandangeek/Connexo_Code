Ext.define('Cfg.view.validation.RuleSetSubMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.ruleSetSubMenu',
    ruleSetId: null,
    title: null,
    toggle: null,
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                pressed: false,
                itemId: 'ruleSetOverviewLink',
                href: '#/administration/validation/rulesets/' + this.ruleSetId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
                pressed: false,
                itemId: 'rulesLink',
                href: '#/administration/validation/rulesets/' + this.ruleSetId + '/rules',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('validation.deviceConfigurations', 'CFG', 'Device configurations'),
                pressed: false,
                href: '#/administration/validation/rulesets/' + this.ruleSetId + '/deviceconfigurations',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});


