Ext.define('Cfg.view.validation.RuleBrowse', {
    extend: 'Ext.panel.Panel',
    //extend: 'Uni.view.container.ContentContainer',
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
                    html: '<h1>' + Uni.I18n.translate('validation.rules', 'CFG', 'Rules') + '</h1>',
                    itemId: 'ruleBrowseTitle'
                },
                {
                    xtype: 'container',
                    layout: 'fit',
                    items: [],
                    itemId: 'validationruleListContainer'
                },
                {
                    xtype: 'component',
                    height : 50
                },
                {
                    xtype: 'rulePreview'
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