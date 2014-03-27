Ext.define('Cfg.view.validation.RuleBrowse', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.validationruleBrowse',
    itemId: 'validationruleBrowse',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleList',
        'Cfg.view.validation.RulePreview',
        'Uni.view.breadcrumb.Trail',
        'Uni.view.container.EmptyGridContainer'
    ],

    ruleSetId: null,

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
                    html: '<h1>' + Uni.I18n.translate('validation.rules', 'CFG', 'Rules') + '</h1>',
                    itemId: 'ruleBrowseTitle',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'container',
                    layout: 'fit',
                    items: [],
                    itemId: 'validationruleListContainer'
                },
                {
                    xtype: 'component',
                    height: 50
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
                xtype: 'emptygridcontainer',
                grid: {
                    xtype: 'validationruleList',
                    ruleSetId: this.ruleSetId
                },
                emptyComponent: {
                    xtype: 'container',
                    layout: 'vbox',
                    items: [
                        {
                            xtype: 'component',
                            html: '<h3>No rules found</h3>'
                        },
                        {
                            xtype: 'component',
                            html: '<p>Sorry there are no rules configured. This could be because:</p>' +
                                '<ul>' +
                                '<li>No rules have been created yet</li>' +
                                '<li>The filter is too narrow</li>' +
                                '</ul>'
                        },
                        {
                            xtype: 'component',
                            html: '<p>Possible steps:</p>'
                        },
                        {
                            xtype: 'container',
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('validation.addRule', 'CFG', 'Add rule'),
                                    itemId: 'addRuleLink',
                                    href: '#administration/validation/addRule/' + this.ruleSetId,
                                    hrefTarget: '_self'
                                }
                            ]
                        }
                    ]
                }
            }
        );
    }
});

