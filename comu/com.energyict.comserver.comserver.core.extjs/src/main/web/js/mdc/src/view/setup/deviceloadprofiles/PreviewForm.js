Ext.define('Mdc.view.setup.deviceloadprofiles.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfilesPreviewForm',
    itemId: 'deviceLoadProfilesPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceloadprofiles.ValidationPreview'
    ],

    mRID: null,
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
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
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'MDC', 'Name'),
                        name: 'loadProfile',
                        renderer: function (value) {
                            var res = '';
                            if (value && value.id && value.name) {
                                var url = me.router.getRoute('devices/device/loadprofiles/loadprofiledata').buildUrl({mRID: encodeURIComponent(me.mRID), loadProfileId: value.id});
                                res = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>'
                            }
                            return res;
                        }
                    },
                    {
                        xtype: 'obis-displayfield',
                        name: 'obisCode'
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
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels'),
                        name: 'channels',
                        renderer: function (value, field) {
                            var result = '',
                                form = field.up('form'),
                                id,
                                url;
                            if (value) {
                                id = form.getRecord().getId();
                                Ext.isArray(value) && Ext.Array.each(value, function (channel) {
                                    url = me.router.getRoute('devices/device/channels/channeldata').buildUrl({mRID: encodeURIComponent(me.mRID), channelId: channel.id});
                                    result += '<a href="' + url + '"> ' + Ext.String.htmlEncode(channel.name) + '</a><br>';
                                });
                            }
                            return result;
                        }
                    }
                ]
            },
            {
                xtype: 'deviceloadprofiles-validation',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});
