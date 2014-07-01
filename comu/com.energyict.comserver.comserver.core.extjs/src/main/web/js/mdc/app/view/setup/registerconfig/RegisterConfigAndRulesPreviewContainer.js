Ext.define('Mdc.view.setup.registerconfig.RegisterConfigAndRulesPreviewContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerConfigAndRulesPreviewContainer',
    itemId: 'registerConfigAndRulesPreviewContainer',
    deviceTypeId: null,
    deviceConfigId: null,
    registerId: null,
    requires: [
        'Mdc.view.setup.registerconfig.RegisterConfigPreview',
        'Mdc.view.setup.registerconfig.RulesForRegisterConfigGrid',
        'Mdc.view.setup.registerconfig.RuleForRegisterConfigPreview'
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
                    xtype: 'ruleForRegisterConfigPreview'
                }
            ]

        }






    ]



});