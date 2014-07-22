Ext.define('Mdc.controller.setup.ComServerComPortsEdit', {
    extend: 'Ext.app.Controller',
    models: [
        'Mdc.model.ComServer',
        'Mdc.model.ComPort',
        'Mdc.model.OutboundComPort',
        'Mdc.model.InboundComPort'
    ],
    views: [
        'Mdc.view.setup.comservercomports.Edit',
        'Mdc.view.setup.comservercomports.TCPForm',
        'Mdc.view.setup.comservercomports.UDPForm',
        'Mdc.view.setup.comservercomports.SerialForm',
        'Mdc.view.setup.comservercomports.ServletForm',
        'Mdc.view.setup.comservercomports.ComPortPoolsGrid',
        'Mdc.view.setup.comservercomports.AddComPortPool'
    ],
    stores: [
        'Mdc.store.ComPorts',
        'Mdc.store.ComPortTypes',
        'Mdc.store.ComPortPools',
        'Mdc.store.BaudRates',
        'Mdc.store.NrOfDataBits',
        'Mdc.store.NrOfStopBits',
        'Mdc.store.Parities',
        'Mdc.store.FlowControls',
        'Mdc.store.TimeUnits',
        'Mdc.store.AddComPortPools',
        'Mdc.store.OutboundComPortPools',
        'Mdc.store.InboundComPortPools',
        'Mdc.store.ComPorts'
    ],
    defaultType: 'TCP',
    refs: [
        {
            ref: 'comPortEdit',
            selector: '#comPortEdit'
        },
        {
            ref: 'addComPortForm',
            selector: '#addComPortForm'
        },
        {
            ref: 'addComPortPoolsGrid',
            selector: '#addComPortPoolsGrid'
        },
        {
            ref: 'addComPortPoolsView',
            selector: '#addComPortPoolToComPort'
        },
        {
            ref: 'comPortPoolsGridSelection',
            selector: '#comPortPoolsGridSelection'
        },
        {
            ref: 'comPortPoolsGrid',
            selector: 'outboundportcomportpools'
        },
        {
            ref: 'comPortPoolsCount',
            selector: '#comPortPoolsCount'
        },
        {
            ref: 'uncheckAllButton',
            selector: '#addComPortPoolToComPort #uncheckAllComPortPools'
        },
        {
            ref: 'comPortPoolsCountContainer',
            selector: '#comPortPoolsCountContainer'
        }
    ],


    init: function () {
        this.control({
            '#comPortTypeSelect': {
                change: this.changeType
            },
            '#addComPortForm button[action=cancel]': {
                click: this.cancelClick
            },
            '#comPortEdit #addEditButton': {
                click: this.addClicked
            },
            'button[action=showAddComPortPoolPage]': {
                click: this.navigateAddPool
            },
            'outboundportcomportpools actioncolumn': {
                click: this.removePool
            },
            '#addComPortPoolsGrid': {
                selectionchange: this.updateSelection
            },
            '#addComPortPoolToComPort #uncheckAllComPortPools': {
                click: this.uncheckAll
            },
            '#addComPortPoolToComPort #createEditButton': {
                click: this.addComportPool
            },
            '#addComPortPoolToComPort #cancelLink': {
                click: this.cancelAddPool
            },
            'outboundportcomportpools': {
                afterrender: this.updatePoolCount
            },
            '#addComPortPoolToComPort radiogroup[name=AllOrSelectedCommunicationPortPools]': {
                change: this.selectRadioButton
            }
        });

    },

    updatePoolCount: function (grid) {
        var store = grid.getStore(),
            countMsg = grid.down('#comPortPoolsCount'),
            count = store.getCount();
        count > 0 ? countMsg.update(count + ' ' +  Uni.I18n.translate('comServerComPorts.addPools.count', 'MDC', ' communication port pools')) :
            countMsg.update(Uni.I18n.translate('comServerComPorts.addPools.noPools', 'MDC', 'No communication port pools'));
    },

    removePool: function (grid, el, index) {
        grid.getStore().removeAt(index, 1);
        this.updatePoolCount(grid.up('panel'))
    },

    addComportPool: function () {
        var portpools = this.getAddComPortPoolsGrid().getSelectionModel().getSelection(),
            addStore = this.getStore('Mdc.store.AddComPortPools');
        addStore.loadData(portpools, true);
        this.cancelAddPool()
    },

    uncheckAll: function () {
        this.getAddComPortPoolsGrid().getSelectionModel().deselectAll()
    },

    updateSelection: function () {
        var grid = this.getAddComPortPoolsGrid(),
            comPortsCountSelected = grid.getView().getSelectionModel().getSelection().length,
            comPortsCountContainer = this.getComPortPoolsCountContainer(),
            comPortsMsgWord;
        comPortsCountSelected > 0 ?
            comPortsMsgWord = comPortsCountSelected + ' ' + Uni.I18n.translate('comServerComPorts.addPools.selectedCount', 'MDC', ' communication port pools selected') :
            comPortsMsgWord = Uni.I18n.translate('comServerComPorts.addPools.noSelectedCount', 'MDC', 'No communication port pools selected');
        var widget = Ext.widget('container', {
            html: comPortsMsgWord
        });
        comPortsCountContainer.removeAll(true);
        comPortsCountContainer.add(widget);
    },

    changeType: function (combo, newValue) {
        this.portType = newValue;
        this.saveState();
        this.comportEdit.showForm(this.portDirection, newValue);
        this.restoreState();
        this.filterStore();
    },

    formToModel: function (form, model) {
        var queryString = Ext.Object.toQueryString(form.getValues()),
            values = Ext.Object.fromQueryString(queryString, true);
        model.beginEdit();
        model.set(values);
        model.endEdit();
        return model;
    },

    selectRadioButton: function (radiogroup) {
        var radioValue = radiogroup.getValue().comPortPoolsRange;
        switch (radioValue) {
            case 'ALL':
                this.checkAllComPortPools();
                break;
            case 'SELECTED':
                this.checkSelectedComPortPools();
                break;
        }
    },


    checkSelectedComPortPools: function () {
        var grid = this.getAddComPortPoolsGrid(),
            view = this.getAddComPortPoolsView(),
            uncheckComPortPools = this.getUncheckAllButton();
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(view)) {
            grid.enable();
            uncheckComPortPools.enable();
        }
    },

    checkAllComPortPools: function () {
        var grid = this.getAddComPortPoolsGrid(),
            view = this.getAddComPortPoolsView(),
            uncheckComPortPools = this.getUncheckAllButton(),
            selectionModel;
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(view)) {
            selectionModel = grid.getView().getSelectionModel();
            selectionModel.selectAll();
            grid.disable();
            uncheckComPortPools.disable();
        }
    },

    cancelAddPool: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute(this.currentUrl).forward();
    },

    cancelClick: function () {
        var router = this.getController('Uni.controller.history.Router');
        delete this.portModel;
        router.getRoute('administration/comservers/detail/comports').forward();
    },

    addClicked: function (btn) {
        var me = this,
            form = btn.up('form'),
            queryString = Ext.Object.toQueryString(form.getValues()),
            values = Ext.Object.fromQueryString(queryString, true),
            formErrorsPanel = form.down('[name=form-errors]'),
            comPortPool = this.getStore('Mdc.store.AddComPortPools'),
            modemInit,
            typeModel,
            record,
            actionType,
            ids;

        if (form.isValid()) {
            formErrorsPanel.hide();
            switch (this.portDirection) {
                case 'inbound':
                    typeModel = Ext.create(Mdc.model.InboundComPort);
                    break;
                case 'outbound':
                    typeModel = Ext.create(Mdc.model.OutboundComPort);
                    ids = comPortPool.collect('id');
                    break;
            }
            switch (btn.action) {
                case 'addModel':
                    actionType = Uni.I18n.translate('general.added', 'MDC', 'added');
                    record = me.formToModel(form, typeModel);
                    record.set('active', false);
                    record.set('comServer_id', me.comServerId);
                    record.set('direction', me.portDirection);
                    record.set('type', me.portDirection + '_' + record.getData().comPortType);
                    record.getProxy().url = record.getProxy().url.replace('{comServerId}', me.comServerId);
                    break;
                case 'editModel':
                    actionType = Uni.I18n.translate('general.saved', 'MDC', 'saved');
                    record = me.formToModel(form, me.recordToEdit);
                    break;
            }
            if (values.modemInitStrings) {
                modemInit = me.parseModemStringToArray(values.modemInitStrings);
                record.set('modemInitStrings', modemInit);
            }

            ids && (record.set('outboundComPortPoolIds', ids));
            record.save({
                callback: function (records, operation, success) {
                    if (success) {
                        me.onSuccessSaving(me.portDirection, actionType);
                    } else {
                        me.onFailureSaving(operation.response, form);
                    }
                }
            })
        } else {
            formErrorsPanel.show()
        }
    },

    parseModemStringToArray: function (string) {
        var stringsArray = string.split(';'),
            returnedArray = [];
        Ext.Array.each(stringsArray, function(str) {
            returnedArray.push({'modemInitString': str});
        });

        return returnedArray;
    },

    parseModemArrayToString: function(array) {
        var string = '';
        Ext.Array.each(array, function(value){
            string += value.modemInitString + ';';
        });

        return string;
    },

    onSuccessSaving: function (portDirection, actionType) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (portDirection) {
            case 'inbound':
                messageText = Uni.I18n.translate('comServerComPorts.form.addInboundSuccess', 'MDC', 'Inbound communication port') + ' ' + actionType;
                break;
            case 'outbound':
                messageText = Uni.I18n.translate('comServerComPorts.form.addOutboundSuccess', 'MDC', 'Outbound communication port') + ' ' + actionType;
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        delete me.portModel;
        router.getRoute('administration/comservers/detail/comports').forward();
    },

    onFailureSaving: function (response, form) {
        var formErrorsPanel = form.down('uni-form-error-message'),
            basicForm = form.getForm(),
            responseText;

        if (response.status == 400) {
            responseText = Ext.decode(response.responseText, true);
            if (responseText && responseText.errors) {
                basicForm.markInvalid(responseText.errors);
                formErrorsPanel.show();
            }
        }
    },

    showEditView: function (id, direction, comPortId) {
        var me = this,
            widget = Ext.widget('comportEdit'),
            addComPortPoolsStore = me.getStore('Mdc.store.AddComPortPools'),
            outboundComPortPoolsStore = me.getStore('Mdc.store.OutboundComPortPools'),
            inboundStore = me.getStore('Mdc.store.InboundComPortPools'),
            comServerModel = me.getModel('Mdc.model.ComServer'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: widget
            }),
            comportTypeSelectCombo,
            actionButton,
            portModel,
            addForm,
            comPortPoolsGrid,
            recordData,
            directionField;

        me.getApplication().fireEvent('changecontentevent', widget);
        preloader.show();

        comServerModel.load(id, {
            success: function (record) {
                widget.down('comserversubmenu').setServer(record);
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            }
        });

        switch (direction) {
            case 'inbound':
                portModel = me.getModel('Mdc.model.InboundComPort');
                break;
            case 'outbound':
                portModel = me.getModel('Mdc.model.OutboundComPort');
                break;
        }


        me.currentUrl = 'administration/comservers/detail/comports/edit'
        me.comServerId = id;
        me.comPortId = parseInt(comPortId);
        me.comportEdit = widget;


        portModel.getProxy().url = portModel.getProxy().url.replace('{comServerId}', me.comServerId);
        portModel.load(me.comPortId, {
            success: function (record) {
                recordData = record.getData();
                me.recordToEdit = record;
                me.portType = recordData.comPortType;
                me.portDirection = recordData.direction;
                me.getApplication().fireEvent('loadComPortOnComServer', recordData.name);
                actionButton = Ext.ComponentQuery.query('#comPortEdit #addEditButton')[0];
                directionField = Ext.ComponentQuery.query('#comPortEdit displayfield[name=direction]')[0];
                actionButton.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                actionButton.action = 'editModel';
                directionField.show();

                me.getAddComPortForm().setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + recordData.name + "'");
                widget.showForm(me.portDirection, me.portType);
                addForm = widget.down('#addComPortForm');
                comportTypeSelectCombo = widget.down('#comPortTypeSelect');
                switch (recordData.direction) {
                    case 'inbound':
                        addForm.loadRecord(record);
                        inboundStore.load({
                            callback: function () {
                                me.filterStoreByType(inboundStore, me.portType);
                                preloader.destroy();
                            }
                        });
                        if (me.portType == 'SERIAL') {
                            var connectTimeoutCount = widget.down('#addFormNest').down('#connectTimeoutCount'),
                                connectTimeoutUnit = widget.down('#addFormNest').down('#connectTimeoutUnit'),
                                connectDelayCount = widget.down('#addFormNest').down('#connectDelayCount'),
                                connectDelayUnit = widget.down('#addFormNest').down('#connectDelayUnit'),
                                sendDelayCount = widget.down('#addFormNest').down('#sendDelayCount'),
                                sendDelayUnit = widget.down('#addFormNest').down('#sendDelayUnit'),
                                atCommandTimeoutCount = widget.down('#addFormNest').down('#atCommandTimeoutCount'),
                                atCommandTimeoutUnit = widget.down('#addFormNest').down('#atCommandTimeoutUnit'),
                                initModem = widget.down('#addFormNest').down('textfield[name=modemInitStrings]');
                            connectTimeoutCount.setValue(recordData.connectTimeout.count);
                            connectTimeoutUnit.setValue(recordData.connectTimeout.timeUnit);
                            connectDelayCount.setValue(recordData.delayAfterConnect.count);
                            connectDelayUnit.setValue(recordData.delayAfterConnect.timeUnit);
                            sendDelayCount.setValue(recordData.delayBeforeSend.count);
                            sendDelayUnit.setValue(recordData.delayBeforeSend.timeUnit);
                            atCommandTimeoutCount.setValue(recordData.atCommandTimeout.count);
                            atCommandTimeoutUnit.setValue(recordData.atCommandTimeout.timeUnit);
                            initModem.setValue(me.parseModemArrayToString(recordData.modemInitStrings));
                        }

                        break;
                    case 'outbound':
                        if (me.portModel) {
                            me.portModel.set('comPortType', me.portType);
                            me.restoreState();
                            preloader.destroy();
                        } else {
                            addForm.loadRecord(record);
                            addComPortPoolsStore.removeAll();
                            comPortPoolsGrid = me.getComPortPoolsGrid();
                            outboundComPortPoolsStore.load({
                                callback: function () {
                                    me.filterStoreByType(outboundComPortPoolsStore, me.portType);
                                    outboundComPortPoolsStore.each(function (value) {
                                        if (Ext.Array.contains(recordData.outboundComPortPoolIds, value.getData().id)) {
                                            addComPortPoolsStore.add(value);
                                        }
                                        comPortPoolsGrid.fireEvent('afterrender', comPortPoolsGrid);
                                        preloader.destroy();
                                    });
                                }
                            });
                        }
                        break;
                }
                comportTypeSelectCombo.disable();
            }
        });
    },

    showAddOutbound: function (id) {
        var me = this,
            widget = Ext.widget('comportEdit'),
            comServerModel = me.getModel('Mdc.model.ComServer');

        me.currentUrl = 'administration/comservers/detail/comports/addOutbound'
        me.comServerId = id;
        me.comportEdit = widget;
        me.portDirection = 'outbound';
        me.portType = me.defaultType;
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getAddComPortForm().setTitle(Uni.I18n.translate('comServerComPorts.addOutboundPort', 'MDC', 'Add outbound communication port'));
        me.getStore('Mdc.store.ComPortTypes').load({
            callback: function () {
                var me = this,
                    index = me.find('comPortType', 'SERVLET');
                index > 0 ? me.removeAt(index, 1) : null;
            }
        });
        comServerModel.load(id, {
            success: function (record) {
                widget.down('comserversubmenu').setServer(record);
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            }
        });
        widget.showForm('outbound', me.defaultType);
        this.restoreState();
    },

    showAddInbound: function (id) {
        var me = this,
            widget = Ext.widget('comportEdit'),
            comServerModel = me.getModel('Mdc.model.ComServer');

        me.currentUrl = 'administration/comservers/detail/comports/addInbound'
        me.portType = me.defaultType;
        me.comServerId = id;
        me.comportEdit = widget;
        me.portDirection = 'inbound';
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.showForm('inbound', me.defaultType);
        this.restoreState();
        me.getAddComPortForm().setTitle(Uni.I18n.translate('comServerComPorts.addInboundPort', 'MDC', 'Add inbound communication port'));
        me.getStore('Mdc.store.ComPortTypes').load();
        me.filterStore();

        comServerModel.load(id, {
            success: function (record) {
                widget.down('comserversubmenu').setServer(record);
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            }
        });
    },

    showAddComPortPool: function () {
        var me = this,
            widget = Ext.widget('addComPortPool');
        me.saveState();
        me.getApplication().fireEvent('changecontentevent', widget);
        me.filterStore();
        me.on('chooseAllComPortPools', function () {
            me.checkAllComPortPools();
            return true;
        }, {single: true});
    },

    filterStoreByType: function (store, type) {
        store.removeFilter('typeFilter', false);
        store.filter(
            {
                property: 'type',
                value: type,
                id: 'typeFilter'
            }
        )
    },

    filterStoreByIds: function (store, ids) {
        for (var a = 0; a < ids.length; a++) {
            var index = store.find('id', ids[a]);
            (index > -1) && store.removeAt(index);
        }
    },

    filterStore: function () {
        var me = this;
        switch (me.portDirection) {
            case 'inbound':
                var inboundStore = me.getStore('Mdc.store.InboundComPortPools');
                inboundStore.load({
                    callback: function() {
                        me.filterStoreByType(inboundStore, me.portType);
                        me.fireEvent('chooseAllComPortPools');
                    }
                });
                break;
            case 'outbound':
                var outboundStore = me.getStore('Mdc.store.OutboundComPortPools'),
                    addStore = me.getStore('Mdc.store.AddComPortPools'),
                    ids = addStore.collect('id');
                outboundStore.load({
                    callback: function () {
                        me.filterStoreByType(outboundStore, me.portType);
                        me.filterStoreByIds(outboundStore, ids);
                        me.fireEvent('chooseAllComPortPools');
                    }
                });
                break;
        }
    },

    navigateAddPool: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute(this.currentUrl + '/addComPortPool').forward();
    },

    saveState: function () {
        var form = this.getAddComPortForm();
        delete this.portModel;
        this.portModel = Ext.create(Mdc.model.OutboundComPort);
        form.updateRecord(this.portModel);
    },

    restoreState: function () {
        var me = this;
        var form = this.getAddComPortForm();
        this.portModel ? form.loadRecord(me.portModel) :
            this.portModel = Ext.create(Mdc.model.OutboundComPort);
    }
});