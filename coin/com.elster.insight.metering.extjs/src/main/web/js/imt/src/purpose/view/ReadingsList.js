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
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('outputs.label.interval', 'IMT', 'Interval'),
                flex: 1,
                dataIndex: 'interval',
                renderer: function(interval){
                    return interval.end;
                }
            },
            {
                header: Uni.I18n.translate('readings.label.value', 'IMT', 'Value'),
                flex: 1,
                dataIndex: 'value'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('outputs.pagingtoolbartop.displayMsg', 'IMT', '{2} outputs'),
                emptyMsg: Uni.I18n.translate('outputs.pagingtoolbartop.emptyMsg', 'IMT', 'There are no outputs to display')
            }
        ];

        me.callParent(arguments);
    }
});