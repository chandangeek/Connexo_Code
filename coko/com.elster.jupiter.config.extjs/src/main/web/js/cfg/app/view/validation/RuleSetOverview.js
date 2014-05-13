Ext.define('Cfg.view.validation.RuleSetOverview', {
    extend: 'Ext.panel.Panel',
    border: false,
    //overflowY: 'auto',
    //cls: 'content-container',
    region: 'center',
    //margins: '0 10 10 10',
    alias: 'widget.ruleSetOverview',
    itemId: 'ruleSetOverview',
    requires: [
        'Cfg.model.ValidationRuleSet' ,
        'Uni.view.breadcrumb.Trail'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },


    items: [
        {
            tbar: [
                {
                    xtype: 'component',
                    border: false,
                    html: '<h4>' +  Uni.I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets') + '</h4>',
                    itemId: 'rulesetOverviewTitle',
                    margins: '0 0 10 0'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text:  Uni.I18n.translate('validation.actions', 'CFG', 'Actions'),
                    menu:{
                        items:[
                            {
                                text:  Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
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
                }]
        },
        {
            xtype: 'form',
            border: false,
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
                    fieldLabel: Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                    labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'description',
                    fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                    labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfRules',
                    fieldLabel:  Uni.I18n.translate('validation.numberOfRules', 'CFG', 'Number of rules'),
                    labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfInactiveRules',
                    fieldLabel:  Uni.I18n.translate('validation.numberOfInActiveRules', 'CFG', 'Number of inactive rules'),
                    labelAlign: 'right',
                    labelWidth:	250
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
