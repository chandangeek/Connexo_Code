Ext.define('Mdc.controller.setup.ComServerComPortsView', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common',
        'Mdc.store.TimeUnits'
    ],

    models: [
        'Mdc.model.ComServer',
        'Mdc.model.ComServerComPort'
    ],

    views: [
        'Mdc.view.setup.comservercomports.View'
    ],

    stores: [
        'Mdc.store.ComServerComPorts',
        'Mdc.store.ComPortPools',
        'Mdc.store.BaudRates',
        'Mdc.store.Parities',
        'TimeUnits',
        'Mdc.store.LogLevels',
        'Mdc.store.ComPortTypes',
        'Mdc.store.FlowControls',
        'Mdc.store.NrOfDataBits',
        'Mdc.store.NrOfStopBits',
        'Mdc.store.ComServers'
    ],

    refs: [
        {
            ref: 'comServerComportsView',
            selector: 'comServerComPortsView'
        },
        {
            ref: 'preview',
            selector: 'comServerComPortsView comServerComPortPreview'
        },
        {
            ref: 'comPortsGrid',
            selector: 'comServerComPortsView comServerComPortsGrid'
        },
        {
            ref: 'previewActionMenu',
            selector: 'comServerComPortPreview #comServerComPortsActionMenu'
        }
    ],

    init: function () {
        this.control({
            'comServerComPortsView comServerComPortsGrid': {
                select: this.showPreview
            },
            'comServerComPortsView comServerComPortsAddMenu': {
                click: this.addComPortToComServer
            },
            'comServerComPortsView comServerComPortPreview [action=passwordVisibleTrigger]': {
                change: this.passwordVisibleTrigger
            },
            '#comServerComPortsActionMenu': {
                click: this.chooseAction,
                show: this.configureMenu
            }
        });
    },

    configureMenu: function (menu) {
        var activate = menu.down('#activate'),
            deactivate = menu.down('#deactivate'),
            active = menu.record.data.active;
        if (active) {
            deactivate.show();
            activate.hide();
        } else {
            activate.show();
            deactivate.hide();
        }
    },

    showView: function (id) {
        var me = this,
            comServerModel = me.getModel('Mdc.model.ComServer'),
            addComPortPoolsStore = me.getStore('Mdc.store.AddComPortPools'),
            comPortModel = me.getModel('Mdc.model.ComServerComPort'),
            comPortsStore = me.getStore('Mdc.store.ComServerComPorts'),
            url = comServerModel.getProxy().url + '/' + id + '/comports',
            storesArr = [
                'Mdc.store.ComPortPools',
                'Mdc.store.BaudRates',
                'Mdc.store.NrOfDataBits',
                'Mdc.store.NrOfStopBits',
                'Mdc.store.Parities',
                'Mdc.store.FlowControls',
                'Mdc.store.ComServers'
            ],
            widget,
            addMenus;


        comPortModel.getProxy().url = url;
        comPortsStore.getProxy().url = url;
        widget = Ext.widget('comServerComPortsView', {
            serverId: id
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Uni.util.Common.loadNecessaryStores(storesArr, function () {
            addMenus = widget.query('comServerComPortsAddMenu');
            addMenus && Ext.Array.each(addMenus, function (menu) {
                menu.comServerId = id;
            });
            me.getApplication().getController('Mdc.controller.setup.ComServerComPortsEdit').portModel && (delete me.getApplication().getController('Mdc.controller.setup.ComServerComPortsEdit').portModel);
            addComPortPoolsStore.removeAll();
            comServerModel.load(id, {
                success: function (record) {
                    widget.setLoading(false);
                    me.getApplication().fireEvent('comServerOverviewLoad', record);
                    widget.down('comserversidemenu #comserverLink').setText(record.get('name'));
                }
            });
        }, false);
    },

    showPreview: function (selectionModel, record, showComServer) {
        var previewPanel = this.getPreview(),
            model = this.getModel('Mdc.model.ComServerComPort'),
            id = record.getId(),
            currentForm = previewPanel.down('form[hidden=false]'),
            comServerNameField,
            form;

        previewPanel.setLoading(true);

        model.load(id, {
            success: function (record) {
                if (!previewPanel.isDestroyed) {
                    previewPanel.setTitle(Ext.String.htmlEncode(record.get('name')));
                    previewPanel.down('menu').record = record;
                    form = previewPanel.down('comPortForm' + record.get('comPortType'));
                    currentForm && currentForm.hide();
                    if (form) {
                        form.show();
                        if (record.get('comPortType') === 'SERIAL') {
                            form.showData(record.get('direction'));
                        }
                        comServerNameField = form.down('displayfield[name=comServerName]');
                        if (showComServer === true) {
                            comServerNameField.show();
                        } else {
                            comServerNameField.hide();
                        }
                        if (record.get('comPortType') != 'SERVLET') {
                            switch (record.get('direction')) {
                                case 'Inbound':
                                    form.down('displayfield[name=outboundComPortPoolIds]').hide();
                                    form.down('displayfield[name=inboundComPortPools]').show();
                                    break;
                                case 'Outbound':
                                    form.down('displayfield[name=outboundComPortPoolIds]').show();
                                    form.down('displayfield[name=inboundComPortPools]').hide();
                                    break;
                            }
                        }
                        form.loadRecord(record);
                    }
                    previewPanel.setLoading(false);
                }
            }
        });
    },

    addComPortToComServer: function (menu, item) {
        var router = this.getController('Uni.controller.history.Router');

        switch (item.action) {
            case 'addInbound':
                router.getRoute('administration/comservers/detail/comports/addInbound').forward();
                break;
            case 'addOutbound':
                router.getRoute('administration/comservers/detail/comports/addOutbound').forward();
                break;
        }
    },

    passwordVisibleTrigger: function (component, newValue) {
        var password = component.up('fieldcontainer').down('displayfield');
        password.setVisible(newValue);
    },

    chooseAction: function (menu, item) {
        var me = this,
            gridView = me.getComPortsGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected(),
            activeChange = 'notChange',
            storeUrl = me.getComPortsGrid().getStore().getProxy().url,
            jsonObject;

        switch (item.action) {
            case 'edit':
                me.editComPortOnComServer(record);
                break;
            case 'remove':
                me.showDeleteConfirmation(record);
                break;
            case 'activate':
                activeChange = true;
                break;
            case 'deactivate':
                activeChange = false;
                break;
            case 'edit':
                me.showEdit(record);
                break;
        }
        if (activeChange != 'notChange') {
            Ext.Ajax.request({
                url: storeUrl + '/' + record.getData().id,
                method: 'GET',
                success: function (response) {
                    jsonObject = Ext.JSON.decode(response.responseText);
                    jsonObject['active'] = activeChange;
                    Ext.Ajax.request({
                        url: storeUrl + '/' + record.getData().id,
                        method: 'PUT',
                        jsonData: jsonObject,
                        success: function () {
                            var msg = activeChange ? Uni.I18n.translate('general.activated', 'MDC', 'activated') :
                                Uni.I18n.translate('general.deactivated', 'MDC', 'deactivated');
                            record.set('active', activeChange);
                            gridView.refresh();
                            me.getComPortsGrid().fireEvent('select', gridView, record);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comPortOnComServer.changeState.msg', 'MDC', 'Communication port ' + ' ' + msg));
                        },
                        failure: function (response) {
                            var title = Uni.I18n.translate('comServerComPorts.activation.failure', 'MDC', 'Failed to activate') + " " + record.get('name'),
                                errorsArray = Ext.JSON.decode(response.responseText).errors,
                                message = '';

                            Ext.Array.each(errorsArray, function (obj) {
                                message += obj.msg + '.</br>'
                            });

                            me.getApplication().getController('Uni.controller.Error').showError(title, message);
                        }
                    });
                }
            });
        }
    },

    showEdit: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();
        router.getRoute('administration/comservers/detail/comports/edit').forward({id: id});
    },

    editComPortOnComServer: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();

        // todo: do not set route params
        router.arguments['comPortId'] = id;
        router.arguments['direction'] = this.lowerFirstLetter(record.getData().direction);
        router.getRoute('administration/comservers/detail/comports/edit').forward(router.arguments);
    },

    lowerFirstLetter: function (string) {
        return string.charAt(0).toLowerCase() + string.slice(1);
    },

    showDeleteConfirmation: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comServerComPorts.deleteConfirmation.msg', 'MDC', 'This communication port will no longer be available.'),
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.deleteComPort(record);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    deleteComPort: function (record) {
        var me = this,
            page = me.getComServerComportsView(),
            grid = me.getComPortsGrid(),
            gridToolbarTop = grid.down('pagingtoolbartop');

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.wasSuccessful()) {
                    gridToolbarTop.totalCount = 0;
                    grid.getStore().loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServerComPorts.deleteSuccess.msg', 'MDC', 'Communication port removed'));
                }
            }
        });
    }
});
