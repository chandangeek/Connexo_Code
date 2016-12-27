Ext.define('Mdc.usagepointmanagement.controller.UsagePointHistory', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.usagepointmanagement.view.history.UsagePointHistory',
        'Mdc.usagepointmanagement.view.history.UsagePointHistoryDevices',
        'Mdc.usagepointmanagement.view.history.AddMetrologyConfigurationVersion'
    ],

    models: [
        'Mdc.usagepointmanagement.model.UsagePointHistoryDevice',
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.MetrologyConfigurationVersion',
        'Mdc.model.MeterActivation'
    ],

    stores: [
        'Mdc.usagepointmanagement.store.UsagePointHistoryDevices',
        'Mdc.usagepointmanagement.store.MetrologyConfigurationVersions',
        'Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'usage-point-history'
        },
        {
            ref: 'devicesPage',
            selector: 'usage-point-history usage-point-history-devices'
        },
        {ref: 'metrologyConfigurationTab', selector: 'metrology-configuration-history-tab'},
        {ref: 'metrologyConfigurationActionMenu', selector: 'metrology-configuration-versions-action-menu'},
        {ref: 'addVersionPanel', selector: 'add-metrology-configuration-version'},
        {ref: 'metrologyConfigGrid', selector: '#metrology-configuration-history-grid-id'}
    ],

    init: function () {
        this.control({
            'usage-point-history-devices usage-point-history-devices-grid': {
                select: this.showDevicePreview
            },
            '#usage-point-history-tab-panel #metrology-configuration-history-grid-id': {
                select: this.selectVersion
            },
            'add-metrology-configuration-version #usage-point-add-edit-button': {
                click: this.addMetrologyConfigurationVersion
            },
            '#metrology-configuration-versions-action-menu-id': {
                click: this.selectAction
            },
            '#usage-point-history-tab-panel': {
                tabChange: this.onTabChange
            }
        });
    },

    showUsagePointHistory: function (id, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        if (!tab) {
            router.getRoute('usagepoints/usagepoint/history').forward({tab: 'devices'});
        } else {
            pageMainContent.setLoading();
            me.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(id, {
                success: function (record) {
                    app.fireEvent('usagePointLoaded', record);
                    var widget = Ext.widget('usage-point-history', {
                        itemId: 'usage-point-history',
                        router: router,
                        usagePointId: record.get('name'),
                        usagePoint: record,
                        activeTab: tab,
                        controller: me
                    });
                    app.fireEvent('changecontentevent', widget);
                    switch (tab) {
                        case 'devices' :
                            me.showDevicesTab(widget.down('#usage-point-devices'));
                            break;
                        case 'metrologyconfigurationversion' :
                            me.showMetrologyConfigurationHistory(widget.down('#usage-point-metrologyconfigurationversion'));
                            break;
                    }
                },
                callback: function () {
                    pageMainContent.setLoading(false);
                }
            });
        }
    },

    onTabChange: function (tabPanel, newCard, oldCard) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routeParams = router.arguments;
        if (newCard.itemId == 'usage-point-devices') {
            routeParams['tab'] = 'devices';
            router.getRoute('usagepoints/usagepoint/history').forward(routeParams)
        } else if (newCard.itemId == 'usage-point-metrologyconfigurationversion') {
            routeParams['tab'] = 'metrologyconfigurationversion';
            router.getRoute('usagepoints/usagepoint/history').forward(routeParams)
        }
    },

    showDevicesTab: function (panel) {
        var me = this,
            store = me.getStore('Mdc.usagepointmanagement.store.UsagePointHistoryDevices'),
            router = me.getController('Uni.controller.history.Router');

        Uni.util.History.suspendEventsForNextCall();
        Uni.util.History.setParsePath(false);
        router.getRoute('usagepoints/usagepoint/history').forward({tab: 'devices'});

        store.getProxy().setExtraParam('usagePointId', router.arguments.usagePointId);
        store.load(function () {
            Ext.suspendLayouts();
            panel.removeAll();
            if (store.getCount()) {
                panel.add({
                    xtype: 'usage-point-history-devices',
                    itemId: 'usage-point-history-devices',
                    router: router
                });
            } else {
                panel.add({
                    xtype: 'form',
                    items: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('usagePoint.history.devices.emptyCmp.title', 'MDC', 'No devices have been linked to this usage point yet')
                    }
                });
            }
            Ext.resumeLayouts(true);
        });
    },

    showDevicePreview: function (selectionModel, record) {
        var me = this;

        Ext.suspendLayouts();
        me.getDevicesPage().down('usage-point-history-devices-preview').setTitle(record.get('name'));
        me.getDevicesPage().down('#usage-point-history-devices-preview-form').loadRecord(record);
        Ext.resumeLayouts(true);
    },

    selectAction: function (menu, item) {
        switch (item.action) {
            case 'remove':
                this.deleteMetrologyConfigurationVersion(menu, item);
                break;
            case 'edit':
                this.editMetrologyConfigurationVersion(menu, item)
        }
    },

    editMetrologyConfigurationVersion: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('usagepoints/usagepoint/history/editmetrologyconfigurationversion').forward({
            usagePointId: menu.usagePoint.get('name'),
            start: menu.record.get('start')
        });
    },

    showMetrologyConfigurationHistory: function (tab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versions = me.getStore('Mdc.usagepointmanagement.store.MetrologyConfigurationVersions'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            usagePoint = tab.usagePoint;
        pageMainContent.setLoading(true);

        versions.getProxy().setExtraParam('usagePointId', usagePoint.get('name'));
        var grid = tab.down('#metrology-configuration-history-grid-id');

        versions.load({
            scope: me,
            callback: function () {
                grid.setVersionCount(versions.getCount());
                pageMainContent.setLoading(false);
            }
        });

    },

    selectVersion: function (selectionModel, record) {
        var me = this,
            page = me.getMetrologyConfigurationTab(),
            actionMenu = me.getMetrologyConfigurationActionMenu(),
            preview = page.down('metrology-configuration-history-preview');

        Ext.suspendLayouts();

        preview.setTitle(Ext.String.htmlEncode(record.get('period')));

        if (actionMenu) {
            actionMenu.setMenuItems(record);
            preview.down('metrology-configuration-versions-action-menu').setMenuItems(record);
        }

        preview.fillReadings(record);
        Ext.resumeLayouts(true);
    },

    showAddVersion: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePoint'),
            availableConfigs = me.getStore('Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations'),
            widget = Ext.widget('add-metrology-configuration-version', {router: router}),

            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        pageMainContent.setLoading(true);

        usagePointModel.load(id, {
            success: function (record) {
                me.usagePoint = record;
                availableConfigs.getProxy().setExtraParam('usagePointId', record.get('name'));
                availableConfigs.load(
                    {
                        scope: me,
                        callback: function (records) {
                            me.getApplication().fireEvent('changecontentevent', widget);
                            if (records.length == 0) {
                                widget.down('#add-version-form #no-mc-available-msg').show();
                                widget.down('#add-version-form #mc-combo').hide();
                                widget.down('#add-version-form #usage-point-add-edit-button').hide()
                            }
                        }
                    }
                )

            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });

    },

    showEditVersion: function (usagePointId, tabname, start) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePoint'),
            versionRecord = me.getModel('Mdc.usagepointmanagement.model.MetrologyConfigurationVersion'),
        //usagePointWithVersionModel = me.getModel('Mdc.usagepointmanagement.model.UsagePointWithVersion'),
            availableConfigs = me.getStore('Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);
        usagePointModel.load(usagePointId, {
            success: function (usagePoint) {
                me.usagePoint = usagePoint;
                availableConfigs.getProxy().setExtraParam('usagePointId', usagePoint.get('name'));
                availableConfigs.load({
                    callback: function () {
                        versionRecord.getProxy().setExtraParam('usagePointId', usagePoint.get('name'));
                        versionRecord.load(start, {
                            success: function (record) {
                                if (record.get('editable') || record.get('current')) {
                                    var widget = Ext.widget('add-metrology-configuration-version', {router: router, edit: true});
                                    me.getApplication().fireEvent('changecontentevent', widget);
                                    widget.loadRecordToForm(record);
                                    me.versionRecord = record;
                                    pageMainContent.setLoading(false);
                                } else {
                                    pageMainContent.setLoading(false);
                                    window.location.replace(router.getRoute('error').buildUrl())
                                }
                            },
                            failure: function () {
                                pageMainContent.setLoading(false);
                                window.location.replace(router.getRoute('error').buildUrl())
                            }
                        });
                    }
                });
            }
        });

    },

    deleteMetrologyConfigurationVersion: function (menu, menuItem) {
        if (menu.usagePoint && menu.record) {
            var me = this,
                url = menu.record.getProxy().url,
                router = me.getController('Uni.controller.history.Router'),
                versionsStore = me.getMetrologyConfigGrid().getStore(),
                doRemove = function () {
                    url = url.replace('{usagePointId}', menu.usagePoint.get('name')) + '/{id}'.replace('{id}', menu.record.get('id'));
                    Ext.Ajax.request({
                        url: url,
                        method: 'DELETE',
                        jsonData: Ext.encode(menu.usagePoint.getData()),
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.version.removed', 'MDC', 'Metrology configuration version removed'));
                            router.getRoute().forward();
                        }
                    });
                };
            Ext.create('Uni.view.window.Confirmation').show({
                title: Uni.I18n.translate('usagePoint.remove.mcversion.confirmation.title', 'MDC', ' Remove \'{0}\' from the usage point', [menu.record.get('name')]),
                msg: Uni.I18n.translate('usagePoint.remove.mcversion.confirmation.msg', 'MDC',
                    'You will not be able to view and use data from reading types specified on this metrology configuration'),
                fn: function (action) {
                    if (action == "confirm") {
                        doRemove();
                    }
                    ;
                }
            });
        }
    },


    addMetrologyConfigurationVersion: function (btn) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionForm = me.getAddVersionPanel().down('#add-version-form'),
            versionRecord = me.versionRecord ? me.versionRecord : Ext.create('Mdc.usagepointmanagement.model.MetrologyConfigurationVersion'),
            configCombo = versionForm.down('#mc-combo');
        var usagePointWithVersionModel = Ext.create('Mdc.usagepointmanagement.model.UsagePointWithVersion'),
            metrologyConfig = configCombo.findRecordByValue(configCombo.getValue());
        versionForm.down('#form-errors').hide();
        versionForm.getForm().clearInvalid();
        versionForm.updateRecord(versionRecord);
        if (versionForm.down('#end-time-date').getValue()['installation-time']) {
            versionRecord.set('end', null);
        }
        if (metrologyConfig) {
            versionRecord.set('metrologyConfiguration', metrologyConfig.getData());
            usagePointWithVersionModel.set(me.usagePoint.getRecordData());
            usagePointWithVersionModel.set('metrologyConfigurationVersion', versionRecord.getData());
            usagePointWithVersionModel.getProxy().setExtraParam('usagePointId', me.usagePoint.get('name'));
            if (btn.action == 'add') {

                usagePointWithVersionModel.save({
                    backUrl: router.getRoute('usagepoints/usagepoint/history').buildUrl(),
                    dontTryAgain: true,
                    success: function () {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.version.added', 'MDC', "Metrology configuration version added"));
                        router.getRoute('usagepoints/usagepoint/history').forward();
                    },
                    failure: function (record, operation) {
                        if (operation.response.status == 400) {
                            if (!Ext.isEmpty(operation.response.responseText)) {
                                var json = Ext.decode(operation.response.responseText, true);
                                if (json && json.errors) {
                                    versionForm.down('#form-errors').show();
                                    versionForm.getForm().markInvalid(json.errors);
                                }
                            }
                        }
                    }
                });
            } else if (btn.action == 'edit') {
                var url = usagePointWithVersionModel.getProxy().url + '/' + router.arguments.start;

                Ext.Ajax.request({
                    url: url,
                    method: 'PUT',
                    backUrl: router.getRoute('usagepoints/usagepoint/history').buildUrl(),
                    dontTryAgain: true,
                    jsonData: Ext.encode(usagePointWithVersionModel.getData()),
                    success: function () {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.version.saved', 'MDC', 'Metrology configuration version saved'));
                        router.getRoute('usagepoints/usagepoint/history').forward();
                    },
                    failure: function (response) {
                        if (response.status == 400) {
                            if (!Ext.isEmpty(response.responseText)) {
                                var json = Ext.decode(response.responseText, true);
                                if (json && json.errors) {
                                    versionForm.down('#form-errors').show();

                                    versionForm.getForm().markInvalid(json.errors);
                                }
                            }
                        }
                    }
                });
            }
        } else {
            versionForm.down('#form-errors').show();
            versionForm.down('#mc-combo').markInvalid(Uni.I18n.translate('readingtypesmanagment.bulk.thisfieldisrequired', 'MDC', 'This field is required'));
        }
    }
});

