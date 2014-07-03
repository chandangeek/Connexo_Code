Ext.define('Mdc.controller.setup.ComPortPools', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComPort'
    ],

    views: [
        'setup.comportpool.ComPortPoolsSetup',
        'setup.comport.OutboundComPortSelectionWindow'
    ],

    stores: [
        'ComPortPools',
        'ComPorts',
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    refs: [
        {ref: 'comPortPoolsSetup', selector:'comPortPoolsSetup'},
        {ref: 'comPortPoolGrid', selector: '#comportpoolsgrid'},
        {ref: 'comPortPoolPreview', selector: '#comportpoolpreview'},
        {ref: 'previewActionMenu', selector: 'comPortPoolPreview #comportpoolViewMenu'}
    ],


    init: function () {
        this.control({
            'comPortPoolsGrid': {
                itemdblclick: this.editComPortPool,
                select: this.showComPortPoolPreview
            },
            '#comportpoolViewMenu': {
                click: this.chooseAction,
                show: this.configureMenu
            },
            '#addComPortPoolMenu' : {
                click: this.addAction
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

    addAction: function(menu, item){
        var router = this.getController('Uni.controller.history.Router');
        switch (item.action) {
            case 'addInbound':
                router.getRoute('administration/comportpools/addinbound').forward();
                break;
            case 'addOutbound':
                router.getRoute('administration/comportpools/addoutbound').forward();
                break;
        }
    },

    chooseAction : function (menu, item) {
        var me = this,
            gridView = me.getComPortPoolGrid().getView(),
            record = gridView.getSelectionModel().getLastSelected(),
            form = me.getComPortPoolPreview().down('form'),
            activeChange = 'notChange';

        switch (item.action) {
            case 'edit':
                me.editComPortPool(record);
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
        }

        if (activeChange != 'notChange') {
            record.set('active', activeChange);
            record.save({
                callback: function (model) {
                    var msg = activeChange ? Uni.I18n.translate('comPortPool.changeState.activated', 'MDC', 'activated') :
                        Uni.I18n.translate('comPortPool.changeState.deactivated', 'MDC', 'deactivated');
                    gridView.refresh();
                    form.loadRecord(model);
                    me.getPreviewActionMenu().record = model;
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comPortPool.changeState.msg', 'MDC', 'Communication port pool' + ' ' + msg));
                }
            });
        }

    },

    showComPortPoolPreview: function (selectionModel, record) {
        var itemPanel = this.getComPortPoolPreview(),
            form = itemPanel.down('form'),
            model = this.getModel('Mdc.model.ComPortPool'),
            id = record.getId();
        itemPanel.setLoading(this.getModel('Mdc.model.ComPortPool'));
        model.load(id, {
            success: function (record) {
                if (!form.isDestroyed) {
                    record.get('direction').toLowerCase() == 'outbound' ? form.down('displayfield[name=discoveryProtocolPluggableClassId]').hide() :
                        form.down('displayfield[name=discoveryProtocolPluggableClassId]').show() ;
                    form.loadRecord(record);
                    form.up('panel').down('menu').record = record;
                    itemPanel.setLoading(false);
                    itemPanel.setTitle(record.get('name'));
                }
            }
        });
    },

    editComPortPool: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();
        router.getRoute('administration/comportpools/edit').forward({id: id});
    },

    showDeleteConfirmation: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comPortPool.deleteConfirmation.msg', 'MDC', 'This communication port pool will no longer be available.'),
            title: Ext.String.format(Uni.I18n.translate('comPortPool.deleteConfirmation.title', 'MDC', 'Remove communication port pool \'{0}\'?'), record.get('name')),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        me.deleteComPortPool(record);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    deleteComPortPool: function (record) {
        var me = this,
            page = me.getComPortPoolsSetup();
        page.setLoading('Removing...');
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.response.status == 204) {
                    me.getComPortPoolGrid().getStore().loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comPortPool.deleteSuccess.msg', 'MDC', 'Communication port pool removed'));
                } else if (operation.response.status == 400) {
                    me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('general.during.removing', 'MDC', 'Error during removing'), response.responseText);
                }
            }
        });
    }
});
