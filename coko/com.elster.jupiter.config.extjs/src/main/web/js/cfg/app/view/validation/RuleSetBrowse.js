Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    border: false,
    alias: 'widget.validationrulesetBrowse',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetBrowseFilter',
        'Cfg.view.validation.RuleSetPreview'
    ],
    region: 'center',

    content: [
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
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets') + '</h1>'
                },
                {
                    xtype: 'validationrulesetList'
                },
                {
                    xtype: 'ruleSetPreview',
                    margin: '32 0 0 0'
                }
            ]
        }
    ],

    /*side: [
     {
     xtype: 'rulesetbrowsefilter'
     }
     ], */

    initComponent: function () {
        this.callParent(arguments);
    }
});
