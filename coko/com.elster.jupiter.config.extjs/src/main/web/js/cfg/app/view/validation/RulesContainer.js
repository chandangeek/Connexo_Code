Ext.define('Cfg.view.validation.RulesContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.rulesContainer',
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
                width: 130
            }, {
                text: 'Rules',
                width: 130
            }]
        }]
    },{
        region:'center',
        itemId: 'rulesListContainer'
        //xtype: 'validationruleBrowse'
        //xtype: 'ruleSetPreview'
    }],

    initComponent: function () {
        this.callParent(arguments);
    }
});
