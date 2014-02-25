Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Ext.panel.Panel',
    border: false,
    alias: 'widget.validationrulesetBrowse',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetPreview',
        'Uni.view.breadcrumb.Trail'
    ],
    region: 'center',


    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            border: false,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'breadcrumbTrail',
                    region: 'north',
                    padding: 6
                },

                {
                    xtype: 'component',
                    html: '<h1>' +  Uni.I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets') +'</h1>',
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
                    xtype: 'ruleSetPreview'
                }
            ]
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});
