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
            'comserver-actionmenu': {
                click: this.chooseAction,
                show: this.configureMenu
            },
            'comServersGrid button[action=add] menuitem': {
                click: this.add
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
            activeChange = -1,
            grid = me.getComServerGrid(),
            store = grid.getStore(),
            selectionModel;

        switch (item.action) {
            case 'edit':
                me.editComServer(record);
                break;
            case 'remove':
                me.deleteComserver(record);
                break;
            case 'activate':
                activeChange = true;
                record.set('active', true);
                break;
            case 'deactivate':
                activeChange = false;
                break;
        }

        if (activeChange != -1) {
            selectionModel = grid.getSelectionModel();
            record.set('active', activeChange);
            delete record.data.comportslink;
            record.save({
                callback: function (model) {
                    store.load(function () {
                        grid.fireEvent('select', selectionModel, model);
                    });
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

    deleteComserver: function (record) {
        var me = this;
        record.destroy({
            callback: function () {
                me.getComServerGrid().getStore().loadPage(1);
            }
        });
    }
});
