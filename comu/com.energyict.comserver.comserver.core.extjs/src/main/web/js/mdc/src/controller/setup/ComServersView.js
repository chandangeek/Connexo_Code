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
        },
        {
            ref: 'previewActionMenu',
            selector: 'comServerPreview #comserverViewMenu'
        }
    ],

    init: function () {
        var me = this;
        this.control({
            'comServersGrid': {
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
            grid = me.getComServerGrid(),
            activeChange = 'notChanged',
            record,
            form,
            gridView;


        if (grid) {
            gridView = grid.getView();
            record = gridView.getSelectionModel().getLastSelected();
            form = this.getComServerPreview().down('form');
        } else {
            record = menu.record;
            form = this.getComServerOverviewForm();
        }


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
            var me = this;
            var recordId = record.get('id');
            Ext.Ajax.request({
                url: '/api/mdc/comservers/' + recordId + '/status',
                method: 'PUT',
                jsonData: {active: activeChange},
                success: function () {
                    Ext.ModelManager.getModel('Mdc.model.ComServer').load(recordId, {
                        callback: function (model) {
                            if (grid) {
                                record.set('active', activeChange);
                                record.commit();
                                gridView.refresh();
                                me.getPreviewActionMenu().record = model;
                            } else {
                                menu.record = model;
                            }

                            var msg = activeChange ? Uni.I18n.translate('comserver.changeState.activated', 'MDC', 'activated') :
                                Uni.I18n.translate('comserver.changeState.deactivated', 'MDC', 'deactivated');
                            form.loadRecord(model);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comserver.changeState.msg', 'MDC', 'Communication server') + ' ' + msg);
                        }
                    });
                },
                failure: function(response, opts) {
                    console.log('server-side failure with status code ' + response.status);
                    var json = Ext.decode(response.responseText);
                    if (json && json.errors) {
                        var msg = '';
                        Ext.each(json.errors, function (error) {
                            msg += error["id"] + ': ' + error["msg"];
                        });
                        var title = Uni.I18n.translate('comserver.changeState.'+item.action+'.failed', 'MDC', 'Activation/Deactivation failed');
                        me.getApplication().getController('Uni.controller.Error').showError(title, msg);
                    }
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
                        me.deleteComserver(record);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    deleteComserver: function (record) {
        var me = this,
            page = me.getComServersView(),
            gridToolbarTop = me.getComServerGrid().down('pagingtoolbartop');

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.wasSuccessful()) {
                    gridToolbarTop.totalCount = 0;
                    me.getComServerGrid().getStore().loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServer.deleteSuccess.msg', 'MDC', 'Communication server removed'));
                }
            }
        });
    }
});
