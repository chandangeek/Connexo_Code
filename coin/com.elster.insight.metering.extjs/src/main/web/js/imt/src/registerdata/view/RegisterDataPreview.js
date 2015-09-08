Ext.define('Imt.registerdata.view.RegisterDataPreview', {
    extend: 'Imt.registerdata.view.GeneralPreview',
    alias: 'widget.registerDataPreview',
    itemId: 'registerDataPreview',
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
                itemId: 'registerDataPreviewForm',
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
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.lastReading', 'IMT', 'Last reading time'),
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'readingTime',
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
                                name: 'value'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.lastValue', 'IMT', 'Delta value'),
                                name: 'deltaValue'
                            },
                        ]
                    },
                ]
            }
        ];

        me.callParent(arguments);
    }
});


