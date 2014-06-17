Ext.define('Mdc.view.setup.validation.RulesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'validation-rules-overview',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.validation.RuleSetsGrid',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigId,
                        toggle: 8
                    }
                ]
            }
        ];

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRules', 'MDC', 'Validation rules'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'validation-rulesets-grid',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigId: me.deviceConfigId
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
                                            html: '<h4>' + Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rule sets found') + '</h4><br>' +
                                                Uni.I18n.translate('validation.empty.detail', 'MDC', 'There are no validation rule sets. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rule sets have been added yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('validation.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            xtype: 'button',
                                            text: Uni.I18n.translate('validation.addValidationRuleSets', 'MDC', 'Add validation rule sets'),
                                            ui: 'action',
                                            href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrules/add'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            // TODO
                            xtype: 'component',
                            html: 'Here be dragons.'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});