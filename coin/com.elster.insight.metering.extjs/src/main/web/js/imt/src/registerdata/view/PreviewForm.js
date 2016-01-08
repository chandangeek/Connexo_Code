Ext.define('Imt.registerdata.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.registerPreviewForm',
    itemId: 'registerPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Imt.registerdata.view.RegisterValidationPreview',
 //       'Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Imt.registerdata.view.ActionMenu'
    ],
    usagepoint: null,
    router: null,
    record: null,
    initComponent: function () {
        var me = this;
        me.items = {
            layout: 'column',
            defaults: {
                xtype: 'form',
                columnWidth: 0.5,
                minWidth: 450
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('usagepointchannelconfiguration.general', 'IMT', 'General'),
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
                                                            + ' ' + Uni.I18n.translate('general.label.at', 'IMT', 'at') + ' '
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
                            },
                            {
                                xtype: 'registerValidationPreview',
                                router: me.router
                            }
                    ]
                },
//                {
//                    xtype: 'custom-attribute-sets-placeholder-form',
//                    itemId: 'custom-attribute-sets-placeholder-form-id',
//                    actionMenuXtype: 'channelsActionMenu',
//                    attributeSetType: 'channel',
//                    router: me.router
//                }
            ]
        };

        me.callParent(arguments);
    }
});
