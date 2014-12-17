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
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.readingType', 'MDC', 'Reading type'),
                            showTimeAttribute: false
                        },
                        {
                            name: 'interval',
                            fieldLabel: Uni.I18n.translate('devicechannels.interval', 'MDC', 'Interval'),
                            renderer: function (value) {
                                var res = '';
                                value ? res = '{count} {timeUnit}'.replace('{count}', value.count).replace('{timeUnit}', value.timeUnit) : null;
                                return res
                            }
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.lastReading', 'MDC', 'Last reading'),
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'lastReading_formatted',
                                    margin: '3 0 0 0',
                                    renderer: function (value) {
                                        this.nextSibling('button').setVisible(value ? true : false);
                                        return value;
                                    }
                                },
                                {
                                    xtype: 'button',
                                    tooltip: Uni.I18n.translate('deviceloadprofiles.tooltip.lastreading', 'MDC', 'The moment when the data was read out for the last time.'),
                                    iconCls: 'icon-info-small',
                                    ui: 'blank',
                                    itemId: 'lastReadingHelp',
                                    shadow: false,
                                    margin: '6 0 0 10',
                                    width: 16
                                }
                            ]
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
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.multiplier', 'MDC', 'Multiplier'),
                            name: 'multiplier'
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
                                    var url = me.router.getRoute('devices/device/loadprofiles/loadprofile/data').buildUrl({mRID: me.mRID, loadProfileId: value.get('id')});
                                    res = '<a href="' + url + '">' + value.get('name') + '</a>';
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
                        },
                        {
                            xtype: 'deviceloadprofilechannelsoverview-validation',
                            router: me.router
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }


});
