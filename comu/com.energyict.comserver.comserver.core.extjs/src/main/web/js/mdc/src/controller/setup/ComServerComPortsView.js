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
                    comPortsStore.load({
                        callback: function () {
                            widget.down('comServerComPortsGrid').reconfigure(comPortsStore);
                            widget.setLoading(false);
                            me.getApplication().fireEvent('comServerOverviewLoad', record);
                            widget.down('comserversidemenu #comserverLink').setText(record.get('name'));
                        }
                    })

                }
            });
        }, false);
    },

    showPreview: function (selectionModel, record, showComServer) {
        var previewPanel = this.getPreview(),
            menu = previewPanel.down('menu'),
            model = this.getModel('Mdc.model.ComServerComPort'),
            id = record.getId(),
            currentForm = previewPanel.down('form[hidden=false]'),
            comServerNameField,
            form;

        previewPanel.setLoading(true);

        if (menu) {
            menu.record = record;
        }
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
                                    form.down('displayfield[name=outboundComPortPoolIdsDisplay]').hide();
                                    form.down('displayfield[name=inboundComPortPools]').show();
                                    break;
                                case 'Outbound':
                                    form.down('displayfield[name=outboundComPortPoolIdsDisplay]').show();
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
            record = menu.record;

        switch (item.action) {
            case 'edit':
                me.editComPortOnComServer(record);
                break;
            case 'remove':
                me.showDeleteConfirmation(record);
                break;
            case 'activate':
                me.toggleActivation(record);
                break;
            case 'deactivate':
                me.toggleActivation(record);
                break;
        }
    },

    toggleActivation: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        record.set('active', !record.get('active'));
        record.save({
            isNotEdit: true,
            success: function (record) {
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', record.get('active')
                    ? Uni.I18n.translate('comPortOnComServer.changeState.activated', 'MDC', 'Communication port activated')
                    : Uni.I18n.translate('comPortOnComServer.changeState.deactivated', 'MDC', 'Communication port deactivated'));
            },
            failure: function (record, options) {
                var title,
                    errorsArray,
                    message;

                record.reject();
                if (options && options.response.status === 400) {
                    title = Uni.I18n.translate('comServerComPorts.activation.failurex', 'MDC', "Failed to activate '{0}'",record.get('name'));
                    errorsArray = Ext.decode(options.response.responseText);
                    message = '';
                    Ext.Array.each(errorsArray, function (obj) {
                        message += obj.msg + '.'
                    });
                    me.getApplication().getController('Uni.controller.Error').showError(title, message);
                }
            }
        });
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
            router = me.getController('Uni.controller.history.Router'),
            page = me.getComServerComportsView();

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            success: function () {
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServerComPorts.deleteSuccess.msg', 'MDC', 'Communication port removed'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});
