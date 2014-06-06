Ext.define('Cfg.view.validation.RuleSetPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.ruleSetPreview',
    itemId: 'ruleSetPreview',
    requires: [
        'Cfg.model.ValidationRuleSet',
        'Cfg.view.validation.RuleSetActionMenu'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: "Details",


    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'ruleset-action-menu'
            }
        }
    ],


    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<H4>' + Uni.I18n.translate('validation.noRuleSetSelected', 'MDC', 'No rule set selected') + '</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>' + Uni.I18n.translate('validation.selectRuleSet', 'MDC', 'Select a rule set to see its details') + '</H5>'
                }
            ]

        },

        {
            xtype: 'form',
            border: false,
            itemId: 'rulesetForm',
            //padding: '0 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            /*tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets')  + '</h4>',
                    itemId: 'rulesetPreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: Uni.I18n.translate('validation.actions', 'CFG', 'Actions'),
                    menu:{
                        items:[
                            {
                                text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                                itemId: 'editRuleset',
                                action: 'editRuleset'

                            },
                            {
                                xtype: 'menuseparator'
                            },
                            {
                                text: Uni.I18n.translate('general.delete', 'CFG', 'Delete'),
                                itemId: 'deleteRuleset',
                                action: 'deleteRuleset'

                            }
                        ]
                    }
                }],  */

            items: [
                {
                    xtype: 'displayfield',
                    //margin: '10 0 0 0',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                    //labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'description',
                    fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                    //labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfRules',
                    fieldLabel: Uni.I18n.translate('validation.numberOfRules', 'CFG', 'Number of rules'),
                    //labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfInactiveRules',
                    fieldLabel:  Uni.I18n.translate('validation.numberOfInActiveRules', 'CFG', 'Number of inactive rules'),
                    //labelAlign: 'right',
                    labelWidth:	250
                }
                /*{
                    xtype: 'toolbar',
                    docked: 'bottom',
                    border: false,
                    title: 'Bottom Toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'component',
                            itemId: 'ruleSetDetailsLink',
                            html: '' // filled in in Controller
                        }

                    ]
                }  */
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
