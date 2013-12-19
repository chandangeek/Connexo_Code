Ext.define('Cfg.view.validation.RulePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '10 10 10 10',
    alias: 'widget.rulePreview',
    itemId: 'rulePreview',
    hidden: true,
    requires: [
        'Cfg.model.ValidationRule'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    tbar: [
        '->',
        {
            text: 'Delete',
            itemId: 'deleteRule',
            action: 'deleteRule'
        },
        {
            text: 'Clone',
            itemId: 'cloneRule',
            action: 'cloneRule'
        },
        {
            text: 'Edit',
            itemId: 'editRule',
            action: 'editRule'
        }],

    items: [
        {
            xtype: 'form',
            itemId: 'ruleForm',
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
                    name: 'implementation',
                    fieldLabel: 'Name:'
                },
                {
                    xtype: 'checkbox',
                    readonly: 'true',
                    name: 'active',
                    fieldLabel: 'Active:'
                },
                {
                    xtype: 'displayfield',
                    name: 'action',
                    fieldLabel: 'Action:'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
