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

    // TODO Refactor this into a preview container cause it is currently broken if scrolling is required.

    /*content: [
        {
            ui: 'large',
            xtype: 'panel',
            itemId: 'ruleBrowsePanel',
            title: Uni.I18n.translate('validation.rules', 'CFG', 'Rules'),
            items: [
                {
                    xtype: 'container',
                    layout: 'fit',
                    items: [],
                    itemId: 'validationruleListContainer'
                },
                {
                    xtype: 'rulePreview',
                    margin: '32 0 0 0'
                }
            ]
        }
    ],     */

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
                    xtype: 'validation-rule-preview',
                    margin: '32 0 0 0'
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