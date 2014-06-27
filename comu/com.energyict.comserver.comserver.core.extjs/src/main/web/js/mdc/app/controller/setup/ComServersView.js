Ext.define('Mdc.controller.setup.ComServersView', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.ComServer'
    ],

    views: [
        'Mdc.view.setup.comserver.ComServersSetup'
    ],

    stores: [
        'ComServers'
    ],

    refs: [
        {
            ref: 'comServersView',
            selector: 'comServersSetup'
        },
        {
            ref: 'comServerGrid',
            selector: 'comServersSetup comServersGrid'
        },
        {
            ref: 'comServerPreview',
            selector: 'comServersSetup comServerPreview'
        }
    ],

    init: function () {
        var me = this;
        this.control({
            'comServersGrid': {
                itemdblclick: this.editComServer,
                select: this.showComServerPreview
            },
            '#comserverViewMenu': {
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

    chooseAction: function (menu, item) {
        var me = this,
            record = menu.record,
            activeChange = 'notChanged',
            gridView = me.getComServerGrid().getView(),
            form = this.getComServerPreview().down('form');

        switch (item.action) {
            case 'edit':
                me.editComServer(record);
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

        if (activeChange != 'notChanged') {
            record.set('active', activeChange);
            record.save({
                callback: function (model) {
                    var msg = activeChange ? Uni.I18n.translate('comServer.changeState.activated', 'MDC', 'activated') :
                        Uni.I18n.translate('comServer.changeState.deactivated', 'MDC', 'deactivated');
                    gridView.refresh();
                    form.loadRecord(model);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServer.changeState.msg', 'MDC', 'Communication server ' + msg));
                }
            });
        }
    },

    showComServerPreview: function (selectionModel, record) {
        var itemPanel = this.getComServerPreview(),
            form = itemPanel.down('form'),
            model = this.getModel('Mdc.model.ComServer'),
            id = record.getId();

        itemPanel.setLoading(this.getModel('Mdc.model.ComServer'));

        model.load(id, {
            success: function (record) {
                if (!form.isDestroyed) {
                    form.loadRecord(record);
                    form.up('panel').down('menu').record = record;
                    itemPanel.setLoading(false);
                    itemPanel.setTitle(record.get('name'));
                }
            }
        });
    },

    editComServer: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();

        router.getRoute('administration/comservers/edit').forward({id: id});
    },

    showDeleteConfirmation: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comServer.deleteConfirmation.msg', 'MDC', 'This communication server will no longer be available.'),
            title: Ext.String.format(Uni.I18n.translate('comServer.deleteConfirmation.title', 'MDC', "Remove '{0}'?"), record.get('name')),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        me.deleteComserver(record);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    deleteComserver: function (record) {
        var me = this,
            page = me.getComServersView();

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.wasSuccessful()) {
                    me.getComServerGrid().getStore().loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServer.deleteSuccess.msg', 'MDC', 'Communication server removed'));
                }
            }
        });
    }
});
