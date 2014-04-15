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
        'Mdc.widget.TimeInfoField'
    ],

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

                ]
            }
        ];

        this.callParent(arguments);
    },
    addTextProperty: function (key, text, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    width: 395,
                    msgTarget: 'under'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    margin: '0 0 0 5',
                    hidden: hidden
                }
            ]
        });
    },
    addPasswordProperty: function (key, text, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    inputType: 'password',
                    size: 200,
                    margin: '0 5 0 0',
                    width: 395,
                    msgTarget: 'under'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addHexStringProperty: function (key, text, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 0 0',
                    width: 395,
                    vtype: 'hexstring',
                    msgTarget: 'under'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addTextAreaProperty: function (key, text, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textareafield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 0 0',
                    width: 395,
                    grow: true,
                    anchor: '100%',
                    msgTarget: 'under'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addNumberProperty: function (key, value, minvalue, maxvalue, allowdecimals, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'numberfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: value,
                    size: 15,
                    margin: '0 5 0 0',
                    width: 200,
                    hideTrigger: true,
                    keyNavEnabled: false,
                    mouseWheelEnabled: false,
                    minValue: minvalue,
                    maxValue: maxvalue,
                    allowDecimals: allowdecimals,
                    msgTarget: 'under'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addBooleanProperty: function (key, value, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'checkbox',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    checked: value,
                    margin: '0 5 0 0',
                    cls: 'check'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addNullableBooleanProperty: function (key, value1, value2, value3, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'rg' + key,
                    allowBlank: false,
                    items: [
                        {
                            boxLabel: 'true',
                            name: 'rb',
                            itemId: 'rb_1_' + key,
                            checked: value1,
                            inputValue: 1,
                            margin: '0 10 0 0'
                        },
                        {
                            boxLabel: 'false',
                            name: 'rb',
                            itemId: 'rb_2_' + key,
                            checked: value2,
                            inputValue: 0,
                            margin: '0 10 0 0'
                        },
                        {
                            boxLabel: 'None',
                            name: 'rb',
                            itemId: 'rb_3_' + key,
                            checked: value3,
                            inputValue: null,
                            margin: '0 10 0 0'
                        }
                    ]
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addDateProperty: function (key, value, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'datefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'date' + key,
                    value: value,
                    format: 'd/m/Y',
                    altFormats: 'd.m.Y|d m Y',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addTimeProperty: function (key, value, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'timefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'time' + key,
                    value: value,
                    format: 'H:i:s',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addDateTimeProperty: function (key, dateValue, timeValue, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'datefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'date' + key,
                    value: dateValue,
                    format: 'd/m/Y',
                    altFormats: 'd.m.Y|d m Y',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'timefield',
                    name: key,
                    fieldLabel: key,
                    itemId: 'time' + key,
                    value: timeValue,
                    format: 'H:i:s',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addTimeDurationProperty: function (key, count, unit, timeUnitsStore, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'numberfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: count,
                    size: 200,
                    margin: '0 5 0 0',
                    width: 200
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
                    margin: '0 5 0 0',
                    width: 200,
                    forceSelection: false

                },

                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addCodeTableProperty: function (key, value, hidden) {
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
    addCodeTablePropertyWithSelectionWindow: function (key, value, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    value: value,
                    itemId: key,
                    size: 75,
                    margin: '0 5 0 0',
                    width: 395
                },
                {
                    xtype: 'button',
                    name: 'btn_' + key,
                    itemId: 'btn_' + key,
                    text: '...',
                    scale: 'small',
                    action: 'showCodeTable',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addUserReferenceFilePropertyWithSelectionWindow: function (key, value, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    value: value,
                    itemId: key,
                    size: 75,
                    margin: '0 5 0 0',
                    width: 395
                },
                {
                    xtype: 'button',
                    name: 'btn_' + key,
                    itemId: 'btn_' + key,
                    text: '...',
                    scale: 'small',
                    action: 'showUserFileReference',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addLoadProfileTypePropertyWithSelectionWindow: function (key, value, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    value: value,
                    itemId: key,
                    size: 75,
                    margin: '0 5 0 0',
                    width: 350
                },
                {
                    xtype: 'button',
                    name: 'btn_' + key,
                    itemId: 'btn_' + key,
                    text: '...',
                    scale: 'small',
                    action: 'showLoadProfileType',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addComboBoxTextProperty: function (key, store, selectedValue, exhaustive, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
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
                    margin: '0 5 0 0',
                    width: 350,
                    forceSelection: exhaustive
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addComboBoxNumberProperty: function (key, store, selectedValue, exhaustive, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
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
                    margin: '0 5 0 0',
                    width: 200,
                    fieldStyle: 'text-align:right;',
                    forceSelection: exhaustive
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addEan18StringProperty: function (key, text, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 0 0',
                    width: 350,
                    vtype: 'ean18'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    },
    addEan13StringProperty: function (key, text, hidden) {
        var me = this;
        me.down('#propertiesform').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'under',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: text,
                    size: 200,
                    margin: '0 5 0 0',
                    width: 350,
                    vtype: 'ean13'
                },
                {
                    xtype: 'button',
                    icon: '../mdc/resources/images/redo.png',
                    tooltip: 'Restore to default value',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    scale: 'small',
                    action: 'delete',
                    disabled: true,
                    hidden: hidden
                }
            ]
        });
    }
})
;