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
                labelWidth: 200,
                validateOnChange: false,
                validateOnBlur: false,
                width: 700
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    name: 'errors',
                    hidden: true,
                    margin: '0 0 32 0'
                },
                {
                    xtype: 'reading-type-displayfield',
                    name: 'readingType',
                    hidden: true
                },
                {
                    xtype: 'reading-type-combo',
                    required: true,
                    allowBlank: false,
                    submitValue: false
                },
                {
                    xtype: 'reading-type-displayfield',
                    name: 'calculatedReadingType',
                    fieldLabel: Uni.I18n.translate('channelConfig.channelConfigForm.calculatedReadingType', 'MDC', 'Calculated reading type'),
                    required: true,
                    submitValue: false,
                    hidden: true
                },
                {
                    xtype: 'obis-displayfield',
                    name: 'obiscode',
                    value: Uni.I18n.translate('channelConfig.channelConfigForm.obisCodeEmptyTxt', 'MDC', 'Select a reading type first')
                },
                {
                    xtype: 'obis-field',
                    required: false,
                    name: 'overruledObisCode',
                    fieldLabel: Uni.I18n.translate('channelConfig.channelConfigForm.overruledObisCodeLabel', 'MDC', 'Overruled OBIS code')
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
                    xtype: 'numberfield',
                    fieldLabel: Uni.I18n.translate('loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm.nbrOfFractionDigits', 'MDC', 'Number of fraction digits'),
                    name: 'nbrOfFractionDigits',
                    required: true,
                    value: 0,
                    minValue: 0,
                    width: 270
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
        Ext.suspendLayouts();
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
                text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles/' + this.loadProfileConfigurationId + '/channels',
                ui: 'link'
            }
        );
        Ext.resumeLayouts();
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
            overflowValueText: Uni.I18n.translate('channelConfig.channelConfigForm.overflowValueText', 'MDC', 'Overflow value is wrong')

        });
    }
});

