Ext.define('Mdc.view.setup.property.Edit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.propertyEdit',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    itemId: 'propertyEdit',
    autoShow: true,
    border: 0,
    autoWidth: true,
    requires: ['Ext.form.Panel',
        'Mdc.view.setup.property.CodeTableSelector',
        'Mdc.view.setup.property.CodeTable',
        'Mdc.view.setup.property.UserFileReference',
        'Mdc.widget.TimeInfoField'
    ],

    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                itemId: 'propertiesform',
                shrinkWrap: 1,
                padding: 10,
                border: 0,
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'fieldset',
                        title: 'Properties',
                        defaults: {
                            labelWidth: 200,
                            anchor: '100%'
                        },
                        collapsible: true,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },


                        itemId: 'fsproperties'

                    }
                ]
            }
        ];

        this.callParent(arguments);
    },
    addTextProperty: function (key, text) {
        var me = this;
        me.down('#fsproperties').add({
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
                    width: 350
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addPasswordProperty: function (key, text) {
        var me = this;
        me.down('#fsproperties').add({
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
                    width: 350
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addHexStringProperty: function (key, text) {
        var me = this;
        me.down('#fsproperties').add({
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
                    width: 350,
                    vtype: 'hexstring'
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addTextAreaProperty: function (key, text) {
        var me = this;
        me.down('#fsproperties').add({
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
                    width: 350,
                    grow: true,
                    anchor: '100%'
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addNumberProperty: function (key, value, minvalue, maxvalue, allowdecimals) {
        var me = this;
        me.down('#fsproperties').add({
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
                    allowDecimals: allowdecimals
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addBooleanProperty: function (key, value) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    width: 350,
                    cls: 'check'
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addNullableBooleanProperty: function (key, value1, value2, value3) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addDateProperty: function (key, value) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    margin: '0 5 0 0',
                    width: 100
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addTimeProperty: function (key, value) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    margin: '0 5 0 0',
                    width: 100
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addDateTimeProperty: function (key, dateValue, timeValue) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addTimeDurationProperty: function (key, value) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
            layout: 'hbox',
            defaults: {
                hideLabel: true
            },
            items: [
                /*    {
                 xtype: 'timeInfoField',
                 name: key,
                 fieldLabel: key
                 },*/
                {
                    xtype: 'textfield',
                    name: key,
                    fieldLabel: key,
                    itemId: key,
                    value: value,
                    size: 200,
                    margin: '0 5 0 0',
                    width: 350
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addCodeTableProperty: function (key, value) {
        var me = this;
        me.down('#fsproperties').add({
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
    addCodeTablePropertyWithSelectionWindow: function (key, value) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    action: 'showCodeTable',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addUserReferenceFilePropertyWithSelectionWindow: function (key, value) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    action: 'showUserFileReference',
                    margin: '0 5 0 0'
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addComboBoxTextProperty: function (key, store, selectedValue, exhaustive) {
        var me = this;
        console.log('store');
        console.log(store);
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addComboBoxNumberProperty: function (key, store, selectedValue, exhaustive) {
        var me = this;
        me.down('#fsproperties').add({
            xtype: 'fieldcontainer',
            fieldLabel: key,
            msgTarget: 'side',
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
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addEan18StringProperty: function (key, text) {
        var me = this;
        me.down('#fsproperties').add({
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
                    width: 350,
                    vtype: 'ean18'
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    },
    addEan13StringProperty: function (key, text) {
        var me = this;
        me.down('#fsproperties').add({
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
                    width: 350,
                    vtype: 'ean13'
                },
                {
                    xtype: 'button',
                    name: 'btn_delete_' + key,
                    itemId: 'btn_delete_' + key,
                    text: 'Restore defaults',
                    scale: 'small',
                    action: 'delete',
                    disabled: true
                }
            ]
        });
    }
});