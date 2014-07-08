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
        'Mdc.store.InboundComPortPools'
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
        }
    ],


    init: function () {
        this.control({
            '#comPortTypeSelect': {
                change: this.changeType
            },
            '#addComPortForm button[action=cancel]' : {
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
            '#addComPortPoolsGrid button[action=uncheckAll]': {
                click: this.uncheckAll
            },
            '#addComPortPoolsGrid #createEditButton': {
                click: this.addComportPool
            },
            '#addComPortPoolsGrid #cancelLink': {
                click: this.cancelAddPool
            },
            'outboundportcomportpools': {
                afterrender: this.updatePoolCount
            }
        });

    },

    updatePoolCount: function (grid) {
        var store = grid.getStore(),
            countMsg = grid.down('#comPortPoolsCount'),
            count = store.getCount();
        count > 0 ? countMsg.update(count + Uni.I18n.translate('comServerComPorts.addPools.count', 'MDC', ' communication port pools')) :
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

    updateSelection: function (selModel, selected) {
        var me = this,
            label = me.getComPortPoolsGridSelection(),
            count = selected.length;
        count > 0 ?
            label.setText(count + Uni.I18n.translate('comServerComPorts.addPools.selectedCount', 'MDC', ' communication port pools selected')) :
            label.setText(Uni.I18n.translate('comServerComPorts.addPools.noSelectedCount', 'MDC', 'No communication port pools selected'))
    },

    changeType: function (combo, newValue) {
        this.portType = newValue;
        this.saveState();
        this.comportEdit.showForm(this.portDirection, newValue);
        this.restoreState()
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

    cancelAddPool: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/comservers/detail/comports/addOutbound').forward();
    },

    cancelClick: function () {
        var router = this.getController('Uni.controller.history.Router');
        delete this.portModel;
        router.getRoute('administration/comservers/detail/comports').forward();
    },

    addClicked: function (btn) {
        var me =this,
            form = btn.up('form'),
            typeModel,
            model,
            formValid,
            formErrorsPanel = form.down('[name=form-errors]'),
            comPortPool = this.getStore('Mdc.store.AddComPortPools'),
            ids,
            url;
        switch (this.portDirection) {
            case 'inbound':
                typeModel = Ext.create(Mdc.model.InboundComPort);
                formValid = form.isValid();
                break;
            case 'outbound':
                typeModel = Ext.create(Mdc.model.OutboundComPort);
                ids = comPortPool.collect('id');
                formValid = (form.isValid() && (ids.length > 0));
                break;
        }
        if (formValid) {
            formErrorsPanel.hide();
            model = me.formToModel(form, typeModel);
            ids && (model.data.outboundComPortPoolIds = ids);
            model.data.active = false;
            model.data.comServer_id = me.comServerId;
            model.data.direction = me.portDirection;
            model.data.type = me.portDirection + '_' + model.data.comPortType;
            url = '../../api/mdc/comservers/' + me.comServerId + '/comports';
            model.getProxy().url = url;
            model.save({
                callback: function (records, operation, success) {
                    console.log(success);
                    if (success) {
                        me.onSuccessSaving(me.portDirection);
                    } else {
                        me.onFailureSaving(operation.response, form);
                    }
                }
            })
        } else {
            formErrorsPanel.show()
        }
    },

    onSuccessSaving: function (portDirection) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (portDirection) {
            case 'inbound':
                messageText = Uni.I18n.translate('comServerComPorts.form.addInboundSuccess', 'MDC', 'Inbound communication port successfuly added');
                break;
            case 'outbound':
                messageText = Uni.I18n.translate('comServerComPorts.form.addOutboundSuccess', 'MDC', 'Outbound communication port successfuly added');
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


    showAddOutbound: function (id) {
        var me = this,
            widget = Ext.widget('comportEdit');
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
        widget.showForm('outbound', me.defaultType);
        this.restoreState()
    },

    showAddInbound: function (id) {
        var me = this,
            widget = Ext.widget('comportEdit');
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
    },

    showAddComPortPool: function () {
        var me = this,
            widget = Ext.widget('addComPortPool');
        me.saveState();
        me.getApplication().fireEvent('changecontentevent', widget);
        me.filterStore();
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
                    callback: me.filterStoreByType(inboundStore, me.portType)
                });
                break;
            case 'outbound':
                var outboundStore = me.getStore('Mdc.store.OutboundComPortPools'),
                    addStore = me.getStore('Mdc.store.AddComPortPools'),
                    ids = addStore.collect('id');
                outboundStore.load({
                    callback: function () {
                        me.filterStoreByType(outboundStore, me.portType);
                        me.filterStoreByIds(outboundStore, ids)
                    }
                });
                break;
        }
    },

    navigateAddPool: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/comservers/detail/comports/addOutbound/addComPortPool').forward();
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