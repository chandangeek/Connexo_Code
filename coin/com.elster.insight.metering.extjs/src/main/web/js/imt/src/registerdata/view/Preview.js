Ext.define('Imt.registerdata.view.Preview', {
    extend: 'Imt.registerdata.view.GeneralPreview',
    alias: 'widget.registerPreview',
    itemId: 'registerPreview',
    record: null,

    requires: [
        'Imt.registerdata.view.ActionMenu',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'registerPreviewForm',
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
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.label.lastReading.timestamp', 'IMT', 'Last reading timestamp'),
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
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

                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.label.lastValue', 'IMT', 'Last value'),
                                name: 'lastReadingValue'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


