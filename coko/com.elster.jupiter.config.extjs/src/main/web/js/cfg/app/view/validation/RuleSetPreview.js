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
            html: '<h4>' + I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets')  + '</h4>',
            itemId: 'rulesetPreviewTitle'
        },
        '->',
        {
            icon: 'resources/images/gear-16x16.png',
            text: I18n.translate('validation.actions', 'CFG', 'Actions'),
            menu:{
                items:[
                    {
                        text: I18n.translate('general.edit', 'CFG', 'Edit'),
                        itemId: 'editRuleset',
                        action: 'editRuleset'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: I18n.translate('general.delete', 'CFG', 'Delete'),
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
                    fieldLabel: I18n.translate('validation.name', 'CFG', 'Name') + ':',
                    //labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'description',
                    fieldLabel: I18n.translate('validation.description', 'CFG', 'Description') + ':',
                    //labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfRules',
                    fieldLabel: I18n.translate('validation.numberOfRules', 'CFG', 'Number of rules') + ':',
                    //labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfInactiveRules',
                    fieldLabel:  I18n.translate('validation.numberOfInActiveRules', 'CFG', 'Number of inactive rules') + ':',
                    //labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'toolbar',
                    docked: 'bottom',
                    title: 'Bottom Toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'component',
                            itemId: 'ruleSetDetailsLink',
                            html: '' // filled in in Controller
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
