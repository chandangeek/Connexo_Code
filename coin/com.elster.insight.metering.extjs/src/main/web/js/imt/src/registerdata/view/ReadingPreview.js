Ext.define('Imt.registerdata.view.ReadingPreview', {
    extend: 'Imt.registerdata.view.GeneralPreview',
    alias: 'widget.readingPreview',
    itemId: 'readingPreview',
    record: null,

    requires: [
        'Imt.registerdata.view.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay',
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'readingPreviewForm',
                layout: 'form',
                items: [
                    {
                        xtype:'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('registerdata.general', 'IMT', 'General'),
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
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.recordedTime', 'IMT', 'Recorded timestamp'),
                                //name: 'recordedTime'
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'recordedTime',
                                        renderer: function (value) {
                                            if (!Ext.isEmpty(value)) {
                                                return Uni.DateTime.formatDateLong(new Date(value))
                                                    + ' ' + Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase() + ' '
                                                    + Uni.DateTime.formatTimeLong(new Date(value));
                                            }
                                            return '-';
                                        }
                                    },
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.lastReading', 'IMT', 'Last reading'),
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'utcTimestamp',
                                        renderer: function (value) {
                                            if (!Ext.isEmpty(value)) {
                                                return Uni.DateTime.formatDateLong(new Date(value))
                                                    + ' ' + Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase() + ' '
                                                    + Uni.DateTime.formatTimeLong(new Date(value));
                                            }
                                            return '-';
                                        }
                                    },
                                ]

                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.lastValue', 'IMT', 'Last value'),
                                name: 'readingValue'
                            },
                        ]
                    },
                ]
            }
        ];

        me.callParent(arguments);
    }
});


