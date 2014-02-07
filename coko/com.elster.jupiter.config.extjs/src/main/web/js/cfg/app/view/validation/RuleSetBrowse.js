Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Ext.panel.Panel',
    //border: true,
    alias: 'widget.validationrulesetBrowse',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetPreview'
    ],
    region: 'center',


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
                    html: '<h1>Validation rule sets</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'validationrulesetList'
                },
                {
                    xtype: 'component',
                    height : 50
                },
                {
                    xtype: 'component',
                    html: '<h3>Selected rule set preview</h3>',
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
