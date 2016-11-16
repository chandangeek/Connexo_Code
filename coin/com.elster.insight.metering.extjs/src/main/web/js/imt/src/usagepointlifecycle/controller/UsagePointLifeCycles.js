Ext.define('Imt.usagepointlifecycle.controller.UsagePointLifeCycles', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.usagepointlifecycle.view.Setup',
        'Imt.usagepointlifecycle.view.Clone',
        'Imt.usagepointlifecycle.view.Edit',
        'Imt.usagepointlifecycle.view.Overview'
    ],

    stores: [
        'Imt.usagepointlifecycle.store.UsagePointLifeCycles'
    ],

    models: [
        'Imt.usagepointlifecycle.model.UsagePointLifeCycle'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'usagepoint-life-cycles-setup'
        },
        {
            ref: 'lifeCyclesGrid',
            selector: 'usagepoint-life-cycles-grid'
        },
        {
            ref: 'clonePage',
            selector: 'usagepoint-life-cycles-clone'
        },
        {
            ref: 'editPage',
            selector: 'usagepoint-life-cycles-edit'
        },
        {
            ref: 'overviewPage',
            selector: 'usagepoint-life-cycles-overview'
        }
    ],

    usagePointLifeCycle: null,
    fromOverview: false,

    init: function () {
        this.control({
            'usagepoint-life-cycles-setup usagepoint-life-cycles-grid': {
                select: this.showUsagePointLifeCyclePreview
            },
            'usagepoint-life-cycles-add-form button[action=add]': {
                click: this.createUsagePointLifeCycle
            },
            'usagepoint-life-cycles-add-form button[action=edit]': {
                click: this.createUsagePointLifeCycle
            },
            'usagepoint-life-cycles-add-form button[action=clone]': {
                click: this.cloneUsagePointLifeCycle
            },
            'usagepoint-life-cycles-action-menu menuitem[action=remove]': {
                click: this.showRemoveConfirmationPanel
            },
            'usagepoint-life-cycles-action-menu menuitem[action=clone]': {
                click: this.moveTo
            },
            'usagepoint-life-cycles-action-menu menuitem[action=edit]': {
                click: this.moveTo
            },
            'usagepoint-life-cycles-action-menu menuitem[action=setAsDefault]': {
                click: this.setAsDefault
            }
        });
    },

    moveTo: function (btn) {
        var me = this,
            record = btn.up('usagepoint-life-cycles-action-menu').record,
            router = me.getController('Uni.controller.history.Router'),
            route;

        me.getOverviewPage() ? me.fromOverview = true : me.fromOverview = false;
        route = btn.action == 'clone' ? 'administration/usagepointlifecycles/clone' : 'administration/usagepointlifecycles/usagepointlifecycle/edit';
        router.getRoute(route).forward({usagePointLifeCycleId: record.get('id')});
    },

    showRemoveConfirmationPanel: function (btn) {
        var me = this,
            record = btn.up('usagepoint-life-cycles-action-menu').record,
            page = Ext.ComponentQuery.query('contentcontainer')[0];

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('usagePointLifeCycles.confirmWindow.removeMsg', 'IMT', 'This usage point life cycle will no longer be available.'),
            title: Uni.I18n.translate('usagePointLifeCycles.remove.title', 'IMT', "Remove '{0}'?",[record.get('name')]),
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
            widget.setLoading(Uni.I18n.translate('general.removing', 'IMT', 'Removing...'));
            model.destroy({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycles.removeSuccessMsg', 'IMT', 'Usage point life cycle removed'));
                    router.getRoute('administration/usagepointlifecycles').forward();
                },
                callback: function () {
                    widget.setLoading(false);
                }
            });
        }
    },

    showUsagePointLifeCycles: function () {
        var me = this,
            view = Ext.widget('usagepoint-life-cycles-setup', {
                router: me.getController('Uni.controller.history.Router')
            }),
            store = me.getStore('Imt.usagepointlifecycle.store.UsagePointLifeCycles');

        me.fromOverview = false;
        store.load();
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showUsagePointLifeCyclePreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),            
            preview = page.down('usagepoint-life-cycles-preview');            

        Ext.suspendLayouts();
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        page.down('usagepoint-life-cycles-preview-form').loadRecord(record);
        me.fillStates(record, page.down('usagepoint-life-cycles-preview-form'));
        if (preview.down('usagepoint-life-cycles-action-menu')) {
            preview.down('usagepoint-life-cycles-action-menu').record = record;
            me.updateActionMenu(record);
        }
        Ext.resumeLayouts(true);
    },

    showAddUsagePointLifeCycle: function () {
        var me = this,
            view = Ext.widget('usagepoint-life-cycles-edit', {
                router: me.getController('Uni.controller.history.Router'),
                route: 'administration/usagepointlifecycles'
            });

        me.usagePointLifeCycle = null;
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showEditUsagePointLifeCycle: function (usagePointLifeCycleId) {
        var me = this,
            usagePointLifeCycleModel = me.getModel('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            route;

        usagePointLifeCycleModel.load(usagePointLifeCycleId, {
            success: function (usagePointLifeCycleRecord) {
                route = me.fromOverview ? 'administration/usagepointlifecycles/usagepointlifecycle' : 'administration/usagepointlifecycles';
                var view = Ext.widget('usagepoint-life-cycles-edit', {
                        router: me.getController('Uni.controller.history.Router'),
                        route: route,
                        isEdit: true
                    }),
                    form = view.down('usagepoint-life-cycles-add-form');

                me.usagePointLifeCycle = usagePointLifeCycleRecord;
                me.getApplication().fireEvent('usagepointlifecycleload', usagePointLifeCycleRecord);
                me.getApplication().fireEvent('usagePointLifeCycleEdit', usagePointLifeCycleRecord);
                form.setTitle(Uni.I18n.translate('usagePointLifeCycles.edit.title', 'IMT', "Edit '{0}'", usagePointLifeCycleRecord.get('name'), false));
                form.loadRecord(usagePointLifeCycleRecord);
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    showCloneUsagePointLifeCycle: function (usagePointLifeCycleId) {
        var me = this,
            usagePointLifeCycleModel = me.getModel('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            view;

        usagePointLifeCycleModel.load(usagePointLifeCycleId, {
            success: function (usagePointLifeCycleRecord) {
                var title = Uni.I18n.translate('usagePointLifeCycles.clone.title', 'IMT', "Clone '{0}'", usagePointLifeCycleRecord.get('name'), false),
                    route;

                me.getApplication().fireEvent('usagepointlifecyclecloneload', usagePointLifeCycleRecord.get('name'));
                route = me.fromOverview ? 'administration/usagepointlifecycles/usagepointlifecycle' : 'administration/usagepointlifecycles';
                view = Ext.widget('usagepoint-life-cycles-clone', {
                    router: me.getController('Uni.controller.history.Router'),
                    title: title,
                    infoText: Uni.I18n.translate('usagePointLifeCycles.clone.templateMsg', 'IMT',
                        "The new usage point life cycle is based on '{0}' and will use the same states and transitions.",
                        [usagePointLifeCycleRecord.get('name')]),
                    route: route
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    cloneUsagePointLifeCycle: function () {
        var me = this,
            page = me.getClonePage(),
            form = page.down('usagepoint-life-cycles-add-form'),
            formErrorsPanel = form.down('#form-errors'),
            router = me.getController('Uni.controller.history.Router'),
            data = {},
            route;

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
            form.getForm().clearInvalid();
        }

        data.name = form.down('#usagepoint-life-cycle-name').getValue();
        page.setLoading();
        Ext.Ajax.request({
            url: '/api/upl/lifecycle/' + router.arguments.usagePointLifeCycleId + '/clone',
            method: 'POST',
            jsonData: data,
            success: function () {
                route = me.fromOverview ? 'administration/usagepointlifecycles/usagepointlifecycle' : 'administration/usagepointlifecycles';
                router.getRoute(route).forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycles.clone.successMsg', 'IMT', 'Usage point life cycle cloned'));
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

    createUsagePointLifeCycle: function (btn) {
        var me = this,
            page = me.getEditPage(),
            form = page.down('usagepoint-life-cycles-add-form'),
            formErrorsPanel = form.down('#form-errors'),
            record = me.usagePointLifeCycle || Ext.create('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            backUrl = me.fromOverview
                ? router.getRoute('administration/usagepointlifecycles/usagepointlifecycle').buildUrl()
                : router.getRoute('administration/usagepointlifecycles').buildUrl();

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
            form.getForm().clearInvalid();
        }
        record.set('name', form.down('#usagepoint-life-cycle-name').getValue());
        page.setLoading();
        record.save({
            backUrl: backUrl,
            success: function () {
                window.location.href = backUrl;
                if (btn.action === 'edit') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycles.edit.successMsg', 'IMT', 'Usage point life cycle saved'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycles.add.successMsg', 'IMT', 'Usage point life cycle added'));
                }
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    showUsagePointLifeCycleOverview: function (usagePointLifeCycleId) {
        var me = this,
            usagePointLifeCycleModel = me.getModel('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            router = me.getController('Uni.controller.history.Router');

        usagePointLifeCycleModel.load(usagePointLifeCycleId, {
            success: function (usagePointLifeCycleRecord) {
                var view = Ext.widget('usagepoint-life-cycles-overview', {
                        router: router
                    }),
                    form = view.down('usagepoint-life-cycles-preview-form');

                me.usagePointLifeCycle = usagePointLifeCycleRecord;
                form.loadRecord(usagePointLifeCycleRecord);
                view.down('#usagepoint-life-cycle-link').setText(usagePointLifeCycleRecord.get('name'));
                me.fillStates(usagePointLifeCycleRecord, form);
                if (view.down('usagepoint-life-cycles-action-menu')) {
                    view.down('usagepoint-life-cycles-action-menu').record = usagePointLifeCycleRecord;
                    me.updateActionMenu(usagePointLifeCycleRecord);
                }

                me.getApplication().fireEvent('usagepointlifecycleload', usagePointLifeCycleRecord);
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    setAsDefault: function (btn) {
        var me = this,
            record = btn.up('usagepoint-life-cycles-action-menu').record,
            page = Ext.ComponentQuery.query('contentcontainer')[0],
            router = me.getController('Uni.controller.history.Router'),
            route;

        me.getOverviewPage() ? me.fromOverview = true : me.fromOverview = false;
        page.setLoading();
        Ext.Ajax.request({
            url: '/api/upl/lifecycle/' + record.get('id') + '/default',
            method: 'PUT',
            jsonData: record.getData(),
            success: function () {
                route = me.fromOverview ? 'administration/usagepointlifecycles/usagepointlifecycle' : 'administration/usagepointlifecycles';
                router.getRoute(route).forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycles.acknowledgment.setAsDefault', 'IMT', 'Usage point life cycle set as default'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    updateActionMenu: function (record) {
        Ext.Array.each(Ext.ComponentQuery.query('#set-as-default'), function (menuItem) {
            menuItem.setVisible(!record.get('isDefault'));
        });
    },

    fillStates: function (record, previewForm) {
        var statesList = '';
        previewForm.down('#states-container').removeAll();
        Ext.Array.each(record.get('states'), function (state) {
            statesList += Ext.String.htmlEncode(state.name) + '<br/>';
        });
        previewForm.down('#states-container').add({
            xtype: 'displayfield',
            htmlEncode: false,
            value: statesList
        });
    }
});