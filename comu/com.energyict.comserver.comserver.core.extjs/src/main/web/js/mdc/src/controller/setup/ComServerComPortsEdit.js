Ext.define('Mdc.controller.setup.ComServerComPortsEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.TimeUnits'
    ],

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
        'TimeUnits',
        'Mdc.store.AddComPortPools',
        'Mdc.store.OutboundComPortPools',
        'Mdc.store.InboundComPortPools',
        'Mdc.store.ComPorts'
    ],

    defaultType: 'TYPE_TCP',

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
            '#comPortEdit checkbox[name=useHttps]': {
                change: this.enablePassFields
            },
            'button[action=showAddComPortPoolPage]': {
                click: this.navigateAddPool
            },
            'outboundportcomportpools gridview': {
                refresh: this.updatePoolCount
            },
            '#addComPortPoolToComPort #createEditButton': {
                click: this.addComportPool
            },
            '#addComPortPoolToComPort #cancelLink': {
                click: this.cancelAddPool
            },
            'addComPortPool add-com-port-pools-grid': {
                allitemsadd: this.onAllComPortPoolsAdd,
                selecteditemsadd: this.onSelectedComPortPoolsAdd
            }
        });

    },

    enablePassFields: function(checkbox) {
        var state = checkbox.getValue(),
            editView = this.getComPortEdit(),
            keyPathField = editView.down('#keyStoreFilePath'),
            keyPasswordField = editView.down('#keyStorePassword'),
            trustPathField = editView.down('#trustStoreFilePath'),
            trustPasswordField = editView.down('#trustStorePassword');

        if (state) {
            keyPathField.enable();
            keyPasswordField.enable();
            trustPathField.enable();
            trustPasswordField.enable();
        } else {
            keyPathField.disable();
            keyPasswordField.disable();
            trustPathField.disable();
            trustPasswordField.disable();
        }
    },

    updatePoolCount: function (gridview) {
        var grid = gridview.up('gridpanel'),
            store = grid.getStore(),
            countMsg = grid.down('#comPortPoolsCount'),
            count = store.getCount();
        if (count == 1) {
            countMsg.update(count + ' ' + Uni.I18n.translate('comServerComPorts.addPool.count', 'MDC', ' communication port pool'));
        } else {
            count ? countMsg.update(count + ' ' + Uni.I18n.translate('comServerComPorts.addPools.count', 'MDC', ' communication port pools')) :
                countMsg.update(Uni.I18n.translate('comServerComPorts.addPools.noPools', 'MDC', 'No communication port pools'));
        }
    },

    onAllComPortPoolsAdd: function () {
        this.addComportPool(true);
    },

    onSelectedComPortPoolsAdd: function (selection) {
        this.addComportPool(false, selection);
    },

    addComportPool: function (allPressed, selection) {
        var addStore = this.getStore('Mdc.store.AddComPortPools');

        if (allPressed) {
            selection = this.getAddComPortPoolsGrid().store.data.items;
        }

        addStore.loadData(selection, true);
        this.cancelAddPool();
    },

    changeType: function (combo) {
        var me = this;
        if (!Ext.isObject(combo.getValue()) && !combo.suspendChangeEvent) {
            this.portType = combo.getValue();
            this.saveState();
            this.comportEdit.showForm(this.portDirection, this.portType);

            //SERIAL form requires some fields to have preset defaults
            if (this.portType == 'TYPE_SERIAL') {
                var editView = this.getComPortEdit();
                Uni.util.Common.loadNecessaryStores([
                    'Mdc.store.FlowControls',
                    'Mdc.store.Parities'
                ], function () {
                    me.setDefaultValuesForSerial(editView);
                }, false);
            }
            if (!this.restorePools) {
                this.getStore('Mdc.store.AddComPortPools').removeAll();
                if (this.getComPortPoolsGrid()) {
                    this.getComPortPoolsGrid().down('#comPortPoolsCount').update(
                        Uni.I18n.translate('comServerComPorts.addPools.noPools', 'MDC', 'No communication port pools'));
                }
            } else {
                this.restorePools = undefined;
            }
            if (!this.currentUrl.includes('edit')) {
                this.restoreState();
            }
            this.filterStore();
        }
    },

    setDefaultValuesForSerial: function (view) {
        var connectTimeoutUnit = view.down('#addFormNest').down('#connectTimeoutUnit'),
            connectDelayUnit = view.down('#addFormNest').down('#connectDelayUnit'),
            sendDelayUnit = view.down('#addFormNest').down('#sendDelayUnit'),
            atCommandTimeoutUnit = view.down('#addFormNest').down('#atCommandTimeoutUnit'),
            baudRate = view.down('#addFormNest').down('#bitsPerSecond'),
            nrOfDatabits = view.down('#addFormNest').down('#bits'),
            parity = view.down('#addFormNest').down('#parity'),
            flowControl = view.down('#addFormNest').down('#flowControl'),
            nrOfStopBits = view.down('#addFormNest').down('#stopBits');
        connectTimeoutUnit.setValue('seconds', true);
        connectDelayUnit.setValue('milliseconds', true);
        sendDelayUnit.setValue('milliseconds', true);
        atCommandTimeoutUnit.setValue('seconds', true);
        baudRate.setValue('9600', true);
        nrOfDatabits.setValue('8', true);
        nrOfStopBits.setValue('1', true);
        parity.setValue('parities_none', true);
        flowControl.setValue('flowcontrol_none', true);
    },

    formToModel: function (form, model) {
        form.updateRecord(model);
        return model;
    },

    cancelAddPool: function () {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        router.getRoute(this.currentUrl).forward();
    },

    cancelClick: function () {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');
        delete me.portModel;
        router.getRoute('administration/comservers/detail/comports').forward();
    },

    addClicked: function (btn) {
        var me = this,
            form = btn.up('form'),
            queryString = Ext.Object.toQueryString(form.getValues()),
            values = Ext.Object.fromQueryString(queryString, true),
            formErrorsPanel = form.down('[name=form-errors]'),
            comPortPool = this.getStore('Mdc.store.AddComPortPools'),
            comPortTypeField = form.down('#comPortTypeSelect'),
            comPortTypeFieldValueObject = comPortTypeField.findRecordByValue(comPortTypeField.getValue()).getData(),
            modemInit = [],
            globalModemInitStrings = [],
            typeModel,
            record,
            actionType,
            ids = [];

        if (form.isValid()) {
            formErrorsPanel.hide();
            switch (this.portDirection.toLowerCase()) {
                case 'inbound':
                    typeModel = Ext.create(Mdc.model.InboundComPort);
                    break;
                case 'outbound':
                    typeModel = Ext.create(Mdc.model.OutboundComPort);
                    comPortPool.each(function (record) {
                        ids.push(_.pick(record.getData(), 'id', 'version'));
                    });
                    break;
            }
            switch (btn.action) {
                case 'addModel':
                    actionType = Uni.I18n.translate('general.added', 'MDC', 'added');
                    record = me.formToModel(form, typeModel);
                    record.beginEdit();
                    record.set('active', false);
                    record.set('comServer_id', me.comServerId);
                    record.set('direction', me.portDirection);
                    record.set('comPortType', comPortTypeFieldValueObject);
                    record.set('type', me.portDirection + '_' + record.getData().comPortType.id.substring(5));
                    record.endEdit();
                    record.getProxy().url = '/api/mdc/comservers/' + me.comServerId + '/comports';
                    break;
                case 'editModel':
                    actionType = Uni.I18n.translate('general.saved', 'MDC', 'saved');
                    record = me.formToModel(form, me.recordToEdit);
                    break;
            }
            if (values.modemInitStrings) {
                modemInit = me.parseModemStringToArray(values.modemInitStrings);
            }
            if (values.globalModemInitStrings) {
                globalModemInitStrings = me.parseGlobalModemStringToArray(values.globalModemInitStrings);
            }
            if (!values.useHttps) {
                record.set('useHttps', false);
            }
            record.set('modemInitStrings', modemInit);
            record.set('globalModemInitStrings', globalModemInitStrings);
            ids && (record.set('outboundComPortPoolIds', ids));
            if (comPortTypeField.getValue() == 'TYPE_SERIAL' && this.portDirection.toLowerCase() == 'inbound') {
                record.set('flowControl', form.down('#flowControl').findRecordByValue(form.down('#flowControl').getValue()).getData());
                record.set('parity', form.down('#parity').findRecordByValue(form.down('#parity').getValue()).getData());
                record.set('connectTimeout', values.atCommandTimeout);
                record.set('delayAfterConnect', values.delayAfterConnect);
                record.set('delayBeforeSend', values.delayBeforeSend);
                record.set('atCommandTimeout', values.atCommandTimeout);
            }
            me.portModel = null;
            record.save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/comservers/detail/comports').buildUrl(),
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
        Ext.Array.each(stringsArray, function (str) {
            returnedArray.push({'modemInitString': str});
        });

        return returnedArray;
    },

    parseModemArrayToString: function (array) {
        var string = '';
        Ext.Array.each(array, function (value) {
            if (value == array[array.length-1]) {
                string += value.modemInitString;
            } else {
                string += value.modemInitString + ';';
            }
        });

        return string;
    },

    parseGlobalModemStringToArray: function (string) {
        var stringsArray = string.split(';'),
            returnedArray = [];
        Ext.Array.each(stringsArray, function (str) {
            returnedArray.push({'globalModemInitString': str});
        });

        return returnedArray;
    },

    parseGlobalModemArrayToString: function (array) {
        var string = '';
        Ext.Array.each(array, function (value) {
            if (value == array[array.length-1]) {
                string += value.globalModemInitString;
            } else {
                string += value.globalModemInitString + ';';
            }
        });

        return string;
    },

    onSuccessSaving: function (portDirection, actionType) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (portDirection.toLowerCase()) {
            case 'inbound':
                messageText = Uni.I18n.translate('comServerComPorts.form.addInboundSuccess', 'MDC', 'Inbound communication port {0}',[actionType]);
                break;
            case 'outbound':
                messageText = Uni.I18n.translate('comServerComPorts.form.addOutboundSuccess', 'MDC', 'Outbound communication port {0}',[actionType]);
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        delete me.portModel;
        me.portType = me.defaulType;
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

    loadTimeUnits: function() {
        var timeUnitsStore = this.getStore('TimeUnits');
        timeUnitsStore.load();
    },

    showEditView: function (id, direction, comPortId) {
        var me = this,
            widget = Ext.widget('comportEdit', {serverId: id}),
            addComPortPoolsStore = me.getStore('Mdc.store.AddComPortPools'),
            outboundComPortPoolsStore = me.getStore('Mdc.store.OutboundComPortPools'),
            inboundStore = me.getStore('Mdc.store.InboundComPortPools'),
            comServerModel = me.getModel('Mdc.model.ComServer'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: widget
            }),
            comportTypeSelectCombo,
            actionButton,
            portModel,
            addForm,
            comPortPoolsGrid,
            recordData,
            directionField;

        me.loadTimeUnits();
        me.getApplication().fireEvent('changecontentevent', widget);
        preloader.show();

        comServerModel.load(id, {
            success: function (record) {
                widget.down('#comserversidemenu #comserverLink').setText(record.get('name'));
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            }
        });

        switch (direction.toLowerCase()) {
            case 'inbound':
                portModel = me.getModel('Mdc.model.InboundComPort');
                break;
            case 'outbound':
                portModel = me.getModel('Mdc.model.OutboundComPort');
                break;
        }


        me.currentUrl = 'administration/comservers/detail/comports/edit';
        me.comServerId = id;
        me.comPortId = parseInt(comPortId);
        me.comportEdit = widget;


        portModel.getProxy().url = portModel.getProxy().url.replace('{comServerId}', me.comServerId);
        Uni.util.Common.loadNecessaryStores([
            'Mdc.store.FlowControls',
            'Mdc.store.Parities',
            'Mdc.store.ComPortTypes'
        ], function () {
            portModel.load(me.comPortId, {
                success: function (record) {
                    recordData = record.getData();
                    me.recordToEdit = record;
                    me.portType = recordData.comPortType.id;
                    me.portDirection = me.getDirection(record);
                    me.getApplication().fireEvent('loadComPortOnComServer', recordData.name);
                    actionButton = Ext.ComponentQuery.query('#comPortEdit #addEditButton')[0];
                    directionField = Ext.ComponentQuery.query('#comPortEdit displayfield[name=direction]')[0];
                    actionButton.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                    actionButton.action = 'editModel';
                    directionField.show();

                    me.getAddComPortForm().setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", recordData.name));
                    widget.showForm(me.portDirection, me.portType);
                    addForm = widget.down('#addComPortForm');
                    comportTypeSelectCombo = widget.down('#comPortTypeSelect');
                    switch (me.portDirection.toLowerCase()) {
                        case 'inbound':
                            addForm.loadRecord(record);
                            comportTypeSelectCombo.suspendChangeEvent = true;
                            comportTypeSelectCombo.setValue(recordData.comPortType.id);
                            comportTypeSelectCombo.suspendChangeEvent = false;
                            inboundStore.load({
                                callback: function () {
                                    me.filterStoreByType(inboundStore, me.portType);
                                    preloader.destroy();
                                }
                            });
                            if (me.portType == 'TYPE_SERIAL') {
                                var connectTimeoutCount = widget.down('#addFormNest').down('#connectTimeoutCount'),
                                    connectTimeoutUnit = widget.down('#addFormNest').down('#connectTimeoutUnit'),
                                    connectDelayCount = widget.down('#addFormNest').down('#connectDelayCount'),
                                    connectDelayUnit = widget.down('#addFormNest').down('#connectDelayUnit'),
                                    sendDelayCount = widget.down('#addFormNest').down('#sendDelayCount'),
                                    sendDelayUnit = widget.down('#addFormNest').down('#sendDelayUnit'),
                                    atCommandTimeoutCount = widget.down('#addFormNest').down('#atCommandTimeoutCount'),
                                    atCommandTimeoutUnit = widget.down('#addFormNest').down('#atCommandTimeoutUnit'),
                                    globalInitModem = widget.down('#addFormNest').down('textfield[name=globalModemInitStrings]'),
                                    initModem = widget.down('#addFormNest').down('textfield[name=modemInitStrings]');
                                connectTimeoutCount.setValue(recordData.connectTimeout.count);
                                connectTimeoutUnit.setValue(recordData.connectTimeout.timeUnit);
                                connectDelayCount.setValue(recordData.delayAfterConnect.count);
                                connectDelayUnit.setValue(recordData.delayAfterConnect.timeUnit);
                                sendDelayCount.setValue(recordData.delayBeforeSend.count);
                                sendDelayUnit.setValue(recordData.delayBeforeSend.timeUnit);
                                atCommandTimeoutCount.setValue(recordData.atCommandTimeout.count);
                                atCommandTimeoutUnit.setValue(recordData.atCommandTimeout.timeUnit);
                                globalInitModem.setValue(me.parseGlobalModemArrayToString(recordData.globalModemInitStrings));
                                initModem.setValue(me.parseModemArrayToString(recordData.modemInitStrings));
                                widget.down('#flowControl').setValue(recordData.flowControl.id);
                                widget.down('#parity').setValue(recordData.parity.id);
                            }

                            break;
                        case 'outbound':
                            if (me.portModel) {
                                me.portModel.set('comPortType', me.portType);
                                comportTypeSelectCombo.suspendChangeEvent = true;
                                me.restoreState();
                                comportTypeSelectCombo.setValue(recordData.comPortType.id);
                                comportTypeSelectCombo.suspendChangeEvent = false;
                                preloader.destroy();
                            } else {
                                me.portModel = record;
                                addForm.loadRecord(record);
                                comportTypeSelectCombo.suspendChangeEvent = true;
                                comportTypeSelectCombo.setValue(recordData.comPortType.id);
                                comportTypeSelectCombo.suspendChangeEvent = false;
                                addComPortPoolsStore.removeAll();
                                comPortPoolsGrid = me.getComPortPoolsGrid();
                                outboundComPortPoolsStore.load({
                                    callback: function () {
                                        me.filterStoreByType(outboundComPortPoolsStore, me.portType);
                                        Ext.Array.each(recordData.outboundComPortPoolIds, function (comPortPool) {
                                            var correspondingComPortPool = outboundComPortPoolsStore.getById(comPortPool.id);

                                            if (correspondingComPortPool) {
                                                addComPortPoolsStore.add(correspondingComPortPool);
                                            }
                                        });
                                        preloader.destroy();
                                    }
                                });
                            }
                            break;
                    }
                    comportTypeSelectCombo.disable();
                }
            });
        }, false);
    },

    getDirection: function (record) {
        if (record.get('type').search(/inbound/i) != -1) {
            return 'Inbound';
        } else if (record.get('type').search(/outbound/i) != -1) {
            return 'Outbound';
        }
    },

    showAddOutbound: function (id) {
        var me = this,
            widget = Ext.widget('comportEdit', {serverId: id}),
            comServerModel = me.getModel('Mdc.model.ComServer');

        me.loadTimeUnits();
        me.currentUrl = 'administration/comservers/detail/comports/addOutbound';
        me.comServerId = id;
        me.comportEdit = widget;
        me.portDirection = 'outbound';
        me.comPortId = undefined;
        me.portType = me.portType ? me.portType : me.defaultType;

        me.getApplication().fireEvent('changecontentevent', widget);
        me.getAddComPortForm().setTitle(Uni.I18n.translate('comServerComPorts.addOutboundPort', 'MDC', 'Add outbound communication port'));
        me.getStore('Mdc.store.ComPortTypes').load({
            callback: function () {
                var index = this.find('id', 'TYPE_SERVLET');
                index > 0 ? this.removeAt(index, 1) : null;
                widget.down('#comPortTypeSelect').setValue(me.portType);
            }
        });

        comServerModel.load(id, {
            success: function (record) {
                if (widget.down('#comserversidemenu')) {
                    widget.down('#comserversidemenu #comserverLink').setText(record.get('name'));
                }
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            }
        });

        widget.showForm('outbound', me.defaultType);
        this.restorePools = true;
        this.restoreState();
    },

    showAddInbound: function (id) {
        var me = this,
            widget = Ext.widget('comportEdit', {serverId: id}),
            comServerModel = me.getModel('Mdc.model.ComServer');

        me.loadTimeUnits();
        me.currentUrl = 'administration/comservers/detail/comports/addInbound';
        me.portType = me.defaultType;
        me.comServerId = id;
        me.comportEdit = widget;
        me.comPortId = undefined;
        me.portDirection = 'inbound';

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.showForm('inbound', me.defaultType);
        this.restoreState();

        me.getAddComPortForm().setTitle(Uni.I18n.translate('comServerComPorts.addInboundPort', 'MDC', 'Add inbound communication port'));
        me.getStore('Mdc.store.ComPortTypes').load(function () {
            widget.down('#comPortTypeSelect').setValue(me.portType);
        });
        me.filterStore();

        comServerModel.load(id, {
            success: function (record) {
                widget.down('#comserversidemenu #comserverLink').setText(record.get('name'));
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            }
        });
    },

    showAddComPortPool: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.widget('addComPortPool');
       // me.showAddOutbound(router.arguments.id);
        if (me.portModel) {
            me.saveState();
            me.getApplication().fireEvent('changecontentevent', widget);
            widget.updateCancelHref(me.comServerId, me.comPortId);
            me.filterStore();
        } else {
            this.currentUrl = 'administration/comservers/detail/comports/addOutbound';
            router.getRoute(this.currentUrl).forward();
        }

    },

    filterStoreByType: function (store, type) {
        store.removeFilter('typeFilter', false);
        store.filter(
            {
                filterFn: function (item) {
                    return item.get('comPortType').id == type;
                },
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
        switch (me.portDirection.toLowerCase()) {
            case 'inbound':
                var inboundStore = me.getStore('Mdc.store.InboundComPortPools');
                inboundStore.load({
                    callback: function () {
                        me.filterStoreByType(inboundStore, me.portType);
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
        var me = this,
            form = this.getAddComPortForm();

        delete me.portModel;
        me.portModel = Ext.create(Mdc.model.OutboundComPort);
        if (form.getRecord()) {
            form.updateRecord(me.portModel);
        } else {
            me.portModel.set(form.getValues());
        }
    },

    restoreState: function () {
        var me = this;
        var form = this.getAddComPortForm();
        me.portModel ? form.loadRecord(me.portModel) :
            me.portModel = Ext.create(Mdc.model.OutboundComPort);
    }
});