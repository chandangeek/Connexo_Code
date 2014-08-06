Ext.define('Mdc.view.setup.deviceloadprofiles.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfilesPreviewForm',
    itemId: 'deviceLoadProfilesPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay'
    ],

    mRID: null,
    router: null,

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'MDC', 'Name'),
                name: 'name'
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
                            url =  me.router.getRoute('devices/device/loadprofiles/loadprofile/channels/channel').buildUrl({mRID: me.mRID, loadProfileId: id, channelId: channel});
                            result += '<a href="' + url + '"> ' + channel + '</a><br>';
                        });
                    }
                    return result;
                }
            }
        ];

        me.callParent(arguments);
    }
});
