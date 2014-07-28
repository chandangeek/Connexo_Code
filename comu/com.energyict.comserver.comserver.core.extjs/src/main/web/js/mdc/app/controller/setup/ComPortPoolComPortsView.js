Ext.define('Mdc.controller.setup.ComPortPoolComPortsView', {
    extend: 'Mdc.controller.setup.ComServerComPortsView',

    models: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComServerComPort',
        'Mdc.model.ComPort'
    ],

    views: [
        'Mdc.view.setup.comportpollcomports.View',
        'Mdc.view.setup.comportpollcomports.addComPortView',
        'Mdc.view.setup.comportpollcomports.addComPortGrid'
    ],
    stores: [
        'Mdc.store.ComPortPoolComports'
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
            selector: '#addComportToComportPoolGrid'
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
            '#addComportToComportPoolGrid': {
                selectionchange: this.getCountOfComPorts
            },
            'comPortPoolsComPortsView button[action=addComPort]': {
                click: this.addComPort
            },
            'comPortPoolComPortsActionMenu': {
                click: this.chooseAction
            },
            '#addComportToComportPoolView radiogroup[name=AllOrSelectedCommunicationPorts]': {
                change: this.selectRadioButton
            },
            '#uncheckAllComPorts': {
                click: this.unCheckAllComPorts
            },
            '#addComportToComportPoolView  button[name=addcomportstocomportpool]': {
                click: this.addComPorts
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
        me.checkLoadingOfNecessaryStores(function () {
            widget = Ext.widget('comPortPoolsComPortsView');
            me.getApplication().fireEvent('changecontentevent', widget);
            comPortPoolModel.load(id, {
                success: function (record) {
                    widget.down('comportpoolsubmenu').setServer(record);
                    me.getApplication().fireEvent('comPortPoolOverviewLoad', record);
                }
            });
        }, storesArr);
    },


    addComPort: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/comportpools/detail/comports/add').forward();
    },

    showAddComPortView: function (id) {
        var me = this,
            widget = Ext.widget('addComportToComportPoolView'),
            comPortPoolModel = me.getModel('Mdc.model.ComPortPool'),
            directionParams = {},
            recordData,
            existedRecordsArray,
            jsonValues;
        me.getApplication().fireEvent('changecontentevent', widget);
        comPortPoolModel.load(id, {
            success: function (record) {
                widget.down('comportpoolsubmenu').setServer(record);
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
                            me.checkAllComPorts();
                            me.comPortsStoreToAdd.sortByType(record.getData().type);
                            me.comPortsStoreToAdd.sortByExisted(existedRecordsArray);
                        }
                    });
            }
        });
    },

    checkAllComPorts: function () {
        var grid = this.getAddComPortGrid(),
            view = this.getAddComPortView(),
            uncheckComPorts = this.getUncheckComPortsButton(),
            selectionModel = grid.getView().getSelectionModel();
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(view)) {
            selectionModel.selectAll();
            grid.disable();
            uncheckComPorts.disable();
        }
    },

    unCheckAllComPorts: function () {
        var grid = this.getAddComPortGrid();
        grid.getView().getSelectionModel().deselectAll();
    },

    selectRadioButton: function (radiogroup) {
        var radioValue = radiogroup.getValue().comPortsRange;
        switch (radioValue) {
            case 'ALL':
                this.checkAllComPorts();
                break;
            case 'SELECTED':
                this.checkSelectedComPorts();
                break;
        }
    },

    checkSelectedComPorts: function () {
        var grid = this.getAddComPortGrid(),
            view = this.getAddComPortView(),
            uncheckComPorts = this.getUncheckComPortsButton();
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(view)) {
            grid.enable();
            uncheckComPorts.enable();
        }
    },

    getCountOfComPorts: function () {
        var grid = this.getAddComPortGrid(),
            comPortsCountSelected = grid.getView().getSelectionModel().getSelection().length,
            comPortsCountContainer = this.getComPortsCountContainer(),
            addButton = this.getAddComPortView().down('button[name=addcomportstocomportpool]'),
            comPortsMsgWord;
        if (comPortsCountSelected > 0) {
            comPortsMsgWord = comPortsCountSelected + ' ' + Uni.I18n.translate('comPortPoolComPorts.addPorts.count', 'MDC', 'communication port(s) selected');
            addButton.enable();
        } else {
            comPortsMsgWord = Uni.I18n.translate('comPortPoolComPorts.addPorts.noPortsSelected', 'MDC', 'No communication ports selected');
            addButton.disable();
        }

        var widget = Ext.widget('container', {
            html: comPortsMsgWord
        });

        comPortsCountContainer.removeAll(true);
        comPortsCountContainer.add(widget);
    },

    addComPorts: function () {
        var me = this,
            grid = me.getAddComPortGrid(),
            view = me.getAddComPortView(),
            selectedArray = grid.getView().getSelectionModel().getSelection(),
            router = me.getController('Uni.controller.history.Router'),
            comPortPoolModel = me.getModel('Mdc.model.ComPortPool'),
            poolId = router.routeparams['id'],
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: view
            }),
            messageText;
        preloader.show();
        comPortPoolModel.load(poolId, {
            success: function (record) {
                switch (record.getData().direction) {
                    case 'Inbound':
                        Ext.Array.each(selectedArray, function (selectedrecord) {
                            record.inboundComPorts().add(selectedrecord);
                        });
                        break;
                    case 'Outbound':
                        Ext.Array.each(selectedArray, function (selectedrecord) {
                            record.outboundComPorts().add(selectedrecord);
                        });
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
                })
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
            callback: function(){
                page.setLoading(false);
            }

        });
    }
});