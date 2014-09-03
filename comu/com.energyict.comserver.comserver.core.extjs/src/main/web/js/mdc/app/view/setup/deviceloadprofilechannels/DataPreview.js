Ext.define('Mdc.view.setup.deviceloadprofilechannels.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay'
    ],
    layout: 'fit',
    frame: true,

    channelRecord: null,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceLoadProfileChannelDataActionMenu'
            }
        }
    ],

    initComponent: function () {
        var me = this,
            readingType = me.channelRecord.get('cimReadingType'),
            measurementType = me.channelRecord.get('unitOfMeasure_formatted'),
            accumulationBehavior;

        me.items = {
            xtype: 'form',
            itemId: 'deviceLoadProfileChannelDataPreviewForm',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            }
        };


        me.items.items = [
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                name: 'interval_formatted'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                name: 'readingTime_formatted'
            }
        ];

        //Getting 4th magic number of a reading type to understand if it holds cumulative values or not
        if (readingType) {
            accumulationBehavior = readingType.split('.')[3];
        }

        // 1 means cumulative
        if (accumulationBehavior && accumulationBehavior == 1) {
            me.items.items.push({
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.cumulativeValue', 'MDC', 'Cumulative value'),
                name: 'value',
                renderer: function (value, metaData, record) {
                    return value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType : '';

                }
            }, {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.delta', 'MDC', 'Delta'),
                name: 'delta',
                renderer: function (value, metaData, record) {
                    return value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType: '';
                }
            });
        } else {
            me.items.items.push(
                {
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                    name: 'value',
                    renderer: function (value, metaData, record) {
                        return value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType : '';
                    }
                });
        }

        me.items.items.push(
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.multiplier', 'MDC', 'Multiplier'),
                name: 'multiplier'
            },
            {
                xtype: 'interval-flags-displayfield'
            });

        me.callParent(arguments);
    }
});
