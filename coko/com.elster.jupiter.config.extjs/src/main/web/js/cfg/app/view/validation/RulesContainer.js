Ext.define('Cfg.view.validation.RulesContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.rulesContainer',
    itemId: 'rulesContainer',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetPreview',
        'Cfg.view.validation.RuleBrowse'
    ],

    layout:'border',
    items: [{
        region:'west',
        width: 150,
        minSize: 150,
        maxSize: 150,
        dockedItems: [{
            xtype: 'toolbar',
            dock : 'left',
            width: 150,
            layout: {
                type: 'vbox',
                align: 'left'
            },
            items: [{
                text: 'Overview',
                itemId:'showRulesetOverviewAction',
                action: 'showRulesetOverviewAction',
                width: 130
            }, {
                text: 'Rules',
                itemId:'showRulesAction',
                action: 'showRulesAction',
                width: 130
            }]
        }]
    },{
        xtype: 'container',
        region: 'center',
        itemId: 'rulesListContainer',
        layout: 'fit'
    }],

    initComponent: function () {
        this.callParent(arguments);
    }
});

