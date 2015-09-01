Ext.define('Mdc.controller.setup.ComPortPoolComPortsView', {
    extend: 'Mdc.controller.setup.ComServerComPortsView',

    models: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComServerComPort',
        'Mdc.model.ComPort'
    ],

    views: [
        'Mdc.view.setup.comportpoolcomports.View',
        'Mdc.view.setup.comportpoolcomports.AddComPortView',
        'Mdc.view.setup.comportpoolcomports.AddComPortGrid'
    ],

    stores: [
        'Mdc.store.ComPortPoolComports'
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
            'comPortPoolsComPortsView comPortPoolComPortPreview [action=passwordVisibleTrigger]': {
                change: this.passwordVisibleTrigger
            },
            'comPortPoolsComPortsView button[action=addComPort]': {
                click: this.addComPort
            },
            'comPortPoolComPortsActionMenu': {
                click: this.chooseAction
            },
            'addComportToComportPoolView addComportToComportPoolGrid': {
                allitemsadd: this.onAllComPortsAdd,
                selecteditemsadd: this.onSelectedComPortsAdd
            }
        });

        this.comPortsStoreToAdd = this.getStore('ComPortPoolComports');
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
                    widget.down('comportpoolsidemenu #comportpoolLink').setText(record.get('name'));
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
            widget = Ext.widget('addComportToComportPoolView', {
                poolId: id
            }),
            comPortPoolModel = me.getModel('Mdc.model.ComPortPool'),
            directionParams = {},
            recordData,
            existedRecordsArray,
            jsonValues;

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.updateCancelHref(id);
        widget.setLoading(true);
        comPortPoolModel.load(id, {
            success: function (record) {
                widget.down('comportpoolsidemenu #comportpoolLink').setText(record.get('name'));
                widget.setLoading(false);
                recordData = record.getData();
                switch (recordData.direction) {
                    case 'Inbound':
                        existedRecordsArray = recordData.inboundComPorts;
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
                        }
                    });
            }
        });
    },

    onAllComPortsAdd: function () {
        var me = this,
            allComPorts = me.getAddComPortGrid().store.data.items;

        me.addComPorts(allComPorts);
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
            gridToolbarTop = grid.down('pagingtoolbartop');

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            success: function (model, operation) {
                gridToolbarTop.totalCount = 0;
                grid.getStore().loadPage(1);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServerComPorts.deleteSuccess.msg', 'MDC', 'Communication port removed'));
            },
            failure: function (model, operation) {
                var title = Uni.I18n.translate('comPortPoolComPorts.remove.failure', 'MDC', 'Failed to remove') + " " + record.get('name'),
                    errorsArray = Ext.JSON.decode(operation.response.responseText).errors,
                    message = '';

                Ext.Array.each(errorsArray, function (obj) {
                    message += obj.msg + '.</br>'
                });

                me.getApplication().getController('Uni.controller.Error').showError(title, message);
            },
            callback: function () {
                page.setLoading(false);
            }

        });
    }
});