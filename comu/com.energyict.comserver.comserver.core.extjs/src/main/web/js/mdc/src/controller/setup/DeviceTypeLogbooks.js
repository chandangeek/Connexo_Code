Ext.define('Mdc.controller.setup.DeviceTypeLogbooks', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookTypes'
    ],

    views: [
        'setup.devicetype.DeviceTypeLogbooks',
        'setup.devicetype.ActionMenu'
    ],

    refs: [
        {
            ref: 'deviceTypeLogbooks',
            selector: 'device-type-logbooks'
        }
    ],

    init: function () {
        var me = this;
        me.control({
            'device-type-logbooks grid': {
                select: me.loadGridItemDetail
            },
            'device-type-logbooks grid uni-actioncolumn': {
                menuclick : me.deleteLogbookType
            },
            'device-type-logbook-action-menu menuitem[action=deleteLogBookType]': {
                click: me.deleteLogbookType
            }
        });
        me.store = me.getStore('Mdc.store.LogbookTypesOfDeviceType');
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('device-type-logbooks panel[name=details]')[0],
            itemForm = Ext.ComponentQuery.query('device-type-logbooks form[name=logbookTypeDetails]')[0],
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.view.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        itemForm.loadRecord(record);
        itemPanel.setTitle(Ext.String.htmlEncode(record.get('name')));
        itemPanel.show();
        preloader.destroy();
    },

    deleteLogbookType: function () {
        var self = this,
            logbooksView = self.getDeviceTypeLogbooks(),
            grid = logbooksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected(),
            url = '/api/dtc/devicetypes/' + logbooksView.deviceTypeId + '/logbooktypes/' + record.data.id;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: 'The logbook type will no longer be available on this device type.',
            title: Uni.I18n.translate('deviceTypeLogbook.removeLogbook','MDC',"Remove logbook type '{0}'?", [record.data.name]),
            config: {
            },
            fn: function (state) {
                if (state === 'confirm') {
                    Ext.Ajax.request({
                        url: url,
                        method: 'DELETE',
                        success: function () {
                            self.getApplication().fireEvent('acknowledge', 'Logbook type removed');
                            grid.getStore().load({
                                    callback: function () {
                                        var gridView = grid.getView(),
                                            selectionModel = gridView.getSelectionModel();
                                        logbooksView.down('pagingtoolbarbottom').resetPaging();
                                        logbooksView.down('pagingtoolbartop').resetPaging();

                                  //     if (grid.getStore().getCount() > 0) {
                                            grid.getStore().load({
                                                callback: function () {
                                                    selectionModel.select(0);
                                                    grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
                                                }
                                            });
                                    //    }
                                    }
                                }
                            );
                        }
                    });
                }
            }
        });
    }
});


