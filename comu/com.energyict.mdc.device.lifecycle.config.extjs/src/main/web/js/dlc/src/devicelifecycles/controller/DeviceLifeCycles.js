Ext.define('Dlc.devicelifecycles.controller.DeviceLifeCycles', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.devicelifecycles.view.Setup',
        'Dlc.devicelifecycles.view.Add',
        'Dlc.devicelifecycles.view.Clone',
        'Dlc.devicelifecycles.view.Edit',
        'Dlc.devicelifecycles.view.Overview'
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
        },
        {
            ref: 'editPage',
            selector: 'device-life-cycles-edit'
        },
        {
            ref: 'overviewPage',
            selector: 'device-life-cycles-overview'
        }
    ],

    deviceLifeCycle: null,
    fromOverview: false,

    init: function () {
        this.control({
            'device-life-cycles-setup device-life-cycles-grid': {
                select: this.showDeviceLifeCyclePreview
            },
            'device-life-cycles-add-form button[action=add]': {
                click: this.createDeviceLifeCycle
            },
            'device-life-cycles-add-form button[action=edit]': {
                click: this.createDeviceLifeCycle
            },
            'device-life-cycles-add-form button[action=clone]': {
                click: this.cloneDeviceLifeCycle
            },
            'device-life-cycles-action-menu menuitem[action=remove]': {
                click: this.showRemoveConfirmationPanel
            },
            'device-life-cycles-action-menu menuitem[action=clone]': {
                click: this.moveTo
            },
            'device-life-cycles-action-menu menuitem[action=edit]': {
                click: this.moveTo
            }
        });
    },

    moveTo: function (btn) {
        var me = this,
            record = btn.up('device-life-cycles-action-menu').record,
            router = me.getController('Uni.controller.history.Router'),
            route;

        me.getOverviewPage() ? me.fromOverview = true : me.fromOverview = false;
        route = btn.action == 'clone' ? 'administration/devicelifecycles/clone' : 'administration/devicelifecycles/devicelifecycle/edit';
        router.getRoute(route).forward({deviceLifeCycleId: record.get('id')});
    },

    showRemoveConfirmationPanel: function (btn) {
        var me = this,
            record = btn.up('device-life-cycles-action-menu').record,
            page = Ext.ComponentQuery.query('contentcontainer')[0];

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceLifeCycles.confirmWindow.removeMsg', 'DLC', 'This device life cycle will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'DLC', 'Remove') + " '" + record.get('name') + "'?",
            config: {
                me: me,
                record: record,
                page: page
            },
            fn: me.removeConfirmationPanelHandler
        });
    },

    removeConfirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            router = me.getController('Uni.controller.history.Router'),
            model = conf.config.record,
            widget = conf.config.page;

        if (state === 'confirm') {
            widget.setLoading(Uni.I18n.translate('general.removing', 'DLC', 'Removing...'));
            model.destroy({
                success: function () {
                    widget.setLoading(false);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycles.removeSuccessMsg', 'DLC', 'Device life cycle removed'));
                    router.getRoute('administration/devicelifecycles').forward();
                },
                failure: function () {
                    widget.setLoading(false);
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
            router = me.getController('Uni.controller.history.Router'),
            preview = page.down('device-life-cycles-preview'),
            previewForm = page.down('device-life-cycles-preview-form'),
            deviceTypesList = '';

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        previewForm.down('#used-by').removeAll();
        Ext.Array.each(record.get('deviceTypes'), function (deviceType) {
            var url = router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: deviceType.id});
            deviceTypesList += '- <a href="' + url + '">' + Ext.String.htmlEncode(deviceType.name) + '</a><br/>';
        });
        previewForm.down('#used-by').add({
            xtype: 'displayfield',
            htmlEncode: false,
            value: deviceTypesList
        });
        if (preview.down('device-life-cycles-action-menu')) {
            preview.down('device-life-cycles-action-menu').record = record;
        }
        Ext.resumeLayouts(true);
    },

    showAddDeviceLifeCycle: function () {
        var me = this,
            view = Ext.widget('device-life-cycles-add', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.deviceLifeCycle = null;
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showEditDeviceLifeCycle: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            route;

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                route = me.fromOverview ? 'administration/devicelifecycles/devicelifecycle' : 'administration/devicelifecycles';
                var view = Ext.widget('device-life-cycles-edit', {
                        router: me.getController('Uni.controller.history.Router'),
                        route: route
                    }),
                    form = view.down('device-life-cycles-add-form');

                me.deviceLifeCycle = deviceLifeCycleRecord;
                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
                me.getApplication().fireEvent('deviceLifeCycleEdit', deviceLifeCycleRecord);
                form.setTitle(Uni.I18n.translatePlural('deviceLifeCycles.edit.title', deviceLifeCycleRecord.get('name'), 'DLC', 'Edit \'{0}\''));
                form.loadRecord(deviceLifeCycleRecord);
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    showCloneDeviceLifeCycle: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            view;

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                var title = Uni.I18n.translate('general.clone', 'DLC', 'Clone') + " '" + deviceLifeCycleRecord.get('name') + "'",
                    route;

                me.getApplication().fireEvent('devicelifecyclecloneload', title);
                route = me.fromOverview ? 'administration/devicelifecycles/devicelifecycle' : 'administration/devicelifecycles';
                view = Ext.widget('device-life-cycles-clone', {
                    router: me.getController('Uni.controller.history.Router'),
                    title: title,
                    infoText: Uni.I18n.translatePlural('deviceLifeCycles.clone.templateMsg', deviceLifeCycleRecord.get('name'), 'DLC', "The new device life cycle is based on the '{0}' and will use the same states and transitions."),
                    route: route
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
            data = {},
            route;

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
                route = me.fromOverview ? 'administration/devicelifecycles/devicelifecycle' : 'administration/devicelifecycles';
                router.getRoute(route).forward();
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

    createDeviceLifeCycle: function (btn) {
        var me = this,
            page = me.deviceLifeCycle ? me.getEditPage() : me.getAddPage(),
            form = page.down('device-life-cycles-add-form'),
            formErrorsPanel = form.down('#form-errors'),
            record = me.deviceLifeCycle || Ext.create('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            route;

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
            form.getForm().clearInvalid();
        }
        record.set('name', form.down('#device-life-cycle-name').getValue());
        page.setLoading();
        record.save({
            success: function () {
                route = me.fromOverview ? 'administration/devicelifecycles/devicelifecycle' : 'administration/devicelifecycles';
                me.getController('Uni.controller.history.Router').getRoute(route).forward();
                if (btn.action === 'edit') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycles.edit.successMsg', 'DLC', 'Device life cycle saved'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycles.add.successMsg', 'DLC', 'Device life cycle added'));
                }
            },
            failure: function (record, operation) {
                page.setLoading(false);
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                }
            }
        });
    },

    showDeviceLifeCycleOverview: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            deviceTypesList = '';

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                var view = Ext.widget('device-life-cycles-overview', {
                        router: router
                    }),
                    form = view.down('device-life-cycles-preview-form');

                me.deviceLifeCycle = deviceLifeCycleRecord;
                form.loadRecord(deviceLifeCycleRecord);
                view.down('#device-life-cycle-link').setText(deviceLifeCycleRecord.get('name'));
                Ext.Array.each(deviceLifeCycleRecord.get('deviceTypes'), function (deviceType) {
                    var url = router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: deviceType.id});
                    deviceTypesList += '- <a href="' + url + '">' + Ext.String.htmlEncode(deviceType.name) + '</a><br/>';
                });
                form.down('#used-by').add({
                    xtype: 'displayfield',
                    value: deviceTypesList,
                    htmlEncode: false
                });

                if (view.down('device-life-cycles-action-menu')) {
                    view.down('device-life-cycles-action-menu').record = deviceLifeCycleRecord;
                }

                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    }
});