Ext.define('Imt.channeldata.view.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.channel-data-grid',
    itemId: 'channelDataGrid',
    store: 'Imt.channeldata.store.ChannelData',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],
    plugins: [
        'bufferedrenderer'
    ],
    viewConfig: {
        loadMask: false,
        enableTextSelection: true
    },
    selModel: {
        mode: 'MULTI'
    },
    channelRecord: null,
    router: null,

    initComponent: function () {
        var me = this,
            measurementType = me.channelRecord.get('unitOfMeasure');

        me.columns = [
            {
                header: Uni.I18n.translate('channeldata.label.endOfInterval', 'IMT', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value) {
                    if(!Ext.isEmpty(value)) {
                        return Uni.DateTime.formatDateTimeLong(new Date(value));
                    }
                    return '-';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('channeldata.label.channel.value', 'IMT', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                //align: 'right',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                noBottomPaging: true,
                displayMsg: '{2} reading(s)',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'save-changes-button',
                        text: Uni.I18n.translate('general.label.saveChanges', 'IMT', 'Save changes'),
                        hidden: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'undo-button',
                        text: Uni.I18n.translate('general.label.undo', 'IMT', 'Undo'),
                        hidden: true
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});