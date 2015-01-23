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
        },
        {
            ref: 'comServerGrid',
            selector: 'comServersSetup comServersGrid'
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

    editComServer: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();

        router.getRoute('administration/comservers/detail/edit').forward({id: id});
    },

    showOverview: function (id) {
        var me = this,
            model = this.getModel('Mdc.model.ComServer'),
            widget = Ext.widget('comServerOverview', {
                serverId: id
            });
        widget.setLoading(true);
        model.load(id, {
            success: function (record) {
                var form = widget.down('form')
                    ;
                me.getApplication().fireEvent('changecontentevent', widget);
                form.loadRecord(record);
                form.up('container').down('container').down('button').menu.record = record;
                widget.down('comserversidemenu #comserverLink').setText(record.get('name'));
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
