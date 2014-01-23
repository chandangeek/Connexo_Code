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
            html: '<h4>Validation rule</h4>',
            itemId: 'rulePreviewTitle'
        },
        '->',
        {
            icon: 'resources/images/gear-16x16.png',
            text: 'Actions',
            menu:{
                items:[
                    {
                        text: 'Edit',
                        itemId: 'editRule',
                        action: 'editRule'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: 'Delete',
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
                    fieldLabel: 'Name:',
                    labelAlign: 'right',
                    labelWidth:	150
                },
                {
                    xtype: 'displayfield',
                    name: 'active',
                    fieldLabel: 'Active:',
                    labelAlign: 'right',
                    labelWidth:	150,
                    renderer:function(value){
                        if (value) {
                            return 'Yes'
                        } else {
                            return 'No'
                        }
                    }
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Reading value(s):',
                    labelAlign: 'right',
                    labelWidth:	150,
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
