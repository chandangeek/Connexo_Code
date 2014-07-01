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
            ref: 'preview',
            selector: 'comServerComPortsView comServerComPortPreview'
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
            }
        });
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

        comServerModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            }
        });

        comPortModel.getProxy().url = url;
        comPortsStore.getProxy().url = url;
        me.checkLoadingOfNecessaryStores(function () {
            widget = Ext.widget('comServerComPortsView');
            addMenus = widget.query('comServerComPortsAddMenu');
            addMenus && Ext.Array.each(addMenus, function (menu) {
                menu.comServerId = id;
            });
            me.getApplication().fireEvent('changecontentevent', widget);
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
    }
});
