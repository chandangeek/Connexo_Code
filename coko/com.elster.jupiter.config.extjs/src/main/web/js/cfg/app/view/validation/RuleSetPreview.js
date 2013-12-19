Ext.define('Cfg.view.validation.RuleSetPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '10 10 10 10',
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
        '->',
        {
            text: 'Delete',
            itemId: 'deleteRuleSet',
            action: 'deleteRuleSet'
        },
        {
            text: 'Clone',
            itemId: 'cloneRuleSet',
            action: 'cloneRuleSet'
        },
        {
            text: 'Edit',
            itemId: 'editRuleSet',
            action: 'editRuleSet'
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
            defaults: {
                anchor: '100%',
                margins: '0 0 5 0'
            },

            items: [
                {
                    xtype: 'displayfield',
                    name: 'name',
                    fieldLabel: 'Name:'
                },
                {
                    xtype: 'displayfield',
                    name: 'description',
                    fieldLabel: 'Description:'
                },
                {
                    xtype: 'displayfield',
                    name: 'numberOfActiveRules',
                    fieldLabel: '# Active Rules:'
                },{
                    xtype: 'displayfield',
                    name: 'numberOfRules',
                    fieldLabel: '# Rules:'
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
                            html: '<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/validation/rulesforset">View details</a>'
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
