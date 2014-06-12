Ext.define('Mdc.controller.setup.DeviceConfigurationLogbooks', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookConfigurations'
    ],

    views: [
        'setup.deviceconfiguration.DeviceConfigurationLogbooks',
        'setup.deviceconfiguration.EditLogbookConfiguration',
        'setup.deviceconfiguration.ActionMenu'
    ],

    refs: [
        {
            ref: 'deviceConfigurationLogbooks',
            selector: 'device-configuration-logbooks'
        }
    ],

    init: function () {
        this.control({
            'device-configuration-logbooks grid': {
                select: this.loadGridItemDetail
            },
            'device-configuration-logbooks grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'device-logbook-action-menu': {
                click: this.chooseAction
            }

        });
        this.store = this.getStore('Mdc.store.LogbookConfigurations');
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('device-configuration-logbooks panel[name=details]')[0],
            itemForm = Ext.ComponentQuery.query('device-configuration-logbooks form[name=logbookConfigurationDetails]')[0],
            overruledField = itemForm.down('[name=overruledObisCode]'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.view.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        this.logbookConfigId = record.internalId;
        itemForm.loadRecord(record);
        if (record.data.overruledObisCode == record.data.obisCode) {
            overruledField.setValue('-');
        }
        itemPanel.setTitle(record.get('name'));
        itemPanel.show();
        preloader.destroy();
    },

    chooseAction: function (menu, item) {
        var action = item.action,
            logbooksView = this.getDeviceConfigurationLogbooks();
        switch (action) {
            case 'edit':
                window.location.href = '#/administration/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations/' + this.logbookConfigId + '/edit';
                break;
            case 'delete':
                this.deleteLogbookType();
                break;
        }
    },

    deleteLogbookType: function () {
        var self = this,
            logbooksView = self.getDeviceConfigurationLogbooks(),
            grid = logbooksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected(),
            url = '/api/dtc/devicetypes/' + logbooksView.deviceTypeId + '/deviceconfigurations/' + logbooksView.deviceConfigurationId + '/logbookconfigurations/' + record.data.id;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: 'The logbook configuration will no longer be available on this device type.',
            title: 'Remove logbook configuration ' + record.data.name + ' ?',
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
})
;
