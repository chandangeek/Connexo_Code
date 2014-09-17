Ext.define('Mdc.view.setup.deviceloadprofilechannels.numerical.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay',
        'Mdc.view.setup.deviceloadprofilechannels.ValidationPreview'
    ],
    title: '',
    frame: true,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD'
        }
    ],

    items: {
        xtype: 'form',
        defaults: {
            xtype: 'container',
            layout: 'form'
        },
        items: [
            {
                xtype:'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [

                ]
            },
            {
                xtype: 'deviceloadprofilechannelspreview-validation'
            }
        ]
    },

    initComponent: function () {
        var me = this;
        //me.items.items[0].removeAll();
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
            measurementType = me.channelRecord.get('unitOfMeasure_formatted'),
            accumulationBehavior;

        //Getting 4th magic number of a reading type to understand if it holds cumulative values or not
        if (readingType) {
            accumulationBehavior = readingType.split('.')[3];
        }

        // 1 means cumulative
        if (accumulationBehavior && accumulationBehavior == 1) {
            me.items.items[0].items.push({
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.delta', 'MDC', 'Delta'),
                name: 'value',
                renderer: function (value, metaData, record) {
                    return value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType : '';

                }
            }, {

                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.cumulativeValue', 'MDC', 'Cumulative value'),
                name: 'delta',
                renderer: function (value, metaData, record) {
                    return value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType: '';
                }
            });
        } else {
            me.items.items[0].items.push(
                {
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                    name: 'value',
                    renderer: function (value, metaData, record) {
                        return value ? Uni.I18n.formatNumber(value, 'MDC', 3) + ' ' + measurementType : '';
                    }
                });
        }

        me.items.items[0].items.push(
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
