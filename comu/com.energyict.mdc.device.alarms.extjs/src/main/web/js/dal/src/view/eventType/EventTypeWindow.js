/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.eventType.EventTypeWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.event-type-window',
    itemId: 'eventTypeWindow',
    closable: false,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    floating: true,
    comboBoxValueForAll: -1,
    defaultFocus: 'des-eventtype-input-field',
    requires: [
        'Uni.util.FormErrorMessage',
        'Dal.model.eventType.EndDeviceEventTypePart'
    ],
    items: {
        xtype: 'form',
        border: false,
        itemId: 'eventTypeForm',
        items: [
            {
                xtype: 'uni-form-error-message',
                name: 'form-errors',
                itemId: 'form-errors',
                margin: '10 0 10 0',
                hidden: true
            },
            {
                xtype: 'radiogroup',
                itemId: 'eventTypeInputMethod',
                required: true,
                width: 600,
                columns: 1,
                vertical: true,
                items: [
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                itemId: 'des-eventtype-container',
                                required: true,
                                msgTarget: 'under',
                                items: [
                                    {
                                        xtype: 'radio',
                                        itemId: 'des-eventtype-input-radio',
                                        boxLabel: '<span style="display:inline-block; float: left; margin-right:7px;" >' + Uni.I18n.translate('export.eventType.specifyEventType', 'DAL', 'Specify event type') + '</span>'
                                            + '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="'
                                            + Uni.I18n.translate('export.eventType.tooltip', 'DAL', "The wildcard '*' can be used in each part of the event type and will match all possible values.")
                                            + '"></span>',
                                        name: 'rb',
                                        inputValue: '0',
                                        margin: '10 0 0 0',
                                        checked: true
                                    }
                                ]
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'specifyEventForm',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        itemId: 'des-eventtype-input-field',
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.eventType', 'DAL', 'Event type'),
                                        required: true
                                    },
                                    {
                                        xtype: 'textfield',
                                        itemId: 'alm-eventtype-device-code-specific',
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.deviceCode', 'DAL', 'Device code'),
                                        required: true,
                                        maskRe: /[0-9.]/,
                                        disabled: true
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'radio',
                                itemId: 'des-eventtypeparts-input-radio',
                                boxLabel: Uni.I18n.translate('alarm.eventType.specifyEventTypeParts', 'DAL', 'Specify event type parts'),
                                name: 'rb',
                                inputValue: '1'
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'specifyEventPartsForm',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        itemId: 'des-eventtype-assembled-field',
                                        disabled: true,
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.eventType', 'DAL', 'Event type'),
                                        value: '*.*.*.*'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-type-combo',
                                        disabled: true,
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.deviceType', 'DAL', 'Device type'),
                                        required: true,
                                        store: 'Dal.store.eventType.DeviceTypes',
                                        queryMode: 'local',
                                        editable: false,
                                        displayField: 'displayName',
                                        valueField: 'value'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-domain-combo',
                                        disabled: true,
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.deviceDomain', 'DAL', 'Device domain'),
                                        required: true,
                                        store: 'Dal.store.eventType.DeviceDomains',
                                        queryMode: 'local',
                                        editable: false,
                                        displayField: 'displayName',
                                        valueField: 'value'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-subdomain-combo',
                                        width: 580,
                                        disabled: true,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.deviceSubDomain', 'DAL', 'Device subdomain'),
                                        required: true,
                                        store: 'Dal.store.eventType.DeviceSubDomains',
                                        queryMode: 'local',
                                        editable: false,
                                        displayField: 'displayName',
                                        valueField: 'value'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-eventoraction-combo',
                                        disabled: true,
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.deviceEventOrAction', 'DAL', 'Device event or action'),
                                        required: true,
                                        store: 'Dal.store.eventType.DeviceEventOrActions',
                                        queryMode: 'local',
                                        editable: false,
                                        displayField: 'displayName',
                                        valueField: 'value'
                                    },
                                    {
                                        xtype: 'textfield',
                                        itemId: 'alm-eventtype-device-code-part',
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('alarm.eventType.deviceCode', 'DAL', 'Device code'),
                                        required: true,
                                        maskRe: /[0-9.]/,
                                        disabled: true
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    },



    initComponent: function () {
        var me = this;

        me.bbar = [
            {
                xtype: 'container',
                width: 270
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.add','DAL','Add'),
                ui: 'action',
                itemId: 'addEventTypeToAlarmRule',
                type: me.type
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.cancel','DAL','Cancel'),
                action: 'cancel',
                ui: 'link',
                listeners: {
                    click: {
                        fn: function () {
                            this.up('#eventTypeWindow').destroy();
                        }
                    }
                }
            }
        ];

        me.on({
            afterrender: {
                fn: me.onAfterRender,
                scope: me,
                single: true
            }
        });
        me.callParent(arguments);
    },

    onAfterRender: function() {
        var me = this,
            radioEventTypeParts = me.down('#des-eventtypeparts-input-radio'),
            deviceTypeCombo = me.down('#des-device-type-combo'),
            deviceDomainCombo = me.down('#des-device-domain-combo'),
            deviceSubDomainCombo = me.down('#des-device-subdomain-combo'),
            eventTypeSpec = me.down('#des-eventtype-input-field'),
            eventTypePart = me.down('#des-eventtype-assembled-field'),
            deviceEventOrActionCombo = me.down('#des-device-eventoraction-combo');

        radioEventTypeParts.on("change", me.onChange, me);
        eventTypeSpec.on("change", me.onChangeEventTypeSpec, me);
        eventTypePart.on("change", me.onChangeEventTypePart, me);
        deviceTypeCombo.setValue(me.comboBoxValueForAll);
        deviceDomainCombo.setValue(me.comboBoxValueForAll);
        deviceSubDomainCombo.setValue(me.comboBoxValueForAll);
        deviceTypeCombo.on("change", me.updateEventTypeField, me);
        deviceDomainCombo.on("change", me.updateEventTypeField, me);
        deviceSubDomainCombo.on("change", me.updateEventTypeField, me);
        deviceEventOrActionCombo.on("change", me.updateEventTypeField, me);
        deviceEventOrActionCombo.setValue(me.comboBoxValueForAll);
    },

    onChange: function(field, newValue, oldValue) {
        var me = this,
            radioEventTypeParts = me.down('#des-eventtypeparts-input-radio'),
            partsSelected = radioEventTypeParts.getValue(),
            fieldToFocus = partsSelected ? me.down('#des-device-type-combo') : me.down('#des-eventtype-input-field');

        me.down('#specifyEventForm').setDisabled(partsSelected);
        me.down('#des-device-type-combo').setDisabled(!partsSelected);
        me.down('#des-device-domain-combo').setDisabled(!partsSelected);
        me.down('#des-device-subdomain-combo').setDisabled(!partsSelected);
        me.down('#des-device-eventoraction-combo').setDisabled(!partsSelected);
        fieldToFocus.focus(false, 200);
        me.down('#form-errors').hide();
    },

    onChangeEventTypeSpec: function(field, newValue, oldValue){
        this.down('#alm-eventtype-device-code-specific').setDisabled(newValue != '0.0.0.0');
    },

    onChangeEventTypePart: function(field, newValue, oldValue){
        this.down('#alm-eventtype-device-code-part').setDisabled(newValue != '0.0.0.0');
    },

    updateEventTypeField: function() {
        var me = this,
            deviceTypeCombo = me.down('#des-device-type-combo'),
            deviceDomainCombo = me.down('#des-device-domain-combo'),
            deviceSubDomainCombo = me.down('#des-device-subdomain-combo'),
            deviceEventOrActionCombo = me.down('#des-device-eventoraction-combo'),
            assembledEventTypeField = me.down('#des-eventtype-assembled-field');

        assembledEventTypeField.setValue(
            (deviceTypeCombo.getValue() === me.comboBoxValueForAll ? '*' : deviceTypeCombo.getValue()) + '.'
            + (deviceDomainCombo.getValue() === me.comboBoxValueForAll ? '*' : deviceDomainCombo.getValue() ) + '.'
            + (deviceSubDomainCombo.getValue() === me.comboBoxValueForAll ? '*' : deviceSubDomainCombo.getValue() ) + '.'
            + (deviceEventOrActionCombo.getValue() === me.comboBoxValueForAll ? '*' : deviceEventOrActionCombo.getValue())
        );
    },

    isFormValid: function(store) {
        return this.isEventTypeFieldValid(store);
    },

    isEventTypeFieldValid: function(store) {
        var me = this,
            radioGroup = this.down('#eventTypeInputMethod'),
            fieldId = radioGroup.getValue().rb === '0' ? '#des-eventtype-input-field' : '#des-eventtype-assembled-field',
            field = this.down(fieldId);

        return this.isFieldNonEmpty(fieldId) &&
            this.isFieldValid(fieldId,
                /^(\d{1,2}|\*)\.(\d{1,2}|\*)\.(\d{1,3}|\*)\.(\d{1,3}|\*)$/.test(field.getValue()),
                Uni.I18n.translate('export.eventType.invalid', 'DAL', 'Event type is invalid')
            ) &&
            this.isFieldValid(fieldId,
                this.isPartValueValid(1, field.getValue()),
                Uni.I18n.translate('alarm.eventType.invalid.partx', 'DAL', 'Event type is invalid (part {0})', 1)
            ) &&
            this.isFieldValid(fieldId,
                this.isPartValueValid(2, field.getValue()),
                Uni.I18n.translate('alarm.eventType.invalid.partx', 'DAL', 'Event type is invalid (part {0})', 2)
            ) &&
            this.isFieldValid(fieldId,
                this.isPartValueValid(3, field.getValue()),
                Uni.I18n.translate('alarm.eventType.invalid.partx', 'DAL', 'Event type is invalid (part {0})', 3)
            ) &&
            this.isFieldValid(fieldId,
                this.isPartValueValid(4, field.getValue()),
                Uni.I18n.translate('alarm.eventType.invalid.partx', 'DAL', 'Event type is invalid (part {0})', 4)
            ) &&
            this.isFieldValid(fieldId,
                store.findExact('eventFilterCode', me.getEventType()) === -1,
                Uni.I18n.translate('alarm.eventType.alreadyAssigned', 'DAL', 'Event type is already assigned to alarm creation rule')
            );
    },

    isFieldNonEmpty: function(fieldId) {
        var me= this,
            field = me.down(fieldId);
        return me.isFieldValid(
            fieldId,
            field.getValue() !== null && field.getValue().length > 0,
            Uni.I18n.translate('alarm.requiredField', 'DAL', 'This field is required')
        );
    },

    isFieldValid: function(fieldId, conditionToBeValid, errorMsg) {
        var me = this,
            component = me.down(fieldId);

        if (conditionToBeValid) {
            component.unsetActiveError();
            me.down('#form-errors').hide();
        } else {
            component.setActiveError(errorMsg);
            me.down('#form-errors').show();
        }
        component.doComponentLayout();
        return conditionToBeValid;
    },

    isPartValueValid: function(partNr, eventTypeAsString) {
        var fieldId,
            parts = eventTypeAsString.split('.');
        switch(partNr) {
            case 1: fieldId = '#des-device-type-combo'; break;
            case 2: fieldId = '#des-device-domain-combo'; break;
            case 3: fieldId = '#des-device-subdomain-combo'; break;
            case 4: fieldId = '#des-device-eventoraction-combo'; break;
        }
        return parts[partNr-1] === '*' ||
            this.down(fieldId).store.findExact('value', parseInt(parts[partNr-1])) != -1;
    },

    getEventType: function() {
        var me = this,
            radioGroup = me.down('#eventTypeInputMethod');

        if (radioGroup.getValue().rb === '0') {
            return me.down('#des-eventtype-input-field').getValue();
        } else {
            return me.down('#des-eventtype-assembled-field').getValue();
        }
    },

    getDeviceTypeName: function() {
        return this.getComboBoxDisplayValue(1);
    },

    getDeviceDomainName: function() {
        return this.getComboBoxDisplayValue(2);
    },

    getDeviceSubDomainName: function() {
        return this.getComboBoxDisplayValue(3);
    },

    getDeviceEventOrActionName: function() {
        return this.getComboBoxDisplayValue(4);
    },

    getDeviceCode: function() {
        if(this.down('#alm-eventtype-device-code-specific').getValue() != ''){
            return this.down('#alm-eventtype-device-code-specific').getValue();
        }else if(this.down('#alm-eventtype-device-code-part').getValue() != ''){
            return this.down('#alm-eventtype-device-code-part').getValue();
        }
    },

    getComboBoxDisplayValue: function(partNr) {
        var me = this,
            radioGroup = me.down('#eventTypeInputMethod'),
            fieldId,
            comboBox,
            selectedValue;

        switch(partNr) {
            case 1: fieldId = '#des-device-type-combo'; break;
            case 2: fieldId = '#des-device-domain-combo'; break;
            case 3: fieldId = '#des-device-subdomain-combo'; break;
            case 4: fieldId = '#des-device-eventoraction-combo'; break;
        }
        comboBox = me.down(fieldId);

        if (radioGroup.getValue().rb === '0') {
            var inputField = me.down('#des-eventtype-input-field'),
                parts = inputField.getValue().split('.');
            selectedValue = parts[partNr-1] === '*' ? me.comboBoxValueForAll : parseInt(parts[partNr-1]);
        } else {
            selectedValue = comboBox.getValue();
        }
        if (selectedValue === me.comboBoxValueForAll) {
            return '*';
        }

        var index = comboBox.store.findExact('value', selectedValue);
        if (index === -1) { // shouldn't be the case, though
            return '?';
        }
        var record = comboBox.store.getAt(index);
        if (!record) { // shouldn't be the case, though
            return '?';
        }
        return record.get('displayName');
    }

});

