Ext.define('Mdc.view.setup.devicechannels.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay',
        'Mdc.view.setup.devicechannels.ValidationPreview',
        'Uni.form.field.EditedDisplay'
    ],
    title: '&nbsp',
    frame: true,

    /* Commented because of JP-5861
     tools: [
     {
     xtype: 'button',
     text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
     iconCls: 'x-uni-action-iconD'
     }
     ],
     */

    items: {
        xtype: 'form',
        defaults: {
            xtype: 'container',
            layout: 'form'
        },
        items: [
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: []
            },
            { xtype: 'deviceloadprofilechannelspreview-validation' }
        ]
    },

    initComponent: function () {
        var me = this;

        me.items.items[0].items = [];
        me.items.items[0].items.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                name: 'interval_formatted'
            }
        );
        me.items.items[0].items.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                name: 'readingTime_formatted'
            }
        );
        var readingType = me.channelRecord.get('cimReadingType'),
            measurementType = me.channelRecord.get('unitOfMeasure'),
            accumulationBehavior;

        //Getting 4th magic number of a reading type to understand if it holds cumulative values or not
        if (readingType) {
            accumulationBehavior = readingType.split('.')[3];
        }

        // 1 means cumulative
        if (accumulationBehavior && accumulationBehavior == 1) {
            me.items.items[0].items.push(
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.delta', 'MDC', 'Delta'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'displayfield',
                            name: 'value',
                            renderer: function (v) {
                                if (!Ext.isEmpty(v)) {
                                    var value = Uni.Number.formatNumber(v, -1);
                                    return !Ext.isEmpty(value) ? Ext.String.htmlEncode(value) + ' ' + measurementType : '';
                                }
                                return '';
                            }
                        },
                        {
                            xtype: 'edited-displayfield',
                            name: 'modificationState',
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'displayfield',
                    name: 'collectedValue',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value'),
                    hidden: true,
                    renderer: function (v) {
                        if (!Ext.isEmpty(v)) {
                            var value = Uni.Number.formatNumber(v, -1);
                            return !Ext.isEmpty(value) ? Ext.String.htmlEncode(value) + ' ' + measurementType : '';
                        }
                        return '';
                    }
                },
                {

                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.cumulativeValue', 'MDC', 'Cumulative value'),
                    name: 'delta',
                    renderer: function (value, metaData, record) {
                        return !Ext.isEmpty(value) ? Ext.String.htmlEncode(value) + ' ' + measurementType : '';
                    }
                }
            );
        } else {
            me.items.items[0].items.push(
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'displayfield',
                            name: 'value',
                            renderer: function (v) {
                                if (!Ext.isEmpty(v)) {
                                    var value = Uni.Number.formatNumber(v, -1);
                                    return !Ext.isEmpty(value) ? value + ' ' + measurementType : '';
                                }
                                return '';
                            }
                        },
                        {
                            xtype: 'edited-displayfield',
                            name: 'modificationState',
                            margin: '0 0 0 10'
                        }
                    ]
                },
                {
                    xtype: 'displayfield',
                    name: 'collectedValue',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value'),
                    hidden: true,
                    renderer: function (v) {
                        if (!Ext.isEmpty(v)) {
                            var value = Uni.Number.formatNumber(v, -1);
                            return !Ext.isEmpty(value) ? Ext.String.htmlEncode(value) + ' ' + measurementType : Uni.I18n.translate('general.missing', 'MDC', 'Missing');
                        }
                        return '';
                    }
                }
            );
        }

        me.items.items[0].items.push(
            {
                xtype: 'interval-flags-displayfield',
                name: 'intervalFlags'
            }
        );

        me.callParent(arguments);
    }
});