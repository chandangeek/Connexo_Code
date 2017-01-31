/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.property.Edit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.propertyEdit',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    itemId: 'propertyEdit',
    border: 0,
    requires: ['Ext.form.Panel',
        'Mdc.view.setup.property.CodeTableSelector',
        'Mdc.view.setup.property.CodeTable',
        'Mdc.view.setup.property.UserFileReference',
        'Mdc.view.setup.property.LoadProfileType',
        'Mdc.widget.TimeInfoField',
        'Mdc.view.setup.property.DefaultButton'
    ],
    fieldMargin: '0 0 0 0',

    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                itemId: 'propertiesform',
                border: 0,
                defaults: {
                    labelWidth: 250
                },
                layout: {
                    type: 'vbox',
                    align: 'stretch'

                },
                items: []
            }
        ];

        this.callParent(arguments);
    },
    addTextProperty: function (key, text, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: text,
                    width: 320,
                    msgTarget: 'under',
                    margin: '0 0 5 0 '
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addPasswordProperty: function (key, text, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            margin: this.fieldMargin,
            required: required,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: text,
                    inputType: 'password',
                    margin: '0 5 5 0',
                    width: 320,
                    msgTarget: 'under'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addHexStringProperty: function (key, text, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: text,
                    margin: '0 5 5 0',
                    width: 320,
                    vtype: 'hexstring',
                    msgTarget: 'under'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addTextAreaProperty: function (key, text, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            margin: this.fieldMargin,
            required: required,
            items: [
                {
                    xtype: 'textareafield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: text,
                    margin: '0 5 5 0',
                    width: 320,
                    grow: true,
                    anchor: '100%',
                    msgTarget: 'under'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addNumberProperty: function (key, value, minvalue, maxvalue, allowdecimals, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'numberfield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: value,
                    margin: '0 0 5 0',
                    width: 128,
                    hideTrigger: true,
                    keyNavEnabled: false,
                    mouseWheelEnabled: false,
                    minValue: minvalue,
                    maxValue: maxvalue,
                    allowDecimals: allowdecimals,
                    msgTarget: 'under'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addBooleanProperty: function (key, value, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'checkbox',
                    name: 'properties.' + key,
                    itemId: key,
                    checked: value,
                    margin: '0 5 5 0',
                    cls: 'check',
                    msgTarget: 'under'
                }
                ,
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addNullableBooleanProperty: function (key, value1, value2, value3, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            margin: this.fieldMargin,
            required: required,
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: key,
                    name: 'properties.' + key,
                    allowBlank: false,
                    vertical: true,
                    columns: 1,
                    items: [
                        {
                            boxLabel: 'true',
                            name: 'rb',
                            itemId: 'rb_1_' + key,
                            checked: value1,
                            inputValue: true,
                            margin: '0 10 5 0'
                        },
                        {
                            boxLabel: 'false',
                            name: 'rb',
                            itemId: 'rb_2_' + key,
                            checked: value2,
                            inputValue: false,
                            margin: '0 10 5 0'
                        },
                        {
                            boxLabel: 'None',
                            name: 'rb',
                            itemId: 'rb_3_' + key,
                            checked: value3,
                            inputValue: null,
                            margin: '0 10 5 0'
                        }
                    ]
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addDateProperty: function (key, value, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'datefield',
                    name: 'properties.' + key,
                    itemId: 'date' + key,
                    value: value,
                    format: 'd/m/Y',
                    altFormats: 'd.m.Y|d m Y',
                    margin: '0 5 5 0',
                    width: 128
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addTimeProperty: function (key, value, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'timefield',
                    name: 'properties.' + key,
                    itemId: 'time' + key,
                    value: value,
                    format: 'H:i:s',
                    margin: '0 5 5 0',
                    width: 128
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addDateTimeProperty: function (key, dateValue, timeValue, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'datefield',
                    name: 'properties.' + key,
                    itemId: 'date' + key,
                    value: dateValue,
                    format: 'd/m/Y',
                    altFormats: 'd.m.Y|d m Y',
                    margin: '0 5 5 0',
                    width: 128,
                    required: required
                },
                {
                    xtype: 'timefield',
                    name: 'properties.' + key,
                    itemId: 'time' + key,
                    value: timeValue,
                    format: 'H:i:s',
                    margin: '0 5 5 0',
                    width: 128,
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addTimeDurationProperty: function (key, count, unit, timeUnitsStore, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'numberfield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: count,
                    margin: '0 5 5 0',
                    width: 128,
                    required: required
                },
                {
                    xtype: 'combobox',
                    itemId: 'tu_' + key,
                    name: 'properties.' + key,
                    store: timeUnitsStore,
                    queryMode: 'local',
                    displayField: 'timeUnit',
                    valueField: 'timeUnit',
                    value: unit,
                    margin: '0 0 5 0',
                    width: 128,
                    forceSelection: false,
                    required: required

                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addCodeTablePropertyWithSelectionWindow: function (key, value, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    value: value,
                    itemId: key,
                    margin: '0 5 5 0',
                    width: 320,
                    readOnly: true
                },
                {
                    xtype: 'button',
                    name: 'btn_' + key,
                    itemId: 'btn_' + key,
                    text: '...',
                    scale: 'small',
                    action: 'showCodeTable',
                    margin: '0 5 5 0'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addUserReferenceFilePropertyWithSelectionWindow: function (key, value, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    value: value,
                    itemId: key,
                    margin: '0 5 5 0',
                    width: 320,
                    readOnly: true
                },
                {
                    xtype: 'button',
                    name: 'btn_' + key,
                    itemId: 'btn_' + key,
                    text: '...',
                    scale: 'small',
                    action: 'showUserFileReference',
                    margin: '0 5 5 0'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addLoadProfileTypePropertyWithSelectionWindow: function (key, value, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    value: value,
                    itemId: key,
                    margin: '0 5 5 0',
                    width: 320,
                    readOnly: true
                },
                {
                    xtype: 'button',
                    name: 'btn_' + key,
                    itemId: 'btn_' + key,
                    text: '...',
                    scale: 'small',
                    action: 'showLoadProfileType',
                    margin: '0 5 5 0'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addComboBoxTextProperty: function (key, store, selectedValue, exhaustive, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'combobox',
                    itemId: key,
                    name: 'properties.' + key,
                    store: store,
                    queryMode: 'local',
                    displayField: 'value',
                    valueField: 'key',
                    value: selectedValue,
                    margin: '0 0 5 0',
                    width: 320,
                    forceSelection: exhaustive
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addComboBoxNumberProperty: function (key, store, selectedValue, exhaustive, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'combobox',
                    itemId: key,
                    name: 'properties.' + key,
                    store: store,
                    queryMode: 'local',
                    displayField: 'value',
                    valueField: 'value',
                    value: selectedValue,
                    margin: '0 0 5 0',
                    width: 128,
                    fieldStyle: 'text-align:right;',
                    forceSelection: exhaustive
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addEan18StringProperty: function (key, text, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: text,
                    margin: '0 5 5 0',
                    width: 320
                   // vtype: 'ean18'
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    },
    addEan13StringProperty: function (key, text, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            required: required,
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: 'properties.' + key,
                    itemId: key,
                    value: text,
                    margin: '0 5 5 0',
                    width: 320
               //     vtype: 'ean13',
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    defaultValue: defaultValue
                }
            ]
        });
    }
})
;