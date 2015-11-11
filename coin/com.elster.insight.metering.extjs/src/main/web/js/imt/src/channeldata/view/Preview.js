Ext.define('Imt.channeldata.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.channel-preview',
    itemId: 'channel-preview',
    record: null,
    frame: true,
    requires: [
        'Uni.form.field.ReadingTypeDisplay'
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'channelPreviewForm',
                layout: 'form',
                items: [
                    {
                        xtype:'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.label.general', 'IMT', 'General'),
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.label.readingType', 'IMT', 'Reading type'),
                                xtype: 'reading-type-displayfield',
                                name: 'readingType',
                                itemId: 'readingType',
                                showTimeAttribute: false
                            },

                            {
                                fieldLabel: Uni.I18n.translate('general.label.interval', 'IMT', 'Interval'),
                                name: 'interval',
                                renderer: function (value) {
                                    var res = '';
                                    value ? res = Ext.String.htmlEncode('{count} {timeUnit}'.replace('{count}', value.count).replace('{timeUnit}', value.timeUnit)) : null;
                                    return res
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.label.lastReading', 'IMT', 'Last reading'),
                                name: 'lastValueTimestamp',
                                renderer: function (value) {
                                    if (!Ext.isEmpty(value)) {
                                        return Uni.DateTime.formatDateLong(new Date(value))
                                            + ' ' + Uni.I18n.translate('general.label.at', 'IMT', 'At').toLowerCase() + ' '
                                            + Uni.DateTime.formatTimeLong(new Date(value));
                                    }
                                    return '-';
                                }

                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


