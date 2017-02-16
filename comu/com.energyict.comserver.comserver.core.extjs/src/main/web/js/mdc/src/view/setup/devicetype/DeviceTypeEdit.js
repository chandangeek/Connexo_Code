/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.DeviceTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypeEdit',
    itemId: 'deviceTypeEdit',
    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.store.DeviceCommunicationProtocols',
        'Mdc.store.DeviceTypePurposes'
    ],

    edit: false,
    cancelLink: null,
    deviceCommunicationProtocols: null,
    deviceTypePurposes: null,

    isEdit: function () {
        return this.edit;
    },

    initComponent: function () {
        var me = this;

        this.content = [
            {
                xtype: 'form',
                //width: '100%',
                itemId: 'deviceTypeEditForm',
                ui: 'large',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'deviceTypeEditFormErrors',
                        hidden: true,
                        width: 600
                    },
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'info-panel',
                        hidden: true,
                        maxWidth: 800,
                        minWidth: 650
                    },
                    {
                        xtype: 'combobox',
                        name: 'deviceTypePurpose',
                        fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                        itemId: 'mdc-deviceTypeEdit-typeComboBox',
                        store: me.deviceTypePurposes,
                        queryMode: 'local',
                        displayField: 'localizedValue',
                        valueField: 'deviceTypePurpose',
                        required: true,
                        forceSelection: true,
                        typeAhead: false,
                        msgTarget: 'under',
                        width: 600
                    },
                    {
                        xtype: 'combobox',
                        name: 'deviceProtocolPluggableClass',
                        fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol'),
                        itemId: 'communicationProtocolComboBox',
                        store: me.deviceCommunicationProtocols,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'name',
                        emptyText: Uni.I18n.translate('devicetype.selectProtocol', 'MDC', 'Select a communication protocol...'),
                        required: true,
                        forceSelection: true,
                        typeAhead: true,
                        msgTarget: 'under',
                        width: 600
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        msgTarget: 'under',
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        itemId: 'editDeviceTypeNameField',
                        maxLength: 80,
                        enforceMaxLength: true,
                        width: 600
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                        required: true,
                        itemId: 'device-life-cycle-field',
                        msgTarget: 'under',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'device-life-cycle-combo',
                                name: 'deviceLifeCycleId',
                                width: 335,
                                store: 'Mdc.store.DeviceLifeCycles',
                                emptyText: Uni.I18n.translate('devicetype.selectDeviceLifeCycle', 'MDC', 'Select a device life cycle...'),
                                editable: false,
                                msgTarget: 'under',
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-device-life-cycles',
                                hidden: true,
                                value: '<div style="color: #FF0000">' + Uni.I18n.translate('general.noDeviceLifeCyclesMsg', 'MDC', 'No device life cycles defined') + '</div>',
                                width: 335
                            }
                        ]
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
                                text: me.edit ? Uni.I18n.translate('general.save', 'MDC', 'Save') : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: me.edit ? 'editDeviceType' : 'createDeviceType',
                                itemId: 'createEditButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: me.cancelLink
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }

});
