Ext.define('Mdc.usagepointmanagement.view.ChannelDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.channel-data-grid',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],
    channel: null,

    initComponent: function () {
        var me = this,
            readingType = me.channel.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval',
                renderer: function (interval) {
                    return interval.end
                        ? Uni.I18n.translate(
                        'general.dateAtTime', 'MDC', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(new Date(interval.end)), Uni.DateTime.formatTimeShort(new Date(interval.end))])
                        : '';
                },
                flex: 1
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.value', 'MDC', 'Value ({0})', [unit])
                    : Uni.I18n.translate('general.value.empty', 'MDC', 'Value'),
                flex: 1,
                dataIndex: 'value'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('usagePointChannelData.pagingtoolbartop.displayMsg', 'MDC', '{2} readings'),
                emptyMsg: Uni.I18n.translate('usagePointChannelData.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display')
            }
        ];

        me.callParent(arguments);
    }
});