Ext.define('Mdc.controller.setup.ComServerOverview', {
    extend: 'Mdc.controller.setup.ComServersView',

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
            record.save({
                callback: function (model) {
                    var msg = activeChange ? Uni.I18n.translate('comserver.changeState.activated', 'MDC', 'activated') :
                        Uni.I18n.translate('comserver.changeState.deactivated', 'MDC', 'deactivated');
                    form.loadRecord(model);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comserver.changeState.msg', 'MDC', 'Communication server') + ' ' + msg);
                }
            });
        }
    },

    editComServer: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();

        router.getRoute('administration/comservers/detail/edit').forward({id: id});
    },

    showOverview: function (id) {
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
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServer.deleteSuccess.msg', 'MDC', 'Communication server removed'));
                }
            }
        });
    }
});
