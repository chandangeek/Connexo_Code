Ext.define('Dlc.devicelifecycles.controller.DeviceLifeCycles', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.devicelifecycles.view.Setup',
        'Dlc.devicelifecycles.view.Add',
        'Dlc.devicelifecycles.view.Clone'
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
        },
        {
            ref: 'clonePage',
            selector: 'device-life-cycles-clone'
        }
    ],

    init: function () {
        this.control({
            'device-life-cycles-setup device-life-cycles-grid': {
                select: this.showDeviceLifeCyclePreview
            },
            'device-life-cycles-add-form button[action=add]': {
                click: this.createDeviceLifeCycle
            },
            'device-life-cycles-add-form button[action=clone]': {
                click: this.cloneDeviceLifeCycle
            },
            'device-life-cycles-action-menu menuitem[action=remove]': {
                click: this.showRemoveConfirmationPanel
            },
            'device-life-cycles-action-menu menuitem[action=clone]': {
                click: this.moveToClone
            }
        });
    },

    moveToClone: function () {
        var me = this,
            grid = me.getLifeCyclesGrid(),
            record = grid.getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('administration/devicelifecycles/clone').forward({deviceLifeCycleId: record.get('id')});
    },

    showRemoveConfirmationPanel: function () {
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

    showCloneDeviceLifeCycle: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            view;

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                var title = Uni.I18n.translate('general.clone', 'DLC', 'Clone') + " '" + deviceLifeCycleRecord.get('name') + "'";
                me.getApplication().fireEvent('devicelifecyclecloneload', title);
                view = Ext.widget('device-life-cycles-clone', {
                    router: me.getController('Uni.controller.history.Router'),
                    title: title,
                    infoText: Uni.I18n.translatePlural('deviceLifeCycles.clone.templateMsg', deviceLifeCycleRecord.get('name'), 'DLC', "The new device life cycle is based on the '{0}' and will use the same states and transitions.")
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    cloneDeviceLifeCycle: function () {
        var me = this,
            page = me.getClonePage(),
            form = page.down('#device-life-cycles-add-form'),
            formErrorsPanel = form.down('#form-errors'),
            router = me.getController('Uni.controller.history.Router'),
            data = {};

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
            form.getForm().clearInvalid();
        }

        data.name = form.down('#device-life-cycle-name').getValue();
        page.setLoading();
        Ext.Ajax.request({
            url: '/api/dld/devicelifecycles/' + router.arguments.deviceLifeCycleId + '/clone',
            method: 'POST',
            jsonData: data,
            success: function () {
                router.getRoute('administration/devicelifecycles').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycles.clone.successMsg', 'DLC', 'Device life cycle cloned'));
            },
            failure: function (response) {
                page.setLoading(false);
                var json = Ext.decode(response.responseText, true);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                }
            }
        });

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