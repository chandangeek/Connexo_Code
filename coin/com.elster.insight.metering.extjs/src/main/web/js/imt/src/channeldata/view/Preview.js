Ext.define('Imt.channeldata.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.channel-preview',
    itemId: 'channel-preview',
    record: null,

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
                        fieldLabel: Uni.I18n.translate('channeldata.general', 'IMT', 'General'),
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                            	fieldLabel: Uni.I18n.translate('general.deviceName', 'IMT', 'Device name'),
                                name: 'deviceName'
                            },
                            {
                            	fieldLabel: Uni.I18n.translate('general.readingTypeAlias', 'IMT', 'Reading type alias'),
                                name: 'readingTypeAlias'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.readingTypemRID', 'IMT', 'Reading type mRID'),
                                name: 'readingTypemRID'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.interval', 'IMT', 'Interval'),
                                name: 'interval',
                                renderer: function (value) {
                                    var res = '';
                                    value ? res = Ext.String.htmlEncode('{count} {timeUnit}'.replace('{count}', value.count).replace('{timeUnit}', value.timeUnit)) : null;
                                    return res
                                }
                            },
                        ]
                    },
                ]
            }
        ];

        me.callParent(arguments);
    }
});


