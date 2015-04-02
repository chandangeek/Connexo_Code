Ext.define('Dlc.devicelifecycles.controller.DeviceLifeCycles', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.devicelifecycles.view.Setup',
        'Dlc.devicelifecycles.view.Add'
    ],

    stores: [
        'Dlc.devicelifecycles.store.DeviceLifeCycles'
    ],

    models: [
        'Dlc.devicelifecycles.model.DeviceLifeCycle'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-life-cycles-setup'
        },
        {
            ref: 'lifeCyclesGrid',
            selector: 'device-life-cycles-grid'
        },
        {
            ref: 'addPage',
            selector: 'device-life-cycles-add'
        }
    ],

    init: function () {
        this.control({
            'device-life-cycles-setup device-life-cycles-grid': {
                select: this.showDeviceLifeCyclePreview
            },
            'device-life-cycles-add #add-button': {
                click: this.createDeviceLifeCycle
            },
            'device-life-cycles-action-menu menuitem[action=remove]': {
                click: this.showRemoveConfirmationPanel
            }
        });
    },

    showRemoveConfirmationPanel: function() {
        var me = this,
            grid = me.getLifeCyclesGrid(),
            record = grid.getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceLifeCycles.confirmWindow.removeMsg', 'DLC', 'This device life cycle will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'DLC', 'Remove') + " '" + record.get('name') + "'?",
            config: {
                me: me
            },
            fn: me.removeConfirmationPanelHandler
        });
    },

    removeConfirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            grid = me.getLifeCyclesGrid(),
            router = me.getController('Uni.controller.history.Router'),
            model = grid.getSelectionModel().getLastSelected(),
            widget = me.getPage();

        if (state === 'confirm') {
            widget.setLoading(Uni.I18n.translate('general.removing', 'DLC', 'Removing...'));
            model.destroy({
                success: function () {
                    widget.setLoading(false);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycles.removeSuccessMsg', 'DLC', 'Device life cycle removed'));
                    router.getRoute().forward(null, router.queryParams);
                }
            });
        }
    },

    showDeviceLifeCycles: function () {
        var me = this,
            view = Ext.widget('device-life-cycles-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showDeviceLifeCyclePreview: function (selectionModel, record, index) {
        var me = this,
            page = me.getPage(),
            preview = page.down('device-life-cycles-preview'),
            previewForm = page.down('device-life-cycles-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showAddDeviceLifeCycle: function () {
        var me = this,
            view = Ext.widget('device-life-cycles-add', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    createDeviceLifeCycle: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#device-life-cycles-add-form'),
            formErrorsPanel = form.down('#form-errors'),
            record = Ext.create('Dlc.devicelifecycles.model.DeviceLifeCycle');

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
            form.getForm().clearInvalid();
        }
        record.set('name', form.down('#device-life-cycle-name').getValue());
        page.setLoading();
        record.save({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute('administration/devicelifecycles').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycles.add.successMsg', 'DLC', 'Device life cycle added'));
            },
            failure: function (record, operation) {
                page.setLoading(false);
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                }
            }
        })
    }
});