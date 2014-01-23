Ext.define('Cfg.view.validation.RuleSetOverview', {
    extend: 'Ext.panel.Panel',
    //border: true,
    overflowY: 'auto',
    cls: 'content-container',
    region: 'center',
    //margins: '0 10 10 10',
    alias: 'widget.ruleSetOverview',
    itemId: 'ruleSetOverview',
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
            itemId: 'rulesetOverviewTitle',
            margins: '0 0 10 0'
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
            itemId: 'rulesetOverviewForm',
            name: 'rulesetOverviewForm',
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
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
