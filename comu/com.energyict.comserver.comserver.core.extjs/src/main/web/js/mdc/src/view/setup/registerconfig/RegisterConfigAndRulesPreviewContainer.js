Ext.define('Mdc.view.setup.registerconfig.RegisterConfigAndRulesPreviewContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'register-config-and-rules-preview-container',
    itemId: 'registerConfigAndRulesPreviewContainer',

    deviceTypeId: null,
    deviceConfigId: null,
    registerId: null,

    requires: [
        'Mdc.view.setup.registerconfig.RegisterConfigPreview',
        'Mdc.view.setup.registerconfig.RulesForRegisterConfigGrid',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
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
            title: Uni.I18n.translate('registerconfig.validationRules','MDC','Validation rules')
        },
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'validation-rules-for-registerconfig-grid',
                privileges: Cfg.privileges.Validation.fineTuneOnDeviceConfiguration,
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId,
                registerId: this.registerId
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('validation.empty.rules.title', 'MDC', 'No validation rules found'),
                reasons: [
                    Uni.I18n.translate('registerConfig.validationRules.empty.list.item1', 'MDC', 'No validation rules are applied on the register configuration.')
                ]
            },
            previewComponent: {
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
        }
    ]
});