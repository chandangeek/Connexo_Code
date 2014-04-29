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
                margin: '20 0 0 0',
                border: 0,
                defaults: {
                    labelWidth: 250
                },
                layout: {
                    type: 'vbox',
                    align: 'stretch'

                },
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'propertiesTitle',
                        fieldLabel: this.propertiesTitle,
                        labelStyle: 'font-size: 17px;',
                        width: 395,
                        hidden: true
                    }
                ]
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    width: 395,
                    msgTarget: 'under',
                    margin: '0 0 5 0 ',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    inputType: 'password',
                    size: 200,
                    margin: '0 5 5 0',
                    width: 395,
                    msgTarget: 'under',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 5 0',
                    width: 395,
                    vtype: 'hexstring',
                    msgTarget: 'under',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,

                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textareafield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 5 0',
                    width: 395,
                    grow: true,
                    anchor: '100%',
                    msgTarget: 'under',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,

                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'numberfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: value,
                    size: 15,
                    margin: '0 0 5 0',
                    width: 200,
                    hideTrigger: true,
                    keyNavEnabled: false,
                    mouseWheelEnabled: false,
                    minValue: minvalue,
                    maxValue: maxvalue,
                    allowDecimals: allowdecimals,
                    msgTarget: 'under',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,

                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'checkbox',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    checked: value,
                    margin: '0 5 5 0',
                    cls: 'check',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,

                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'rg' + key,
                    allowBlank: false,
                    required: required,
                    vertical: true,
                    columns: 1,
                    items: [
                        {
                            boxLabel: 'true',
                            name: 'rb',
                            itemId: 'rb_1_' + key,
                            checked: value1,
                            inputValue: 1,
                            margin: '0 10 5 0'
                        },
                        {
                            boxLabel: 'false',
                            name: 'rb',
                            itemId: 'rb_2_' + key,
                            checked: value2,
                            inputValue: 0,
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

                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'datefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'date' + key,
                    value: value,
                    format: 'd/m/Y',
                    altFormats: 'd.m.Y|d m Y',
                    margin: '0 5 5 0',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'timefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'time' + key,
                    value: value,
                    format: 'H:i:s',
                    margin: '0 5 5 0',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'datefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'date' + key,
                    value: dateValue,
                    format: 'd/m/Y',
                    altFormats: 'd.m.Y|d m Y',
                    margin: '0 5 5 0',
                    required: required
                },
                {
                    xtype: 'timefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'time' + key,
                    value: timeValue,
                    format: 'H:i:s',
                    margin: '0 5 5 0',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'numberfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: count,
                    size: 200,
                    margin: '0 5 5 0',
                    width: 200,
                    required: required
                },
                {
                    xtype: 'combobox',
                    itemId: 'tu_' + key,
                    name: 'tu_' + key,
                    store: timeUnitsStore,
                    queryMode: 'local',
                    displayField: 'timeUnit',
                    valueField: 'timeUnit',
                    value: unit,
                    size: 50,
                    margin: '0 5 5 0',
                    width: 200,
                    forceSelection: false,
                    required: required

                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
                }
            ]
        });
    },
    addCodeTableProperty: function (key, value) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'codeTableSelector',
            name: key,
            fieldLabel: key,
            itemId: key,
            value: value,
            width: 600,
            handler: function (picker, date) {
                console.log('dit is een test');
            }
        })
    },
    addCodeTablePropertyWithSelectionWindow: function (key, value, defaultValue, required) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    value: value,
                    itemId: key,
                    size: 75,
                    margin: '0 5 5 0',
                    width: 395,
                    required: required
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
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    value: value,
                    itemId: key,
                    size: 75,
                    margin: '0 5 5 0',
                    width: 395,
                    required: required
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
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    value: value,
                    itemId: key,
                    size: 75,
                    margin: '0 5 5 0',
                    width: 350,
                    required: required
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
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'combobox',
                    itemId: key,
                    name: key,
                    fieldLabel: key,
                    store: store,
                    queryMode: 'local',
                    displayField: 'value',
                    valueField: 'key',
                    value: selectedValue,
                    size: 50,
                    margin: '0 0 5 0',
                    width: 390,
                    forceSelection: exhaustive,
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'combobox',
                    itemId: key,
                    name: key,
                    fieldLabel: key,
                    store: store,
                    queryMode: 'local',
                    displayField: 'value',
                    valueField: 'value',
                    value: selectedValue,
                    size: 50,
                    margin: '0 0 5 0',
                    width: 200,
                    fieldStyle: 'text-align:right;',
                    forceSelection: exhaustive,
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 5 0',
                    width: 350,
                    vtype: 'ean18',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
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
            defaults: {
                hideLabel: true
            },
            margin: this.fieldMargin,
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 5 0',
                    width: 350,
                    vtype: 'ean13',
                    required: required
                },
                {
                    xtype: 'defaultButton',
                    key: key,
                    default: defaultValue
                }
            ]
        });
    }
})
;