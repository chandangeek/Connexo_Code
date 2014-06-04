Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConfigurationsGrid',
    overflowY: 'auto',
    itemId: 'deviceconfigurationsgrid',
    deviceTypeId: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceConfigurations'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    store: 'DeviceConfigurations',
    /*listeners: {
     'render': function(component) {
     // Get sure that the store is not loading and that it
     // has at least a record on it
     if (this.store.isLoading() || this.store.getCount() == 0) {
     // If it is still pending attach a listener to load
     // event for a single time to handle the selection
     // after the store has been loaded
     this.store.on('load', function() {
     this.getView().getSelectionModel().select(0);
     this.getView().focusRow(0);
     }, this, {
     single: true
     });
     } else {
     this.getView().getSelectionModel().select(0);
     this.getView().focusRow(0);
     }

     }
     },  */


    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + record.get('id') + '">' + value + '</a>';
                },
                flex: 0.4
            },
            {
                header: Uni.I18n.translate('deviceconfiguration.status', 'MDC', 'Status'),
                dataIndex: 'active',
                renderer: function (value, b, record) {
                    return value === true ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                },
                flex: 0.4
            },

            {
                xtype: 'uni-actioncolumn',
                items: [
                    {
                        itemId: 'actionStatusMenuItem',
                        text: "test",
                        action: 'activateItem'
                    },
                    {
                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                        action: 'editItem'
                    },
                    {
                        text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                        action: 'deleteItem'
                    }

                ]
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceconfigurarion.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} device configurations'),
                displayMoreMsg: Uni.I18n.translate('deviceconfigurarion.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} device configurations'),
                emptyMsg: Uni.I18n.translate('deviceconfigurarion.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device configurations to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: Uni.I18n.translate('deviceconfiguration.createDeviceConfiguration', 'MDC', 'Add device configuration'),
                        itemId: 'createDeviceConfiguration',
                        xtype: 'button',
                        action: 'createDeviceConfiguration'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                //todo: check if this works
                params: [
                    {deviceType: this.deviceTypeId}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device configurations per page'),
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});

