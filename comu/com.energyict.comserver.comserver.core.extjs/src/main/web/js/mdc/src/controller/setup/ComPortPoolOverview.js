Ext.define('Mdc.controller.setup.ComPortPoolOverview', {
    extend: 'Mdc.controller.setup.ComPortPools',

    requires: [
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    models: [
        'Mdc.model.ComPortPool'
    ],

    views: [
        'Mdc.view.setup.comportpool.Overview'
    ],

    stores: [
        'Mdc.store.ComPortPools',
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    refs: [
        {
            ref: 'comPortPoolOverview',
            selector: 'comPortPoolOverview'
        }
    ],

    init: function () {
        this.control({
            'comPortPoolOverview comportpool-actionmenu': {
                click: this.chooseAction,
                show: this.configureMenu
            }
        });
    },

    showOverview: function (id) {
        var me = this,
            widget = Ext.widget('comPortPoolOverview', {
                poolId: id
            }),
            model = me.getModel('Mdc.model.ComPortPool'),
            deviceDiscoveryProtocolsStore = this.getStore('Mdc.store.DeviceDiscoveryProtocols');

        !deviceDiscoveryProtocolsStore.getCount() && deviceDiscoveryProtocolsStore.load();

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        model.load(id, {
            success: function (record) {
                var form = widget.down('form');
                if (record.get('direction').toLowerCase() === 'outbound') {
                    form.down('[name=discoveryProtocolPluggableClassId]').hide();
                }
                form.loadRecord(record);
                var actionMenu = widget.down('comportpool-actionmenu');
                if(actionMenu)
                    actionMenu.record = record;
                widget.down('comportpoolsidemenu #comportpoolLink').setText(record.get('name'));
                me.getApplication().fireEvent('comPortPoolOverviewLoad', record);
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    },

    configureMenu: function (menu) {
        var activate = menu.down('#activate'),
            deactivate = menu.down('#deactivate'),
            active = this.getComPortPoolOverview().down('form').getRecord().get('active');

        if (active) {
            deactivate.show();
            activate.hide();
        } else {
            activate.show();
            deactivate.hide();
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            form = me.getComPortPoolOverview().down('form'),
            record = form.getRecord(),
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
                    var msg = activeChange ? Uni.I18n.translate('general.activated', 'MDC', 'activated') :
                        Uni.I18n.translate('general.deactivated', 'MDC', 'deactivated');
                    form.loadRecord(model);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool') + ' ' + msg);
                }
            });
        }

    },

    editComPortPool: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/comportpools/detail/edit').forward();
    },

    deleteComPortPool: function (record) {
        var me = this,
            page = me.getComPortPoolOverview();
        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.wasSuccessful()) {
                    var router = me.getController('Uni.controller.history.Router');
                    router.getRoute('administration/comportpools').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comPortPool.deleteSuccess.msg', 'MDC', 'Communication port pool removed'));
                }
            }
        });
    }
});
