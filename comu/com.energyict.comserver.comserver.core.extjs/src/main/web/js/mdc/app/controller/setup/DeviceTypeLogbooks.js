Ext.define('Mdc.controller.setup.DeviceTypeLogbooks', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookTypes'
    ],

    views: [
        'setup.devicetype.DeviceTypeLogbooks',
        'setup.devicetype.ActionMenu',
        'Isu.view.ext.button.GridAction'
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
            }
        });
        me.store = me.getStore('Mdc.store.LogbookTypes');
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('device-type-logbooks panel[name=details]')[0],
            itemForm = Ext.ComponentQuery.query('device-type-logbooks form[name=logbookTypeDetails]')[0],
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.view.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        itemForm.loadRecord(record);
        itemPanel.setTitle(record.get('name'));
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
            title: 'Remove logbook type ' + record.data.name + ' ?',
            config: {
            },
            fn: function (state) {
                if (state === 'confirm') {
                    Ext.Ajax.request({
                        url: url,
                        method: 'DELETE',
                        success: function () {
                            self.getApplication().fireEvent('acknowledge', 'Successfully removed');
                            grid.getStore().load({
                                    callback: function () {
                                        var gridView = grid.getView(),
                                            selectionModel = gridView.getSelectionModel();
                                        logbooksView.down('pagingtoolbartop').totalCount = 0;
                                        if (grid.getStore().getCount() > 0) {
                                            grid.getStore().load({
                                                callback: function () {
                                                    selectionModel.select(0);
                                                    grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
                                                }
                                            });
                                        }
                                    }
                                }
                            );
                        },
                        failure: function (response) {
                            if (response.status == 400) {
                                var result = Ext.decode(response.responseText, true),
                                    title = Ext.String.format(Uni.I18n.translate('logbooktype.remove.failed', 'MDC', 'Failed to remove {0}'), record.data.name),
                                    message = Uni.I18n.translate('general.server.error', 'MDC', 'Server error');
                                if(!Ext.isEmpty(response.statusText)) {
                                    message = response.statusText;
                                }
                                if (result && result.message) {
                                    message = result.message;
                                } else if (result && result.error) {
                                    message = result.error;
                                }
                                self.getApplication().getController('Uni.controller.Error').showError(title, message);
                            }
                        }
                    });
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    }
});


