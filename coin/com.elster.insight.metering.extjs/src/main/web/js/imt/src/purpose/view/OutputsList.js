Ext.define('Imt.purpose.view.OutputsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.outputs-list',
    requires: [
        'Imt.purpose.store.Outputs',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.ReadingType'
    ],
    store: 'Imt.purpose.store.Outputs',
    overflowY: 'auto',
    itemId: 'metrologyConfigurationList',

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                flex: 1,
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/metrologyconfiguration/' + record.get('id') + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('outputs.label.interval', 'IMT', 'Interval'),
                flex: 1,
                dataIndex: 'interval',
                renderer: function(value){
                    return value.timeUnit;
                }
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