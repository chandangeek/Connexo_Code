Ext.define('Apr.view.appservers.ImportServicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.apr-import-services-grid',
    requires: [
        'Apr.view.appservers.ImportServiceActionMenu'
    ],
    width: '100%',
    maxHeight: 300,
    columns: [
        {
            header: Uni.I18n.translate('general.name', 'APR', 'Name'),
            dataIndex: 'importService',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'APR', 'Status'),
            dataIndex: 'status',
            flex: 0.8
        },
        /*{
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'apr-import-services-action-menu',
                itemId: 'apr-import-services-action-menu'
            }
        }*/


        {
            xtype: 'actioncolumn',
            align: 'right',
            items: [
                {
                    iconCls: 'uni-icon-delete',
                    itemId: 'apr-remove-import-service-btn',
                    tooltip: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),
                    handler: function (grid, rowIndex, colIndex, column, event, record) {
                        this.fireEvent('removeEvent', record);
                    }
                }
            ]
         }
    ]
});
