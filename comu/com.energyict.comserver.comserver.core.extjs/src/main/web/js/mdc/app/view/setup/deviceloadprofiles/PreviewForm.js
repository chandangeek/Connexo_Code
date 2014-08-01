Ext.define('Mdc.view.setup.deviceloadprofiles.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceLoadProfilesPreviewForm',
    itemId: 'deviceLoadProfilesPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay'
    ],

    mRID: null,

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
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
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'displayfield',
                    name: 'lastReading_formatted',
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
                    margin: '0 0 0 10',
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
                    mRID,
                    id;
                if (value) {
                    mRID = form.mRID;
                    id = form.getRecord().getId();
                    Ext.isArray(value) && Ext.Array.each(value, function (channel) {
                        result += '<a href="#/devices/' + mRID + '/loadprofiles/' + id + '/channels/' + channel + '"> ' + channel + '</a><br>'
                    });
                }
                return result;
            }
        }
    ]
});
