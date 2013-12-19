Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Ext.container.Container',
    alias: 'widget.validationrulesetBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetPreview'
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
                    html: '<h1>Rule Sets</h1>'
                },
                {
                    xtype: 'validationrulesetList'
                },
                {
                    xtype: 'container'
                },
                {
                    xtype: 'component',
                    height : 50},
                {
                    xtype: 'component',
                    html: '<h3>Selected ruleset preview</h3>',
                    margins:  '0 0 0 10'
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
