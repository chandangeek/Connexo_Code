Ext.define('Imt.purpose.view.RegisterDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.register-data-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'Imt.purpose.store.RegisterReadings',
    output: null,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                flex: 1,
                dataIndex: 'timeStamp',
                renderer: function (value) {
                    return !value
                        ? Uni.DateTime.formatDateTimeShort(new Date(value))
                        : '-'
                }
            },
            {
                header: Uni.I18n.translate('general.value', 'IMT', 'Value') + ' (' + me.output.get('readingType').unit + ')',
                dataIndex: 'value',
                flex: 1
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('outputs.registers.pagingtoolbartop.displayMsgItems', 'UNI', '{0} - {1} of {2} items'),
                displayMoreMsg: Uni.I18n.translate('outputs.registers.displayMsgMoreItems', 'UNI', '{0} - {1} of more than {2} items'),
                emptyMsg: Uni.I18n.translate('outputs.registers.noItemsToDisplay', 'UNI', 'There are no items to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('outputs.registers.pagingtoolbarbottom.itemsPerPage', 'UNI', 'Readings per page')
            }

        ];
        me.callParent(arguments);
    }
});