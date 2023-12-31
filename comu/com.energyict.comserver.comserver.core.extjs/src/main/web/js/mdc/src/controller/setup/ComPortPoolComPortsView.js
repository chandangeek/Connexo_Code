/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.ComPortPoolComPortsView', {
    extend: 'Mdc.controller.setup.ComServerComPortsView',

    models: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComServerComPort',
        'Mdc.model.ComPort',
        'Mdc.model.ComPortPoolComPort'
    ],

    views: [
        'Mdc.view.setup.comportpoolcomports.View',
        'Mdc.view.setup.comportpoolcomports.AddComPortView',
        'Mdc.view.setup.comportpoolcomports.AddComPortGrid'
    ],

    stores: [
        'Mdc.store.ComPortPoolComports',
        'Mdc.store.ComPortPools'
    ],

    required: [
        'Uni.util.common'
    ],

    refs: [
        {
            ref: 'comPortPoolsComPortsView',
            selector: 'comPortPoolsComPortsView'
        },
        {
            ref: 'preview',
            selector: 'comPortPoolsComPortsView comPortPoolComPortPreview'
        },
        {
            ref: 'comPortsGrid',
            selector: 'comPortPoolsComPortsView comPortPoolComPortsGrid'
        },
        {
            ref: 'addComPortView',
            selector: '#addComportToComportPoolView'
        },
        {
            ref: 'addComPortGrid',
            selector: 'addComportToComportPoolView addComportToComportPoolGrid'
        },
        {
            ref: 'uncheckComPortsButton',
            selector: '#uncheckAllComPorts'
        },
        {
            ref: 'comPortsCountContainer',
            selector: '#comPortsCountContainer'
        }

    ],

    init: function () {
        this.control({
            'comPortPoolsComPortsView comPortPoolComPortsGrid': {
                select: this.showPreviewWithServerName
            },
            'comPortPoolsComPortsView comPortPoolComPortsGrid actioncolumn': {
                removeCommPort: this.removeCommunicationPort
            },
            'comPortPoolsComPortsView comPortPoolComPortPreview [action=passwordVisibleTrigger]': {
                change: this.passwordVisibleTrigger
            },
            'comPortPoolsComPortsView button[action=addComPort]': {
                click: this.addComPort
            },
            'comPortPoolComPortsActionMenu': {
                click: this.chooseAction
            },
            'addComportToComportPoolView': {
                selecteditemsadd: this.onSelectedComPortsAdd
            }
        });

        this.comPortsStoreToAdd = this.getStore('ComPortPoolComports');
    },

    removeCommunicationPort: function (record) {
        this.showDeleteConfirmation(record);
    },

    showPreviewWithServerName: function (selectionModel, record) {
        this.showPreview(selectionModel, record, true);
    },

    showView: function (id) {
        var me = this,
            comPortPoolModel = me.getModel('Mdc.model.ComPortPool'),
            comPortModel = me.getModel('Mdc.model.ComServerComPort'),
            comPortsStore = me.getStore('Mdc.store.ComServerComPorts'),
            url = comPortPoolModel.getProxy().url + '/' + id + '/comports',
            storesArr = [
                'Mdc.store.ComPortPools',
                'Mdc.store.ComServers',
                'Mdc.store.BaudRates',
                'Mdc.store.NrOfDataBits',
                'Mdc.store.NrOfStopBits',
                'Mdc.store.Parities',
                'Mdc.store.FlowControls'
            ],
            widget;
        comPortModel.getProxy().url = url;
        comPortsStore.getProxy().url = url;
        widget = Ext.widget('comPortPoolsComPortsView', {
            poolId: id
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Uni.util.Common.loadNecessaryStores(storesArr, function () {
            comPortPoolModel.load(id, {
                success: function (record) {
                    widget.down('comportpoolsidemenu').setHeader(record.get('name'));
                    widget.comPortPool = record;
                    me.getApplication().fireEvent('comPortPoolOverviewLoad', record);
                    widget.setLoading(false);
                }
            });
        });
    },

    addComPort: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/comportpools/detail/comports/add').forward();
    },

    showAddComPortView: function (id) {
        var me = this,
            comPortPoolModel = me.getModel('Mdc.model.ComPortPool'),
            directionParams = {},
            recordData,
            existedRecordsArray,
            jsonValues,
            portPoolsStore = Ext.getStore('Mdc.store.ComPortPools');

        portPoolsStore.load(function () {

            var widget = Ext.widget('addComportToComportPoolView', {
                poolId: id,
                comportPoolStore: portPoolsStore
            });

            me.getApplication().fireEvent('changecontentevent', widget);

            comPortPoolModel.load(id, {
                success: function (record) {
                    widget.down('comportpoolsidemenu #comportpoolLink').setText(record.get('name'));
                    recordData = record.getData();
                    switch (recordData.direction) {
                        case 'Inbound':
                            existedRecordsArray = recordData.inboundComPorts;
                            widget.showPoolColumn();
                            break;
                        case 'Outbound':
                            existedRecordsArray = recordData.outboundComPorts;
                            break;
                    }
                    directionParams['property'] = 'direction';
                    directionParams['value'] = me.lowerFirstLetter(recordData.direction);
                    jsonValues = Ext.JSON.encode(directionParams);
                    me.comPortsStoreToAdd.load(
                        {
                            params: {filter: '[' + jsonValues + ']'},
                            callback: function () {
                                me.comPortsStoreToAdd.sortByType(record.get('comPortType'));
                                me.comPortsStoreToAdd.sortByExisted(existedRecordsArray);
                                if (me.comPortsStoreToAdd.getCount() === 0) {
                                    widget.noItemsAvailable();
                                } else {
                                    widget.down('grid').reconfigure(me.comPortsStoreToAdd);
                                }
                                widget.setLoading(false);
                            }
                        });
                },
                failure: function (record) {
                    widget.setLoading(false);
                }
            });

        });
    },

    onSelectedComPortsAdd: function (selection) {
        this.addComPorts(selection);
    },

    addComPorts: function (selection) {
        var me = this,
            view = me.getAddComPortView(),
            router = me.getController('Uni.controller.history.Router'),
            comPortPoolModel = me.getModel('Mdc.model.ComPortPool'),
            poolId = router.arguments['id'],
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: view
            }),
            messageText;

        preloader.show();
        comPortPoolModel.load(poolId, {
            success: function (record) {
                switch (record.getData().direction) {
                    case 'Inbound':
                        record.inboundComPorts().add(selection);
                        break;
                    case 'Outbound':
                        record.outboundComPorts().add(selection);
                        break;
                }

                record.save({
                    callback: function (records, operation, success) {
                        if (success) {
                            messageText = Uni.I18n.translate('comPortPoolComPorts.addPorts.successMessage', 'MDC', 'Communication port(s) added');
                            me.getApplication().fireEvent('acknowledge', messageText);
                            router.getRoute('administration/comportpools/detail/comports').forward();
                        }
                        preloader.destroy();
                    }
                });
            }
        });
    },

    lowerFirstLetter: function (string) {
        return string.charAt(0).toLowerCase() + string.slice(1);
    },

    deleteComPort: function (record) {
        var me = this,
            page = me.getComPortPoolsComPortsView(),
            grid = me.getComPortsGrid(),
            gridToolbarTop = grid.down('pagingtoolbartop'),
            comPortPool = me.getComPortPoolsComPortsView().comPortPool;

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        Ext.Ajax.request({
            url: record.getProxy().url + '/' + record.getId(),
            method: 'DELETE',
            jsonData: _.pick(comPortPool.getData(), 'id', 'name', 'version', 'direction'),
            success: function () {
                gridToolbarTop.totalCount = 0;
                grid.getStore().loadPage(1);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServerComPorts.deleteSuccess.msg', 'MDC', 'Communication port removed'));
            },
            failure: function (response) {
                if (response.status === 409) {
                    return
                }
                var title = Uni.I18n.translate('comPortPoolComPorts.remove.failurex', 'MDC', "Failed to remove '{0}'", [record.get('name')]),
                    json = Ext.JSON.decode(response.responseText),
                    errorsArray = Ext.JSON.decode(response.responseText).errors,
                    message = '',
                    code = '';

                Ext.Array.each(errorsArray, function (obj) {
                    message += obj.msg + '.</br>';
                });

                if (json && json.errorCode) {
                    code = json.errorCode;
                }

                me.getApplication().getController('Uni.controller.Error').showError(title, message, code);
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});