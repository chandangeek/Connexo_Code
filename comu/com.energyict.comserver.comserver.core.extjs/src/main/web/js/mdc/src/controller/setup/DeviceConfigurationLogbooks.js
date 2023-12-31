/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
            'device-logbook-action-menu': {
                click: this.chooseAction
            }

        });
        this.store = this.getStore('Mdc.store.LogbookConfigurations');
    },

    loadGridItemDetail: function (grid, record) {
        var itemPanel = Ext.ComponentQuery.query('device-configuration-logbooks panel[name=details]')[0],
            itemForm = Ext.ComponentQuery.query('device-configuration-logbooks form[name=logbookConfigurationDetails]')[0],
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.view.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        this.logbookConfigId = record.internalId;
        itemForm.loadRecord(record);
        itemPanel.setTitle(Ext.String.htmlEncode(record.get('name')));
        itemPanel.show();
        preloader.destroy();
    },

    chooseAction: function (menu, item) {
        var action = item.action,
            logbooksView = this.getDeviceConfigurationLogbooks();
        switch (action) {
            case 'edit':
                window.location.href = '#/administration/devicetypes/' + encodeURIComponent(logbooksView.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(logbooksView.deviceConfigurationId) + '/logbookconfigurations/' + encodeURIComponent(this.logbookConfigId) + '/edit';
                break;
            case 'delete':
                this.deleteLogbookType();
                break;
        }
    },

    deleteLogbookType: function () {
        var me = this,
            logbooksView = me.getDeviceConfigurationLogbooks(),
            grid = logbooksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: 'The logbook configuration will no longer be available on this device configuration.',
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
            config: {
            },
            fn: function (state) {
                if (state === 'confirm') {
                    record.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', 'Logbook configuration removed');
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
