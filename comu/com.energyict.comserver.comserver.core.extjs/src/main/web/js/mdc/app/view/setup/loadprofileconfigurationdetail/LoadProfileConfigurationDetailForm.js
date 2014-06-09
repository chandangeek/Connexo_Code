Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationDetailForm',
    loadProfileConfigurationChannelHeader: null,
    loadProfileConfigurationChannelAction: null,
    content: [
        {
            items: [
                {
                    xtype: 'container',
                    itemId: 'LoadProfileChannelHeader'
                },
                {
                    xtype: 'form',
                    width: '50%',
                    itemId: 'loadProfileConfigurationDetailChannelFormId',
                    defaults: {
                        labelWidth: 150,
                        labelAlign: 'right',
                        margin: '0 0 20 0',
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            name: 'errors',
                            layout: 'hbox',
                            margin: '0 0 20 100',
                            hidden: true,
                            defaults: {
                                xtype: 'container'
                            }
                        },
                        {
                            xtype: 'combobox',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'Measurement type',
                            emptyText: 'Select a measurement type',
                            name: 'measurementType',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local'
                        },
                        {
                            xtype: 'displayfield',
                            labelSeparator: ' ',
                            fieldLabel: 'CIM reading type',
                            name: 'cimreadingtype',
                            value: 'Select a measurement type first'
                        },
                        {
                            xtype: 'displayfield',
                            labelSeparator: ' ',
                            fieldLabel: 'OBIS code',
                            name: 'obiscode',
                            value: 'Select a measurement type first'
                        },
                        {
                            xtype: 'textfield',
                            labelSeparator: ' ',
                            fieldLabel: 'Overruled OBIS code',
                            name: 'overruledObisCode',
                            maskRe: /[\d.]+/,
                            vtype: 'overruledObisCode',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'combobox',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'Unit of measure ',
                            emptyText: 'Select a unit of measure',
                            name: 'unitOfMeasure',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local'
                        },
                        {
                            xtype: 'textfield',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'Overflow value',
                            name: 'overflowValue',
                            msgTarget: 'under',
                            vtype: 'overflowValue'
                        },
                        {
                            xtype: 'textfield',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'Multiplier',
                            name: 'multiplier',
                            msgTarget: 'under',
                            vtype: 'multiplier',
                            value: 1,
                            afterSubTpl: '<div class="x-form-display-field"><i>' + 'Multiplies the collected value. The multiplied value will be stored in the channel' + '</i></div>'
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'bottom',
                            border: false,
                            margin: '0 0 0 100',
                            items: [
                                {
                                    xtype: 'container',
                                    itemId: 'LoadProfileChannelAction'
                                },
                                {
                                    xtype: 'button',
                                    text: 'Cancel',
                                    handler: function (button, event) {
                                        Ext.History.back();
                                    },
                                    ui: 'link'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        this.down('#LoadProfileChannelHeader').add(
            {
                xtype: 'container',
                html: '<h2>' + this.loadProfileConfigurationChannelHeader + '</h2>'
            }
        );
        this.down('#LoadProfileChannelAction').add(
            {
                xtype: 'button',
                name: 'loadprofilechannelaction',
                text: this.loadProfileConfigurationChannelAction,
                action: this.loadProfileConfigurationChannelAction,
                ui: 'action'
            }
        );
        Ext.apply(Ext.form.VTypes, {
            overruledObisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            overflowValue: function (val, field) {
                var over = /[\d]+/;
                if (over.test(val)) {
                    if (val > 0) {
                        return true
                    } else {
                        return false
                    }
                } else {
                    return false
                }
            },
            multiplier: function (val, field) {
                var mult = /[\d]+/;
                if (mult.test(val)) {
                    if (val > 0) {
                        return true
                    } else {
                        return false
                    }
                } else {
                    return false
                }
            },
            overruledObisCodeText: 'OBIS code is wrong',
            overflowValueText: 'Overflow value is wrong',
            multiplierText: 'Multiplier is wrong'
        });
    }
});

