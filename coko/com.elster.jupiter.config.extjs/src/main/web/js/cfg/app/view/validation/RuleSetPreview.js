Ext.define('Cfg.view.validation.RuleSetPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.ruleSetPreview',
    itemId: 'ruleSetPreview',
    hidden: true,
    requires: [
        'Cfg.model.ValidationRuleSet'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    tbar: [
        {
            xtype: 'component',
            html: '<h4>Validation rule sets</h4>',
            itemId: 'rulesetPreviewTitle'
        },
        '->',
        {
            icon: 'resources/images/gear-16x16.png',
            text: 'Actions',
            menu:{
                items:[
                    {
                        text: 'Edit',
                        itemId: 'editRuleset',
                        action: 'editRuleset'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: 'Delete',
                        itemId: 'deleteRuleset',
                        action: 'deleteRuleset'

                    }
                ]
            }
        }],

    items: [
        {
            xtype: 'form',
            itemId: 'rulesetForm',
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'displayfield',
                    name: 'name',
                    fieldLabel: 'Name:',
                    labelAlign: 'right',
                    labelWidth:	150
                },
                {
                    xtype: 'displayfield',
                    name: 'description',
                    fieldLabel: 'Description:',
                    labelAlign: 'right',
                    labelWidth:	150
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfRules',
                    fieldLabel: 'Number of rules:' ,
                    labelAlign: 'right',
                    labelWidth:	150
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfInactiveRules',
                    fieldLabel: 'Number of inactive rules:',
                    labelAlign: 'right',
                    labelWidth:	150
                },
                {
                    xtype: 'toolbar',
                    docked: 'bottom',
                    title: 'Bottom Toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'component',
                            cls: 'content-container',
                            itemId: 'ruleSetDetailsLink',
                            html: '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation/rules">View details</a>'
                        }

                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
