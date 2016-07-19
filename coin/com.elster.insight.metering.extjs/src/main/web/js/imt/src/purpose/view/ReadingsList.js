Ext.define('Imt.purpose.view.ReadingsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.readings-list',
    itemId: 'readings-list',
    requires: [
        'Imt.purpose.store.Readings',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Imt.purpose.store.Readings',
    overflowY: 'auto',

    initComponent: function () {
        var me = this,
            readingType = me.output.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval',
                renderer: function (interval) {
                    return  interval.end
                        ? Uni.I18n.translate(
                        'general.dateAtTime', 'MDC', '{0} at {1}',
                        [Uni.DateTime.formatDateShort(new Date(interval.end)), Uni.DateTime.formatTimeShort(new Date(interval.end))] )
                        : '';
                },
                flex: 1
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.value', 'MDM', 'Value ({0})', [unit])
                    : Uni.I18n.translate('general.value.empty', 'MDC', 'Value'),
                flex: 1,
                align: 'right',
                dataIndex: 'value'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('readings.pagingtoolbartop.displayMsg', 'IMT', '{2} readings'),
                emptyMsg: Uni.I18n.translate('readings.pagingtoolbartop.emptyMsg', 'IMT', 'There are no readings to display')
            }
        ];

        me.callParent(arguments);
    }
});