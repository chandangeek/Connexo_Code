Ext.define('Mdc.usagepointmanagement.controller.History', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.MetrologyConfigurationVersion',
        'Mdc.usagepointmanagement.model.UsagePointWithVersion',
        'Ext.container.Container'
    ],
    stores: [
        'Mdc.usagepointmanagement.store.MetrologyConfigurationVersions'
    ],
    views: [
        'Mdc.usagepointmanagement.view.history.Setup',
        'Mdc.usagepointmanagement.view.history.AddMetrologyConfigurationVersion'
    ],
    refs: [
        {ref: 'metrologyConfigurationTab', selector: 'metrology-configuration-history-tab'},
        {ref: 'metrologyConfigurationActionMenu', selector: 'metrology-configuration-versions-action-menu'},
        {ref: 'addVersionPanel', selector: 'add-metrology-configuration-version'},
        {ref: 'metrologyConfigGrid', selector: '#metrology-configuration-history-grid-id'}
    ],

    init: function () {
        this.control({
            'metrology-configuration-history-tab metrology-configuration-history-grid': {
                select: this.selectVersion
            },
            'add-metrology-configuration-version #usage-point-add-edit-button': {
                click: this.addMetrologyConfigurationVersion
            },
            '#metrology-configuration-versions-action-menu-id': {
                click: this.selectAction
            }
        });
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
        router.getRoute('usagepoints/usagepoint/history/editmetrologyconfigurationversion').forward({mRID: menu.usagePoint.get('mRID'), id: menu.record.get('id')});
    },

    showMetrologyConfigurationHistory: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePoint'),
            versions = me.getStore('Mdc.usagepointmanagement.store.MetrologyConfigurationVersions'),

            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);

        usagePointModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);


                versions.getProxy().setUrl(record.get('mRID'));
                switch (router.queryParams.historyTab) {
                    default:
                    {
                        var widget = Ext.widget('usage-point-history-setup', {router: router, usagePoint: record});
                        var grid = widget.down('metrology-configuration-history-grid');

                        versions.load({
                            callback: function () {
                                me.getApplication().fireEvent('changecontentevent', widget);
                                grid.setVersionCount(versions.getCount());
                                pageMainContent.setLoading(false);
                            }
                        });
                    }
                        break;
                    case 'meterActivation':
                    {
                        //TODO: should be changed in another story
                        var widget = Ext.widget('usage-point-history-setup', {router: router, usagePoint: record});
                        var grid = widget.down('metrology-configuration-history-grid');

                        versions.load({
                            callback: function () {
                                me.getApplication().fireEvent('changecontentevent', widget);
                                grid.setVersionCount(versions.getCount())
                                pageMainContent.setLoading(false);
                            }
                        });
                    }
                        break;

                }
            },
            failure: function () {
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
                availableConfigs.getProxy().setUrl(record.get('mRID'));
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

    showEditVersion: function (mRID, id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePoint'),
            versionRecord = me.getModel('Mdc.usagepointmanagement.model.MetrologyConfigurationVersion'),
            usagePointWithVersionModel = me.getModel('Mdc.usagepointmanagement.model.UsagePointWithVersion'),
            availableConfigs = me.getStore('Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations'),
            widget = Ext.widget('add-metrology-configuration-version', {router: router, edit: true}),

            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        pageMainContent.setLoading(true);

        usagePointModel.load(mRID, {
            success: function (usagePoint) {
                me.usagePoint = usagePoint;
                versionRecord.getProxy().setUrl(usagePoint.get('mRID'));
                versionRecord.load(id, {
                    success: function (record) {
                        if (record.get('editable') || record.get('current')) {
                            availableConfigs.getProxy().setUrl(usagePoint.get('mRID'));
                            availableConfigs.load({
                                callback: function () {
                                    me.getApplication().fireEvent('changecontentevent', widget);
                                    widget.loadRecordToForm(record);
                                    me.versionRecord = record;
                                    pageMainContent.setLoading(false);
                                }
                            });
                        } else {
                            pageMainContent.setLoading(false);
                            window.location.replace(router.getRoute('error').buildUrl())
                        }
                    }
                });
            }
        });

    },

    deleteMetrologyConfigurationVersion: function (menu, menuItem) {
        if (menu.usagePoint && menu.record) {
            var me = this,
                url = menu.record.getProxy().urlTpl,
                router = me.getController('Uni.controller.history.Router'),
                versionsStore = me.getMetrologyConfigGrid().getStore(),
                doRemove = function () {
                    url = url.replace('{mRID}', menu.usagePoint.get('mRID')) + '/{id}'.replace('{id}', menu.record.get('id'));
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
                        doRemove()
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
            usagePointWithVersionModel.getProxy().setUrl(me.usagePoint.get('mRID'));
            if (btn.action == 'add') {

                usagePointWithVersionModel.save({
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
                var url = usagePointWithVersionModel.getProxy().url + '/' + versionRecord.get('id');

                Ext.Ajax.request({
                    url: url,
                    method: 'PUT',
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
})
;

