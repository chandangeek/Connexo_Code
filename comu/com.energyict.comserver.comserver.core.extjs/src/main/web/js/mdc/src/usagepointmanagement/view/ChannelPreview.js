Ext.define('Mdc.usagepointmanagement.view.ChannelPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usage-point-channel-preview',
    requires: [
        'Uni.form.field.ReadingTypeDisplay'
    ],
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },

    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'reading-type-displayfield',
                itemId: 'readingType-field',
                fieldLabel: Uni.I18n.translate('general.readingType', 'MDC', 'Reading type'),
                name: 'readingType'
            },
            {
                itemId: 'interval-field',
                fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                name: 'interval',
                renderer: function (value) {
                    return Ext.isObject(value)
                        ? Uni.util.Common.translateTimeUnit(value.count, value.timeUnit)
                        : '-';
                }
            },
            {
                itemId: 'dataUntil-field',
                fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                name: 'dataUntil',
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateTimeShort(new Date(value))
                        : '-';
                }
            },
            {
                itemId: 'deviceChannels-field',
                fieldLabel: Uni.I18n.translate('general.deviceChannels', 'MDC', 'Device channels'),
                name: 'deviceChannels',
                renderer: function (value) {
                    var result = '',
                        canViewDevices = Mdc.privileges.Device.canView();

                    if (Ext.isArray(value)) {
                        Ext.Array.each(value, function (deviceChannel, index) {
                            var device = canViewDevices
                                    ? '<a href="'
                                + me.router.getRoute('devices/device').buildUrl({
                                    mRID: deviceChannel.mRID
                                })
                                + '">'
                                + deviceChannel.mRID
                                + '</a>'
                                    : deviceChannel.mRID,
                                channel = !deviceChannel.until
                                    ? '<a href="'
                                + me.router.getRoute('devices/device/channels').buildUrl({
                                    mRID: deviceChannel.mRID,
                                    channelId: deviceChannel.channel.id
                                })
                                + '">'
                                + deviceChannel.channel.name
                                + '</a>'
                                    : deviceChannel.channel.name
                                + '<span style="margin-left: 10px" class="icon-info" data-qtip="'
                                + Uni.I18n.translate('usagePointChannel.notAvailableChannel.qtip', 'MDC', 'This channel is not available on the device anymore because device configuration is changed')
                                + '"></span>',
                                period = !deviceChannel.until
                                    ? Uni.I18n.translate('general.fromX', 'MDC', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(deviceChannel.from))], false)
                                    : Uni.I18n.translate('general.fromXuntilX', 'MDC', 'from {0} until {1}', [
                                    Uni.DateTime.formatDateTimeShort(new Date(deviceChannel.from)),
                                    Uni.DateTime.formatDateTimeShort(new Date(deviceChannel.until))
                                ], false);

                            if (index) {
                                result += '<br><br>';
                            }

                            result += device + ' &#62; ' + channel;
                            result += '<br><span style="font-size: 90%">' + period + '</span>';
                        });
                    }

                    return result;
                }
            }
        ];

        me.callParent(arguments);
    }
});