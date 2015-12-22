Ext.define('Imt.channeldata.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.channelsPreviewForm',
    itemId: 'channelsPreviewForm',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Imt.channeldata.view.ValidationOverview',
        'Imt.customattributesonvaluesobjects.view.AttributeSetsPlaceholderForm',
        'Imt.channeldata.view.ActionMenu'
    ],
    usagepoint: null,
    router: null,
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
                            labelAlign: 'top',
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 200
                            },
                            items: [
                                {
                                    xtype: 'reading-type-displayfield',
                                    name: 'readingType',
                                    itemId: 'readingType',
                                    showTimeAttribute: false
                                },
                                {
                                    xtype: 'reading-type-displayfield',
                                    name: 'calculatedReadingType',
                                    itemId: 'calculatedReadingType',
                                    fieldLabel: Uni.I18n.translate('channels.calculatedReadingType', 'IMT', 'Calculated reading type'),
                                    showTimeAttribute: false,
                                    hidden: true
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('channels.interval', 'IMT', 'Interval'),
                                    name: 'interval',
                                    renderer: function (value) {
                                        var res = '';
                                        value ? res = Ext.String.htmlEncode(Ext.String.format('{0} {1}', value.count, value.timeUnit)) : null;
                                        return res
                                    }
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('channels.timestampLastValue', 'IMT', 'Timestamp last value'),
                                    name: 'lastValueTimestamp',
                                    renderer: function (value) {
                                        var tooltip = Uni.I18n.translate('channels.tooltip.lastvaluetime', 'IMT', 'The timestamp from the latest reading.');
                                        return value
                                            ? Uni.DateTime.formatDateTimeLong(value) + '<span style="margin: 0 0 0 10px; width: 16px; height: 16px" class="uni-icon-info-small" data-qtip="' + tooltip + '"></span>'
                                            : '';
                                    }
                                }                                
                            ]
                        },
                        {
                            xtype: 'channelsValidationOverview',
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
