/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfilesPreviewForm',
    itemId: 'deviceLoadProfilesPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceloadprofiles.ValidationPreview'
    ],

    deviceId: null,
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'loadProfile',
                        renderer: function (value) {
                            var res = '';
                            if (value && value.id && value.name) {
                                var url = me.router.getRoute('devices/device/loadprofiles/loadprofiledata').buildUrl({deviceId: encodeURIComponent(me.deviceId), loadProfileId: value.id});
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
                        fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                        name: 'interval_formatted'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                        name: 'dataUntil',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                var date = new Date(value);
                                return Uni.DateTime.formatDateLong(date) + ' - ' + Uni.DateTime.formatTimeShort(date);
                            }
                            return '-';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.nextReadingBlockStart', 'MDC', 'Next reading block start'),
                        name: 'lastReading',
                        renderer: function (value) {
                            if (value) {
                                var date = new Date(value);
                                return Uni.DateTime.formatDateLong(date) + ' - ' + Uni.DateTime.formatTimeShort(date);
                            }
                            return '-';
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
                        itemId: 'channels-list-container',
                        layout: 'fit'
                    }
                ]
            },
            {
                xtype: 'deviceloadprofiles-validation',
                router: me.router
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            channelsListContainer = me.down('#channels-list-container'),
            channels = record.get('channels');

        me.callParent(arguments);
        Ext.suspendLayouts();
        channelsListContainer.removeAll();
        Ext.Array.each(channels, function (channel) {
            channelsListContainer.add({
                xtype: 'reading-type-displayfield',
                fieldLabel: undefined,
                value: channel.readingType,
                link: me.router.getRoute('devices/device/channels/channeldata').buildUrl({deviceId: encodeURIComponent(me.deviceId), channelId: channel.id})
            });
        });
        if (Ext.isEmpty(channels)) {
            channelsListContainer.add({
                xtype: 'displayfield',
                fieldLabel: undefined
            });
        }
        Ext.resumeLayouts(true);
    }
});
