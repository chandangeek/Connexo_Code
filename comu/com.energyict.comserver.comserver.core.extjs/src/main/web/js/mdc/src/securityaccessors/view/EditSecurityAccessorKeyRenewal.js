/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.EditSecurityAccessorKeyRenewal', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.editSecurityAccessorKeyRenewal',
    deviceType: null,
    cancelLink: undefined,
    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.securityaccessors.store.SecurityCategoryCommands',
        'Mdc.securityaccessors.store.WrappingSecurityAccessors',
        'Uni.view.form.ComboBoxWithEmptyComponent'

    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        router: this.router,
                        deviceTypeId: this.deviceType.get('id')
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('keyRenewal.edit.title', 'MDC', 'Edit key renewal'),
                layout: 'vbox',
                itemId: 'edit-security-accessor-key-renewal-panel',
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'key-renewal-with-key-renewal-error',
                        width: 550,
                        hidden: true
                    },
                    {
                        xtype: 'form',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        margin: '16 0 0 0',
                        itemId: 'key-renewal-with-form',
                        items: [
                            {
                                xtype: 'combobox',
                                name: 'command',
                                fieldLabel: Uni.I18n.translate('keyRenewal.command', 'MDC', 'Key renewal command'),
                                itemId: 'key-renewal-command-combo',
                                emptyText: Uni.I18n.translate('keyRenewal.selectACommand', 'MDC', 'Select a command...'),
                                store: 'Mdc.securityaccessors.store.SecurityCategoryCommands',
                                displayField: 'name',
                                valueField: 'id',
                                allowBlank: false,
                                editable: false,
                                queryMode: 'local',
                                labelWidth: 200,
                                width: 550
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'key-renewal-no-command',
                                fieldLabel: Uni.I18n.translate('keyRenewal.command', 'MDC', 'Key renewal command'),
                                value: Uni.I18n.translate('keyRenewal.noCommands', 'MDC', 'No commands are available'),
                                fieldStyle: {
                                    color: '#EB5642'
                                },
                                hidden: true
                            },
                            {
                                itemId: 'key-renewal-property-header',
                                margin: '16 0 0 0',
                                hidden: true
                            },
                            {
                                xtype: 'property-form',
                                itemId: 'key-renewal-property-form',
                                margin: '20 0 0 0',
                                defaults: {
                                    labelWidth: 200,
                                    resetButtonHidden: false,
                                    width: 334
                                }
                            },
                            {
                                xtype: 'combobox',
                                name: 'servicekeycommand',
                                fieldLabel: Uni.I18n.translate('serviceKeyRenewal.command', 'MDC', 'Service key renewal command'),
                                itemId: 'service-key-renewal-command-combo',
                                emptyText: Uni.I18n.translate('keyRenewal.selectACommand', 'MDC', 'Select a command...'),
                                store: 'Mdc.securityaccessors.store.SecurityCategoryCommands',
                                displayField: 'name',
                                valueField: 'id',
                                allowBlank: false,
                                editable: false,
                                queryMode: 'local',
                                labelWidth: 200,
                                width: 550
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'service-key-renewal-no-command',
                                fieldLabel: Uni.I18n.translate('serviceKeyRenewal.command', 'MDC', 'Service key renewal command'),
                                value: Uni.I18n.translate('keyRenewal.noCommands', 'MDC', 'No commands are available'),
                                fieldStyle: {
                                    color: '#EB5642'
                                },
                                hidden: true
                            },
                            {
                                itemId: 'service-key-renewal-property-header',
                                margin: '16 0 0 0',
                                hidden: true
                            },
                            {
                                xtype: 'property-form',
                                itemId: 'service-key-renewal-property-form',
                                margin: '20 0 0 0',
                                defaults: {
                                    labelWidth: 200,
                                    resetButtonHidden: false,
                                    width: 334
                                }
                            },
                        ]
                    },
                    {
                        xtype: 'form',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        itemId: 'key-wrapper-with-form',
                        items: [
                            {
                                xtype: 'combobox',
                                name: 'wrapperAccessorId',
                                fieldLabel: Uni.I18n.translate('key.wrapper.label', 'MDC', 'Wrapped by'),
                                itemId: 'key-wrapper-combo',
                                emptyText: Uni.I18n.translate('key.wrapper.message', 'MDC', 'Select a wrapper...'),
                                store: 'Mdc.securityaccessors.store.WrappingSecurityAccessors',
                                displayField: 'name',
                                valueField: 'id',
                                required: false,
                                allowBlank: false,
                                editable: true,
                                queryMode: 'local',
                                labelWidth: 200,
                                width: 550
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        labelWidth: 250,
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'key-renewal-add-button',
                                text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                ui: 'action',
                                action: 'save'
                            },
                            {
                                xtype: 'button',
                                itemId: 'key-renewal-cancel-button',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                href: me.cancelLink,
                                action: 'cancel'
                            }
                        ]
                    }
                ]
            }];

        me.callParent(arguments);
    }
});