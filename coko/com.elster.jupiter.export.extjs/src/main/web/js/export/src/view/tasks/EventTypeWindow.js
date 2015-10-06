Ext.define('Dxp.view.tasks.EventTypeWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.event-type-window',
    itemId: 'eventTypeWindow',
    closable: false,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    floating: true,
    requires: [
        'Uni.util.FormErrorMessage'
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
                hidden: true,
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
                                xtype: 'radio',
                                itemId: 'des-eventtype-input-radio',
                                boxLabel: Uni.I18n.translate('export.eventType.specifyEventType', 'DES', 'Specify event type'),
                                name: 'rb',
                                inputValue: '0',
                                margin: '10 0 0 0',
                                checked: true
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'specifyEventForm',
                                //width: 800,
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
                                        fieldLabel: Uni.I18n.translate('export.eventType.eventType', 'DES', 'Event type'),
                                        required: true
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
                                boxLabel: Uni.I18n.translate('export.eventType.specifyEventTypeParts', 'DES', 'Specify event type parts'),
                                name: 'rb',
                                inputValue: '1',
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'specifyEventPartsForm',
                                //width: 800,
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
                                        fieldLabel: Uni.I18n.translate('export.eventType.eventType', 'DES', 'Event type')
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-type-combo',
                                        disabled: true,
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceType', 'DES', 'Device type'),
                                        required: true
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-domain-combo',
                                        disabled: true,
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceDomain', 'DES', 'Device domain'),
                                        required: true
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-subdomain-combo',
                                        width: 580,
                                        disabled: true,
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceSubDomain', 'DES', 'Device subdomain'),
                                        required: true
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'des-device-eventoraction-combo',
                                        disabled: true,
                                        width: 580,
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceEventOrAction', 'DES', 'Device event or action'),
                                        required: true
                                    },
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    },

    bbar: [
        {
            xtype: 'container',
            width: 270
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.add','DES','Add'),
            ui: 'action',
            itemId: 'addEventTypeToTask'
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.cancel','DES','Cancel'),
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
    ],

    initComponent: function () {
        var me = this;
        me.on({
            afterrender: {
                fn: me.onAfterRender,
                scope: me,
                single: true
            }
        });

        // custom Vtype for vtype:'EndDeviceEventType'
        Ext.apply(Ext.form.field.VTypes, {
            EndDeviceEventType:  function(v) {
                return /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/.test(v);
            },
            EndDeviceEventTypeText: Uni.I18n.translate('export.eventType.invalid', 'DES', 'Event type is invalid'),
            EndDeviceEventTypeMask: /[\d\.]/i
        });
        me.callParent(arguments);
    },

    onAfterRender: function() {
        var me = this,
            radioEventTypeParts = me.down('#des-eventtypeparts-input-radio'),
            fieldToFocus = me.down('#des-eventtype-input-field');

        radioEventTypeParts.on("change", me.onChange, me);
        fieldToFocus.focus(false, 200);
    },

    onChange: function(field, newValue, oldValue) {
        var me = this,
            radioEventTypeParts = me.down('#des-eventtypeparts-input-radio'),
            partsSelected = radioEventTypeParts.getValue();

        me.down('#specifyEventForm').setDisabled(partsSelected);
        me.down('#des-device-type-combo').setDisabled(!partsSelected);
        me.down('#des-device-domain-combo').setDisabled(!partsSelected);
        me.down('#des-device-subdomain-combo').setDisabled(!partsSelected);
        me.down('#des-device-eventoraction-combo').setDisabled(!partsSelected);
        me.down('#form-errors').hide();
    },

    isFormValid: function() {
        return this.isEventTypeFieldValid();
    },

    isEventTypeFieldValid: function() {
        var radioGroup = this.down('#eventTypeInputMethod'),
            fieldId = radioGroup.getValue().rb === '0' ? '#des-eventtype-input-field' : '#des-eventtype-assembled-field',
            field = this.down(fieldId);

        return this.isFieldNonEmpty(fieldId) &&
            this.isFieldValid(fieldId,
                /^\d{1,2}\.\d{1,2}\.\d{1,3}\.\d{1,3}$/.test(field.getValue()),
                Uni.I18n.translate('export.eventType.invalid', 'DES', 'Event type is invalid')
            );
    },

    isFieldNonEmpty: function(fieldId) {
        var me= this,
            field = me.down(fieldId);
        return me.isFieldValid(
            fieldId,
            field.getValue() !== null && field.getValue().length > 0,
            Uni.I18n.translate('dataExport.requiredField', 'DES', 'This field is required')
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

    getEventType: function() {
        var me = this,
            radioGroup = me.down('#eventTypeInputMethod');

        if (radioGroup.getValue().rb === '0') {
            return me.down('#des-eventtype-input-field').getValue();
        } else {
            return me.down('#des-eventtype-assembled-field').getValue();
        }
    }

});

