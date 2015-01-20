Ext.define('Cfg.view.validation.RuleSetSubMenu', {
    extend: 'Uni.view.menu.SideMenu',
    xtype: 'ruleSetSubMenu',

    ruleSetId: null,

    title: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                itemId: 'ruleSetOverviewLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId
            },
            {
                text: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
                itemId: 'rulesLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId + '/rules'
            },
            {
                text: Uni.I18n.translate('validation.deviceConfigurations', 'CFG', 'Device configurations'),
                itemId: 'deviceConfigLink',
                href: '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations',
                hidden: true
            }
        ];

        me.callParent(arguments);
    }

});


/*Ext.define('Cfg.view.validation.RuleSetSubMenu', {
 extend: 'Uni.view.navigation.SubMenu',
 xtype: 'ruleSetSubMenu',

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
 itemId: 'deviceConfigLink',
 href: '#/administration/validation/rulesets/' + this.ruleSetId + '/deviceconfigurations',
 hrefTarget: '_self',
 hidden: true
 }
 );

 this.toggleMenuItem(this.toggle);
 }
 });*/


