Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationDetailForm',
    loadProfileConfigurationChannelAction: null,
    loadProfileConfigurationId: null,
    deviceConfigurationId: null,
    deviceTypeId: null,
    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],
    content: [
        {
            ui: 'large',
            title: 'Channel configuration',
            xtype: 'form',
            width: '100%',
            itemId: 'loadProfileConfigurationDetailChannelFormId',
            defaults: {
                labelWidth: 150,
                validateOnChange: false,
                validateOnBlur: false,
                anchor: '50%'
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    name: 'errors',
                    hidden: true,
                    margin: '0 0 32 0'
                },
                {
                    xtype: 'displayfield',
                    required: true,
                    fieldLabel: 'Measurement type',
                    name: 'measurementtype',
                    value: 'measurementType',
                    hidden: true
                },
                {
                    xtype: 'combobox',
                    required: true,
                    allowBlank: false,
                    fieldLabel: 'Measurement type',
                    emptyText: 'Select a measurement type',
                    name: 'measurementType',
                    displayField: 'name',
                    forceSelection: true,
                    valueField: 'id',
                    queryMode: 'local'
                },
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: 'CIM reading type',
                    emptyText: 'Select a measurement type first',
                    name: 'readingType'
                },
                {
                    xtype: 'obis-displayfield',
                    name: 'obiscode',
                    value: 'Select a measurement type first'
                },
                {
                    xtype: 'obis-field',
                    fieldLabel: 'Overruled OBIS code',
                    name: 'overruledObisCode'
                },
                {
                    xtype: 'combobox',
                    required: true,
                    allowBlank: false,
                    fieldLabel: 'Unit of measure ',
                    emptyText: 'Select a unit of measure',
                    name: 'unitOfMeasure',
                    forceSelection: true,
                    displayField: 'name',
                    valueField: 'id',
                    queryMode: 'local'
                },
                {
                    xtype: 'textfield',
                    required: true,
                    allowBlank: false,
                    fieldLabel: 'Overflow value',
                    name: 'overflowValue',
                    msgTarget: 'under',
                    maxLength: 80,
                    vtype: 'overflowValue'
                },
                {
                    xtype: 'textfield',
                    required: true,
                    allowBlank: false,
                    fieldLabel: 'Multiplier',
                    name: 'multiplier',
                    msgTarget: 'under',
                    vtype: 'multiplier',
                    value: 1,
                    maxLength: 80,
                    afterSubTpl: '<div class="x-form-display-field"><i>' + 'Multiplies the collected value. The multiplied value will be stored in the channel' + '</i></div>'
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
                            xtype: 'container',
                            itemId: 'LoadProfileChannelAction'
                        },
                        {
                            xtype: 'container',
                            itemId: 'LoadProfileChannelCancel'
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        this.down('#LoadProfileChannelAction').add(
            {
                xtype: 'button',
                name: 'loadprofilechannelaction',
                text: this.loadProfileConfigurationChannelAction,
                action: this.loadProfileConfigurationChannelAction,
                ui: 'action'
            }
        );
        this.down('#LoadProfileChannelCancel').add(
            {
                xtype: 'button',
                text: 'Cancel',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles/' + this.loadProfileConfigurationId + '/channels',
                ui: 'link'
            }
        );
        Ext.apply(Ext.form.VTypes, {
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
            overflowValueText: 'Overflow value is wrong',
            multiplierText: 'Multiplier is wrong'
        });
    }
});

