/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.FirmwareOptionsSignatureEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Fwc.model.FirmwareManagementOptions',
        'Fwc.store.SecurityAccessors'
    ],
    alias: 'widget.firmware-options-signature-edit',
    itemId: 'firmware-options-signature-edit',
    deviceType: null,

    initComponent: function () {
        this.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.firmwareSignatureCheck.edit', 'FWC', 'Edit firmware signature check'),
                ui: 'large',
                border: false,
                width: 600,
                itemId: 'firmwareSignatureCheck',
                layout: {
                    type: 'vbox'
                },
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.securityAccessor.edit', 'FWC', 'Security accessor'),
                        margin: '0 16 0 0',
                        allowBlank: false,
                        editable: false,
                        validateOnChange: false,
                        validateOnBlur: false,
                        itemId: 'securityAccessorCombo',
                        store: 'Fwc.store.SecurityAccessors',
                        displayField: 'name',
                        valueField: 'id'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.save', 'FWC', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'saveOptionsAction',
                                itemId: 'saveButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: '#/administration/devicetypes/' + this.deviceType.data.id + '/firmwareversions'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },
    getSecurityAccessorId: function () {
        return this.down('#securityAccessorCombo').getValue();
    }
});



