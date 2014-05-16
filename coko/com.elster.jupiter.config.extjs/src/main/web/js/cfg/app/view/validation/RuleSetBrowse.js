Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    border: false,
    alias: 'widget.validationrulesetBrowse',
    //cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetBrowseFilter',
        'Cfg.view.validation.RuleSetPreview',
        'Uni.view.breadcrumb.Trail'
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
                    html: '<h1>' +  Uni.I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets') +'</h1>'
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

    /*side: [
        {
            xtype: 'rulesetbrowsefilter'
        }
    ], */



    initComponent: function () {
        this.callParent(arguments);
    }
});
