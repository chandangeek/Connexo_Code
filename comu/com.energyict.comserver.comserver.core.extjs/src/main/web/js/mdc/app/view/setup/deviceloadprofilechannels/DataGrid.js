Ext.define('Mdc.view.setup.deviceloadprofilechannels.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelDataGrid',
    itemId: 'deviceLoadProfileChannelDataGrid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.deviceloadprofilechannels.DataActionMenu'
    ],
    height: 395,
    plugins: {
        ptype: 'bufferedrenderer'
    },

    channelRecord: null,

    initComponent: function () {
        var me = this,
            readingType = me.channelRecord.get('cimReadingType'),
            accumulationBehavior;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                width: 200
            }
        ];

        //Getting 4th magic number of a reading type to understand if it holds cumulative values or not
        if (readingType) {
            accumulationBehavior = readingType.split('.')[3];
        }

        // 3 means cumulative
        if (accumulationBehavior && accumulationBehavior == 3) {
            me.columns.push({
                header: Uni.I18n.translate('deviceloadprofiles.channels.cumulativeValue', 'MDC', 'Cumulative value'),
                dataIndex: 'cumulativeValue',
                flex: 1
            }, {
                header: Uni.I18n.translate('deviceloadprofiles.channels.delta', 'MDC', 'Delta'),
                dataIndex: 'delta',
                flex: 1
            });
        } else {
            me.columns.push({
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                dataIndex: 'value',
                flex: 1
            });
        }

        me.columns.push({
                header: Uni.I18n.translate('deviceloadprofiles.channels.intervalFlags', 'MDC', 'Interval flags'),
                dataIndex: 'intervalFlags',
                        renderer: function (data) {
                            var result = '',
                                tooltip = '';
//                                icon = this.nextSibling('button');
                            if (Ext.isArray(data) && data.length) {
                                result = data.length;
                                Ext.Array.each(data, function (value, index) {
                                    index++;
                                    tooltip += Uni.I18n.translate('deviceloadprofiles.flag', 'MDC', 'Flag') + ' ' + index + ': ' + value + '<br>';

                                });
//                                icon.setTooltip(tooltip);
//                                icon.show();
                            }
//                            else {
//                                icon.hide();
//                            }
                            return result;
                        },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'deviceLoadProfileChannelDataActionMenu'
                }
            });

        me.callParent(arguments);
    }

});