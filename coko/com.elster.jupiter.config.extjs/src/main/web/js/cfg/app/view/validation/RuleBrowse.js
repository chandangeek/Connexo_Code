Ext.define('Cfg.view.validation.RuleBrowse', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.validationruleBrowse',
    itemId: 'validationruleBrowse',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleList',
        'Cfg.view.validation.RulePreview'
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Rules</h1>',
                    itemId: 'ruleBrowseTitle',
                    margins: '10 10 10 10'
                },
                {
                    tbar: [
                        '->',
                        {
                            text: 'Create new ruleset',
                            itemId: 'newRuleset',
                            action: 'newRuleset'
                        },
                        {
                            text: 'Bulk action',
                            itemId: 'rulesetBulkAction',
                            action: 'rulesetBulkAction'
                        }]
                },
                {
                    xtype: 'validationruleList'
                },
                {
                    xtype: 'container'
                },
                {
                    xtype: 'component',
                    height : 50
                },
                {
                    xtype: 'component',
                    html: '<h3>Selected rule preview</h3>',
                    margins:  '0 0 0 10'
                },
                {
                    xtype: 'rulePreview'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

