/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.PurposeDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.purpose-data-grid',
    itemId: 'purpose-data-grid',
    store: 'Imt.purpose.store.PurposeSummaryData',
    requires: [
    ],
    plugins: [
        {
            ptype: 'bufferedrenderer',
            trailingBufferZone: 12,
            leadingBufferZone: 24
        }
    ],
    viewConfig: {
        enableTextSelection: true
    },
    outputs: null,
    height: 322,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('purpose.summary.endOfInterval', 'IMT', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('purpose.summary.dateAtTime', 'IMT', '{0} at {1}',[Uni.DateTime.formatDateShort(value),Uni.DateTime.formatTimeShort(value)])
                        : '';
                },
                flex: 1
            }
        ];

        me.outputs.each(function (channel) {
            var channelHeader = Ext.String.format('{0} ({1})', channel.get('name'), channel.get('readingType').names.unitOfMeasure);

            // value column
            me.columns.push({
                header: channelHeader,
                tooltip: channelHeader,
                dataIndex: 'channelData',
                align: 'right',
                flex: 1,
                renderer: function (data) {
                    return !Ext.isEmpty(data[channel.get('id')]) ? Uni.Number.formatNumber(data[channel.get('id')], -1) : '-';
                }
            });
        });

        me.callParent(arguments);
    }
});