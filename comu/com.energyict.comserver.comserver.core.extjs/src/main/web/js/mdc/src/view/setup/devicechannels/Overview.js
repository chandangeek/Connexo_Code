Ext.define('Mdc.view.setup.devicechannels.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelOverview',
    itemId: 'deviceLoadProfileChannelOverview',

    requires: [
        'Mdc.view.setup.devicechannels.ValidationOverview',
        'Mdc.view.setup.devicechannels.ActionMenu'
    ],

    router: null,
    device: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        ui: 'large',
                        flex: 1,
                        items: {
                            xtype: 'form',
                            margin: '0 0 0 100',
                            itemId: 'deviceLoadProfileChannelsOverviewForm',
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
                                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.unitOfMeasure', 'MDC', 'Unit of measure'),
                                            name: 'unitOfMeasure_formatted'
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
                                        }
                                    ]
                                },
                                {
                                    xtype: 'deviceloadprofilechannelsoverview-validation',
                                    router: me.router
                                }
                            ]
                        }
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        iconCls: 'x-uni-action-iconD',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'deviceLoadProfileChannelsActionMenu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});