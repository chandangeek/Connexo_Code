Ext.define('Dlc.devicelifecycles.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycles-grid',
    store: 'Dlc.devicelifecycles.store.DeviceLifeCycles',
    router: null,

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dlc.devicelifecycles.view.ActionMenu'
    ],


    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                dataIndex: 'sorted_name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/devicelifecycles/devicelifecycle').buildUrl({deviceLifeCycleId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Dlc.privileges.DeviceLifeCycle.configure,
                menu: {
                    xtype: 'device-life-cycles-action-menu',
                    itemId: 'lifeCycleActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('deviceLifeCycles.pagingtoolbartop.displayMsg', 'DLC', '{0} - {1} of {2} device life cycles'),
                displayMoreMsg: Uni.I18n.translate('deviceLifeCycles.pagingtoolbartop.displayMoreMsg', 'DLC', '{0} - {1} of more than {2} device life cycles'),
                emptyMsg: Uni.I18n.translate('deviceLifeCycles.pagingtoolbartop.emptyMsg', 'DLC', 'There are no device life cycles to display'),
                dock: 'top',
                items: [
                    {
                        xtype: 'button',
                        privileges: Dlc.privileges.DeviceLifeCycle.configure,
                        itemId: 'add-device-life-cycle-button',
                        text: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Add device life cycle'),
                        href: me.router.getRoute('administration/devicelifecycles/add').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceLifeCycles.pagingtoolbarbottom.itemsPerPage', 'DLC', 'Device life cycles per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

