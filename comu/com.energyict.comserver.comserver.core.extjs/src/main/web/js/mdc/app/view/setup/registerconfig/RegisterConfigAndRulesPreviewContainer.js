Ext.define('Mdc.view.setup.registerconfig.RegisterConfigAndRulesPreviewContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerConfigAndRulesPreviewContainer',
    itemId: 'registerConfigAndRulesPreviewContainer',
    xtype: 'register-config-and-rules-preview-container',
    deviceTypeId: null,
    deviceConfigId: null,
    registerId: null,
    requires: [
        'Mdc.view.setup.registerconfig.RegisterConfigPreview',
        'Mdc.view.setup.registerconfig.RulesForRegisterConfigGrid'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'registerConfigPreview',
            deviceTypeId: this.deviceTypeId,
            deviceConfigId: this.deviceConfigId
        },

        {
            xtype: 'panel',
            ui: 'medium',
            padding: '32 0 0 0',
            itemId: 'rulesForRegisterConfigPreview',
            title: 'test validation rules',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'validation-rules-for-registerconfig-grid',
                    deviceTypeId: this.deviceTypeId,
                    deviceConfigId: this.deviceConfigId,
                    registerId: this.registerId
                },
                {
                    xtype: 'validation-rule-preview',
                    tools: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                            iconCls: 'x-uni-action-iconD',
                            menu: {
                                xtype: 'rules-for-registerconfig-actionmenu'
                            }
                        }
                    ]
                }
            ]

        }






    ]



});