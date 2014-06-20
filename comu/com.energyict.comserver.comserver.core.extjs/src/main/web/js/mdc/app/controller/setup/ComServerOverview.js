Ext.define('Mdc.controller.setup.ComServerOverview', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.ComServer'
    ],

    views: [
        'Mdc.view.setup.comserver.ComServerOverview'
    ],

    stores: [
        'ComServers'
    ],

    refs: [
        {
            ref: 'comServerOverview',
            selector: 'comServerOverview'
        },
        {
            selector: '#comServerOverviewForm',
            ref: 'comServerOverviewForm'
        }
    ],

    init: function () {
        this.control({
            '#comserverOverviewMenu': {
                show: this.configureMenu,
                click: this.chooseAction
            }
        })
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
            form = this.getComServerOverviewForm();

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
            delete record.data.comportslink;
            record.save({
                callback: function (model) {
                    form.loadRecord(model);
                }
            });
        }
    },

    showOverview: function (id) {
        console.log(id);
        var me = this,
            widget = Ext.widget('comServerOverview'),
            model = this.getModel('Mdc.model.ComServer');

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        model.load(id, {
            success: function (record) {
                var form = widget.down('form');
                form.loadRecord(record);
                form.up('container').down('container').down('button').menu.record = record;
                me.getApplication().fireEvent('comServerOverviewLoad', record);
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    },

    showDeleteConfirmation: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comServer.deleteConfirmation.msg', 'MDC', 'This communication server will disappear from the list.'),
            title: Ext.String.format(Uni.I18n.translate('comServer.deleteConfirmation.title', 'MDC', 'Delete communication server "{0}"?'), record.get('name')),
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
            page = me.getComServerOverview();
        page.setLoading('Removing...');
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.response.status == 204) {
                    var router = me.getController('Uni.controller.history.Router');
                    router.getRoute('administration/comservers').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServer.deleteSuccess.msg', 'MDC', 'Communication server has been deleted'));
                }
            }
        });
    }
});
