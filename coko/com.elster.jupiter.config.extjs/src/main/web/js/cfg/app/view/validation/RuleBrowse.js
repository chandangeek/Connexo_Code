Ext.define('Cfg.view.validation.RuleBrowse', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.validationruleBrowse',
    itemId: 'validationruleBrowse',

    requires: [
        'Cfg.view.validation.RuleList',
        'Cfg.view.validation.RulePreview'
    ],

    ruleSetId: null,

    items: [
        {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules') + '</h1>',
                    itemId: 'ruleBrowseTitle'
                },
                {
                    xtype: 'container',
                    layout: 'fit',
                    items: [],
                    itemId: 'validationruleListContainer'
                },
                {
                    xtype: 'validation-rule-preview',
                    margin: '32 0 0 0'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.down('#validationruleListContainer').add(
            {
                xtype: 'validationruleList',
                ruleSetId: this.ruleSetId
            }
        );

    }
});