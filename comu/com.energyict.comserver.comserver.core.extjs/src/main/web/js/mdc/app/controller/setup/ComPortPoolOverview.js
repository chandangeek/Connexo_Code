Ext.define('Mdc.controller.setup.ComPortPoolOverview', {
    extend: 'Mdc.controller.setup.ComPortPools',

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
                click: this.chooseAction
            }
        });
    },

    showOverview: function (id) {
        var me = this,
            widget = Ext.widget('comPortPoolOverview'),
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
                widget.down('comportpool-actionmenu').record = record;
                me.getApplication().fireEvent('comPortPoolOverviewLoad', record);
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
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
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comportpool.deleteSuccess.msg', 'MDC', 'Communication port pool removed'));
                }
            }
        });
    }
});
