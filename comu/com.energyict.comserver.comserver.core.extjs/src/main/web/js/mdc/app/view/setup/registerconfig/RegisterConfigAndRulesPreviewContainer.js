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
        'Uni.view.container.PreviewContainer'
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
            title: 'Validation rules'
        },
        {
            xtype: 'preview-container',
            grid: {
                xtype: 'validation-rules-for-registerconfig-grid',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId,
                registerId: this.registerId
            },
            emptyComponent: {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'left'
                },
                minHeight: 20,
                items: [
                    {
                        xtype: 'image',
                        margin: '0 10 0 0',
                        src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                        height: 20,
                        width: 20
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'component',
                                html: '<h4>' + Uni.I18n.translate('registerConfig.validationRules.empty.title', 'MDC', 'No validation rules found') + '</h4><br>' +
                                    Uni.I18n.translate('registerConfig.validationRules.empty.detail', 'MDC', 'This could be because:') + '<ul>' +
                                    '<li>' + Uni.I18n.translate('registerConfig.validationRules.empty.list.item1', 'MDC', 'No validation rules are applied on the channel configuration.') + '</li>' +
                                    '<li>' + Uni.I18n.translate('registerConfig.validationRules.empty.list.item2', 'MDC', 'Validation rules exists, but you do not have permission to view them.') + '</li>' +
                                    '</ul>'
                            }
                        ]
                    }
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
    ],

    updateRegisterConfig: function (registerConfig) {

    }

});