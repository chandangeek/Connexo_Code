/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceProtocolDialectEdit',
    itemId: 'deviceProtocolDialectEdit',
    edit: false,
    deviceId: null,

    required: [
        'Uni.property.form.Property'
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#addEditButton').action = 'editDeviceProtocolDialect';
        } else {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'addDeviceProtocolDialect';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'protocolLink'
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'deviceProtocolDialectEditAddTitle',
                layout: {
                    type: 'vbox'
                },

                items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceProtocolDialectEditForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                        ],
                        returnLink: me.returnLink,
                        device: me.device
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'editProtocolDialectsDetailsTitle',
                        hidden: true,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('protocolDialect.protocolDialectDetails', 'MDC', 'Protocol dialect details'),
                                renderer: function() {
                                    return ''; // No dash!
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('protocolDialect.noAttributesDefined', 'MDC', 'No attributes defined'),
                        hidden: true,
                        itemId: 'noAttributesDefinedLabel'
                    },
                    {
                        xtype: 'property-form',
                        width: '100%'
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceProtocolDialectEditButtonsForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'createAction',
                                        itemId: 'addEditButton'
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.restoreToDefaultSettings', 'MDC', 'Restore to default settings'),
                                        iconCls: 'icon-rotate-ccw3',
                                        itemId: 'restoreAllButton',
                                        action: 'restoreAll'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/devices/' + encodeURIComponent(this.deviceId) + '/'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        ;
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#addEditButton').action = 'editDeviceProtocolDialect';
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'createDeviceProtocolDialect';
        }
        this.down('#cancelLink').href = this.returnLink;

    }

})
;



