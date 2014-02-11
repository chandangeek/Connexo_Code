Ext.define('Cfg.view.validation.RuleBrowse', {
    extend: 'Ext.panel.Panel',
    ruleSetId: null,
    alias: 'widget.validationruleBrowse',
    itemId: 'validationruleBrowse',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleList',
        'Cfg.view.validation.RulePreview',
        'Uni.view.breadcrumb.Trail'
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
                    xtype: 'breadcrumbTrail',
                    region: 'north',
                    padding: 6
                },
                {
                    xtype: 'component',
                    html: '<h1>Rules</h1>',
                    itemId: 'ruleBrowseTitle',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'validationruleListContainer'
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
        this.down('#validationruleListContainer').add(
            {
                xtype: 'validationruleList',
                ruleSetId: this.ruleSetId
            }
        );
    }
});

