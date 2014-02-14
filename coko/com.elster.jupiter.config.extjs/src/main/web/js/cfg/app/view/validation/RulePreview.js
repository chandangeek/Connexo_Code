Ext.define('Cfg.view.validation.RulePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
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
        {
            xtype: 'component',
            html: '<h4>' + I18n.translate('validation.validationRule', 'CFG', 'Validation rule') + '</h4>',
            itemId: 'rulePreviewTitle'
        },
        '->',
        {
            icon: 'resources/images/gear-16x16.png',
            text: I18n.translate('validation.actions', 'CFG', 'Actions'),
            menu:{
                items:[
                    {
                        text: I18n.translate('general.edit', 'CFG', 'Edit'),
                        itemId: 'editRule',
                        action: 'editRule'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: I18n.translate('general.delete', 'CFG', 'Delete'),
                        itemId: 'deleteRule',
                        action: 'deleteRule'

                    }
                ]
            }
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
                    name: 'displayName',
                    fieldLabel: I18n.translate('validation.Rule', 'CFG', 'Rule'),
                    labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'active',
                    fieldLabel: I18n.translate('validation.active', 'CFG', 'Active'),
                    labelAlign: 'right',
                    labelWidth:	250,
                    renderer:function(value){
                        if (value) {
                            return I18n.translate('general.yes', 'CFG', 'Yes')
                        } else {
                            return I18n.translate('general.no', 'CFG', 'no')
                        }
                    }
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel:  I18n.translate('validation.readingValues', 'CFG', 'Reading value(s)'),
                    labelAlign: 'right',
                    labelWidth:	250,
                    layout: 'vbox',
                    defaults: {
                        flex: 1,
                        hideLabel: true
                    },
                    itemId: 'readingTypesArea',
                    items: []
                },
                {
                    xtype: 'container',
                    itemId: 'propertiesArea',
                    items: []
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
