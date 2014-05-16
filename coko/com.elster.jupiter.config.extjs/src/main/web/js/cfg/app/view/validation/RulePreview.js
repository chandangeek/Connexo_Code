Ext.define('Cfg.view.validation.RulePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.rulePreview',
    itemId: 'rulePreview',
    requires: [
        'Cfg.model.ValidationRule'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: "Details",
    tools: [
        {
            xtype: 'button',
            icon: '../cfg/resources/images/actionsDetail.png',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            menu: {
                items: [
                    {
                        text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
                        itemId: 'editRule',
                        action: 'editRule'

                    },
                    {
                        text: Uni.I18n.translate('general.delete', 'CFG', 'Delete'),
                        itemId: 'deleteRule',
                        action: 'deleteRule'

                    }
                ]
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
                    html: '<H4>' + Uni.I18n.translate('validation.noRuleSelected', 'MDC', 'No rule selected') + '</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>' + Uni.I18n.translate('validation.selectRule', 'MDC', 'Select a rule to see its details') + '</H5>'
                }
            ]

        },

        {
            xtype: 'form',
            border: false,
            itemId: 'ruleForm',
            layout: {
                type: 'vbox'
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
                    name: 'displayName',
                    fieldLabel: Uni.I18n.translate('validation.Rule', 'CFG', 'Rule'),
                    labelAlign: 'right',
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'active',
                    fieldLabel: Uni.I18n.translate('validation.active', 'CFG', 'Active'),
                    labelAlign: 'right',
                    labelWidth:	250,
                    renderer:function(value){
                        if (value) {
                            return Uni.I18n.translate('general.yes', 'CFG', 'Yes')
                        } else {
                            return Uni.I18n.translate('general.no', 'CFG', 'No')
                        }
                    }
                },

                {
                    xtype: 'container',
                    itemId: 'readingTypesArea',
                    items: []
                },
                ,
                {
                    xtype: 'container',
                    margin: '5 0 0 0',
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
