Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.validationrulesetBrowse',
    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetBrowseFilter',
        'Cfg.view.validation.RuleSetPreview',
        'Uni.view.breadcrumb.Trail'
    ],

    content: [
        {
            ui: 'large',
            xtype: 'panel',
            title: Uni.I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets'),
            items: [
                {
                    xtype: 'validationrulesetList'
                },
                {
                    xtype: 'ruleSetPreview'
                }
            ]
        }
    ],



    initComponent: function () {
        this.callParent(arguments);
    }
});
