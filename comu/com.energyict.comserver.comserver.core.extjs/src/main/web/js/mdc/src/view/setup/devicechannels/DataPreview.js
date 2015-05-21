Ext.define('Mdc.view.setup.devicechannels.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay',
        'Mdc.view.setup.devicechannels.ValidationPreview',
        'Uni.form.field.EditedDisplay'
    ],
    //   title: '&nbsp',
    frame: false,

    /* Commented because of JP-5861
     tools: [
     {
     xtype: 'button',
     text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
     iconCls: 'x-uni-action-iconD'
     }
     ],
     */

    updateForm: function (record) {
        var me = this,
            intervalEnd = record.get('interval_end'),
            bulkValueField = me.down('displayfield[name=collectedValue]'),
            title = Uni.DateTime.formatDateLong(intervalEnd)
                + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                + Uni.DateTime.formatTimeLong(intervalEnd);
        Ext.suspendLayouts();
        me.down('#general-panel').setTitle(title);
        me.down('#values-panel').setTitle(title);
        bulkValueField.setVisible(record.get('isBulk'));
        me.down('form').loadRecord(record);
        Ext.resumeLayouts(true);
    },

    initComponent: function () {
        var me = this,
            generalItems = [],
            valuesItems = [],
            measurementType = me.channelRecord.get('unitOfMeasure');

        generalItems.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                name: 'interval_formatted'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                name: 'readingTime_formatted'
            },
            {
                xtype: 'interval-flags-displayfield',
                name: 'intervalFlags'
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationstatus.title', 'MDC', 'Validation status'),
                name: 'validationStatus',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('devicechannelsreadings.validationstatus.active', 'MDC', 'Active') :
                        Uni.I18n.translate('devicechannelsreadings.validationstatus.inactive', 'MDC', 'Inactive')
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.dataValidated.title', 'MDC', 'Data validated'),
                name: 'dataValidated',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('devicechannelsreadings.dataValidated.yes', 'MDC', 'Yse') :
                        Uni.I18n.translate('devicechannelsreadings.dataValidated.no', 'MDC', 'No')
                }
            },
            {
                fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                name: 'readingQualities',
                renderer: function (value) {
                    return value
                }
            }
        );


        valuesItems.push(
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                labelAlign: 'top',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                layout: 'vbox',
                items: [
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'value',
                                renderer: function (v) {
                                    if (!Ext.isEmpty(v)) {
                                        var value = Uni.Number.formatNumber(v, -1);
                                        return !Ext.isEmpty(value) ? value + ' ' + measurementType : '';
                                    }
                                    return '';
                                }
                            },
                            {
                                xtype: 'edited-displayfield',
                                name: 'deltaModificationState',
                                margin: '0 0 0 10'
                            }
                        ]
                    },
                    {
                        fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationResult.title', 'MDC', 'Validation result'),
                        name: 'deltaValidationInformation',
                        renderer: function (value) {
                            var res = '';
                            if (value.validationResult) {
                                switch (value.validationResult.split('.')[1]) {
                                    case 'notValidated':
                                        res = Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated');
                                        break;
                                    case 'suspect':
                                        res = Uni.I18n.translate('devicechannelsreadings.validationResult.suspect', 'MDC', 'Suspect');
                                        break;
                                    case 'ok':
                                        res = Uni.I18n.translate('devicechannelsreadings.validationResult.notsuspect', 'MDC', 'Not suspect');
                                        break;
                                }
                            } else {
                                res = Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated');
                            }
                            return res;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                        name: 'deltaValidationInfo',
                        renderer: function (value) {
                            return value
                        }
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value'),
                labelAlign: 'top',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                layout: 'vbox',
                items: [
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'collectedValue',
                                renderer: function (v) {
                                    if (!Ext.isEmpty(v)) {
                                        var value = Uni.Number.formatNumber(v, -1);
                                        return !Ext.isEmpty(value) ? value + ' ' + measurementType : '';
                                    }
                                    return '';
                                }
                            },
                            {
                                xtype: 'edited-displayfield',
                                name: 'bulkModificationState',
                                margin: '0 0 0 10'
                            }
                        ]
                    },
                    {
                        fieldLabel: Uni.I18n.translate('devicechannelsreadings.validationResult.title', 'MDC', 'Validation result'),
                        name: 'bulkValidationInformation',
                        renderer: function (value) {
                            var res = '';
                            if (value.validationResult) {
                                switch (value.validationResult.split('.')[1]) {
                                    case 'notValidated':
                                        res = Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated');
                                        break;
                                    case 'suspect':
                                        res = Uni.I18n.translate('devicechannelsreadings.validationResult.suspect', 'MDC', 'Suspect');
                                        break;
                                    case 'ok':
                                        res = Uni.I18n.translate('devicechannelsreadings.validationResult.notsuspect', 'MDC', 'Not suspect');
                                        break;
                                }
                            } else {
                                res = Uni.I18n.translate('devicechannelsreadings.validationResult.notvalidated', 'MDC', 'Not validated');
                            }
                            return res;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('devicechannelsreadings.readingqualities.title', 'MDC', 'Reading qualities'),
                        name: 'deltaValidationInfo',
                        renderer: function (value) {
                            return value
                        }
                    }
                ]
            }
        );

        me.items = {
            xtype: 'form',
            items: {
                xtype: 'tabpanel',
                items: [
                    {
                        title: Uni.I18n.translate('devicechannelsdata.generaltab.title', 'MDC', 'General'),
                        items: {
                            itemId: 'general-panel',
                            frame: true,
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 200
                            },
                            items: generalItems
                        }
                    },
                    {
                        title: Uni.I18n.translate('devicechannelsdata.readingvaluetab.title', 'MDC', 'Reading values'),
                        items: {
                            itemId: 'values-panel',
                            frame: true,
                            items: valuesItems,
                            title: 'sdfsd'
                        }
                    }
                ]
            }
        };
        me.callParent(arguments);
    }
});