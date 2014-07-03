Ext.define('Mdc.controller.setup.ComServerComPortsView', {
    extend: 'Ext.app.Controller',

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
        'Mdc.store.TimeUnits',
        'Mdc.store.LogLevels',
        'Mdc.store.ComPortTypes',
        'Mdc.store.FlowControls',
        'Mdc.store.NrOfDataBits',
        'Mdc.store.NrOfStopBits'
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
            'comServerComPortsView comServerComPortsAddMenu' : {
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

    checkLoadingOfNecessaryStores: function (callback, storesArr) {
        var me = this,
            counter = storesArr.length,
            check = function () {
                counter--;
                counter === 0 && callback();
            };

        Ext.Array.each(storesArr, function (item) {
            var store = me.getStore(item);

            if (store.getCount()) {
                check();
            } else {
                store.load(check);
            }
        });
    },

    showView: function (id) {
        var me = this,
            comServerModel = me.getModel('Mdc.model.ComServer'),
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
        me.checkLoadingOfNecessaryStores(function () {
            widget = Ext.widget('comServerComPortsView');
            addMenus = widget.query('comServerComPortsAddMenu');
            addMenus && Ext.Array.each(addMenus, function (menu) {
                menu.comServerId = id;
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            comServerModel.load(id, {
                success: function (record) {
                    widget.down('comserversubmenu').setServer(record);
                    me.getApplication().fireEvent('comServerOverviewLoad', record);
                }
            });
        }, storesArr);
    },

    showPreview: function (selectionModel, record) {
        var previewPanel = this.getPreview(),
            model = this.getModel('Mdc.model.ComServerComPort'),
            id = record.getId(),
            currentForm = previewPanel.down('form[hidden=false]'),
            form;

        previewPanel.setLoading(true);

        model.load(id, {
            success: function (record) {
                if (!previewPanel.isDestroyed) {
                    previewPanel.setTitle(record.get('name'));
                    previewPanel.down('menu').record = record;
                    form = previewPanel.down('comPortForm' + record.get('comPortType'));
                    currentForm && currentForm.hide();
                    if (form) {
                        form.show();
                        form.loadRecord(record);
                    }
                    previewPanel.setLoading(false);
                }
            }
        });
    },

    addComPortToComServer: function (menu, item) {
        var router = this.getController('Uni.controller.history.Router'),
            id = menu.comServerId;

        switch (item.action) {
            case 'addInbound':
                router.getRoute('administration/comservers/' + id + 'comports/add/inbound').forward();
                break;
            case 'addOutbound':
                router.getRoute('administration/comservers/' + id + 'comports/add/outbound').forward();
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
            previewPanel = this.getPreview(),
            activeChange = 'notChange',
            storeUrl = me.getComPortsGrid().getStore().getProxy().url,
            jsonObject;

        switch (item.action) {
            case 'remove':
                me.showDeleteConfirmation(record);
                break;
            case 'activate':
                activeChange = true;
                break;
            case 'deactivate':
                activeChange = false;
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
                            var msg = activeChange ? Uni.I18n.translate('comPortOnComServer.changeState.activated', 'MDC', 'activated') :
                                Uni.I18n.translate('comPortOnComServer.changeState.deactivated', 'MDC', 'deactivated');
                            record.set('active', activeChange);
                            gridView.refresh();
                            me.getComPortsGrid().fireEvent('select', gridView, record);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comPortOnComServer.changeState.msg', 'MDC', 'Communication port ' + ' ' + msg));
                        }
                    });
                }
            });
        }

    },

    showDeleteConfirmation: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comServerComPorts.deleteConfirmation.msg', 'MDC', 'This communication port will no longer be available.'),
            title: Ext.String.format(Uni.I18n.translate('comServerComPorts.deleteConfirmation.title', 'MDC', "Remove '{0}'?"), record.get('name')),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        me.deleteComPort(record);
                        break;
                    case 'cancel':
                        this.close();
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
