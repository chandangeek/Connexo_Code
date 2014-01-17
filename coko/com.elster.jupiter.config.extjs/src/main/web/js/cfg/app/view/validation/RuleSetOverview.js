Ext.define('Cfg.view.validation.RuleSetOverview', {
    extend: 'Ext.panel.Panel',
    //border: true,
    margins: '10 10 10 10',
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
            html: '<h1>Validation rule sets</h1>',
            itemId: 'rulesetOverviewTitle'
        },
        '->',
        {
            icon: 'resources/images/gear-16x16.png',
            text: 'Actions',
            menu:{
                items:[
                    {
                        text: 'Edit',
                        action: 'editRuleset'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: 'Delete',
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

