Ext.define('Mdc.view.setup.devicechannels.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreviewForm',
    itemId: 'deviceLoadProfileChannelsPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Mdc.view.setup.devicechannels.ValidationOverview'
    ],
    device: null,
    router: null,
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                    labelAlign: 'top',
                    layout: 'vbox',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            xtype: 'reading-type-displayfield',
                            name: 'readingType',
                            itemId: 'readingType',
                            showTimeAttribute: false
                        },
                        {
                            xtype: 'reading-type-displayfield',
                            name: 'calculatedReadingType',
                            itemId: 'calculatedReadingType',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.calculatedReadingType', 'MDC', 'Calculated reading type'),
                            showTimeAttribute: false,
                            hidden: true
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                            name: 'interval_formatted'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.lastReading', 'MDC', 'Last reading'),
                            name: 'lastReading',
                            renderer: function (value) {
                                var tooltip = Uni.I18n.translate('deviceloadprofiles.tooltip.lastreading', 'MDC', 'The moment when the data was read out for the last time.');
                                return value
                                    ? Uni.DateTime.formatDateTimeLong(value) + '<span style="margin: 0 0 0 10px; width: 16px; height: 16px" class="uni-icon-info-small" data-qtip="' + tooltip + '"></span>'
                                    : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('device.channels.timestampLastValue', 'MDC', 'Timestamp last value'),
                            name: 'lastValueTimestamp_formatted'
                        },
                        {
                            xtype: 'obis-displayfield',
                            name: 'obisCode'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.overflowValue', 'MDC', 'Overflow value'),
                            name: 'overflowValue'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm.nbrOfFractionDigits', 'MDC', 'Number of fraction digits'),
                            name: 'nbrOfFractionDigits'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.loadProfile', 'MDC', 'Load profile'),
                            name: 'loadProfileId',
                            renderer: function (value) {
                                var res = '',
                                    device;
                                if (value instanceof Mdc.model.LoadProfileOfDevice) {
                                    var url = me.router.getRoute('devices/device/loadprofiles/loadprofiledata').buildUrl({mRID: encodeURIComponent(me.mRID), loadProfileId: value.get('id')});
                                    res = '<a href="' + url + '">' +Ext.String.htmlEncode(value.get('name')) + '</a>';
                                } else if (Ext.isNumber(value)) {
                                    var loadProfile = Mdc.model.LoadProfileOfDevice;
                                    loadProfile.getProxy().setUrl(me.device.get('mRID'));
                                    loadProfile.load(value, {
                                        success: function (record) {
                                            me.down('[name=loadProfileId]').setValue(record)
                                        }
                                    })
                                }
                                return res
                            }
                        }
                    ]
                },
                {
                    xtype: 'deviceloadprofilechannelsoverview-validation',
                    router: me.router
                }
            ]
        };

        me.callParent(arguments);
    }


});
