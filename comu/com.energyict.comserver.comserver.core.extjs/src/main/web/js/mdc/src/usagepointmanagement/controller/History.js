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
                        availableConfigs.getProxy().setUrl(usagePoint.get('mRID'));
                        availableConfigs.load({
                            callback: function () {
                                me.getApplication().fireEvent('changecontentevent', widget);
                                widget.loadRecordToForm(record);
                                console.log(record);
                                me.versionRecord = record;
                                pageMainContent.setLoading(false);
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
                url = menu.record.getProxy().urlTpl,
                versionsStore = me.getMetrologyConfigGrid().getStore(),
                doRemove = function () {
                    url = url.replace('{mRID}', menu.usagePoint.get('mRID')) + '/{id}'.replace('{id}', menu.record.get('id'));
                    Ext.Ajax.request({
                        url: url,
                        method: 'DELETE',
                        jsonData: Ext.encode(menu.usagePoint.getData()),
                        success: function () {
                            versionsStore.load();
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePoint.remove.mcversion.acknowledge', 'MDC', 'Metrology configuration version removed'));
                        }
                    });
                };
            Ext.create('Uni.view.window.Confirmation').show({
                title: Uni.I18n.translate('usagePoint.remove.mcversion.confirmation.title', 'MDC', ' Remove \'{0}\' from the usage point', [menu.record.get('name')]),
                msg: Uni.I18n.translate('usagePoint.remove.mcversion.confirmation.msg', 'MDC',
                    'You will not be able to view and use data from reading types specified on this metrology configuration'),
                fn: doRemove
            });
        }
    },


    addMetrologyConfigurationVersion: function (btn) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionForm = me.getAddVersionPanel().down('#add-version-form'),
            versionRecord = me.versionRecord ? me.versionRecord : Ext.create('Mdc.usagepointmanagement.model.MetrologyConfigurationVersion'),
            configCombo = versionForm.down('#mc-combo'),
            usagePointWithVersionModel = me.versionRecord
                ? me.getModel('Mdc.usagepointmanagement.model.UsagePointWithVersion')
                : Ext.create('Mdc.usagepointmanagement.model.UsagePointWithVersion'),
            metrologyConfig = configCombo.findRecordByValue(configCombo.getValue());
        //TODO: make PUT method for edit
        // console.log(versionRecord);
        // usagePointWithVersionModel.phantom = false;
        versionForm.down('#form-errors').hide();
        versionForm.getForm().clearInvalid();
        versionForm.updateRecord(versionRecord);
        if (metrologyConfig) {
            versionRecord.set('metrologyConfiguration', metrologyConfig.getData());
            // me.usagePoint.set('metrologyConfigurationVersion', versionRecord.getData());
            usagePointWithVersionModel.set(me.usagePoint.getRecordData());
            usagePointWithVersionModel.set('metrologyConfigurationVersion', versionRecord.getData());
            usagePointWithVersionModel.getProxy().setUrl(me.usagePoint.get('mRID'));
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
            // Ext.Ajax.request({
            //     url: Ext.String.format("/api/mtr/usagepoints/{0}/metrologyconfigurationversion",me.usagePoint.get('mRID')),
            //     method: 'POST',
            //     jsonData: me.usagePoint.getRecordData(),
            //     success: function () {
            //         me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.version.added', 'MDC', "Metrology configuration version added"));
            //         router.getRoute('usagepoints/usagepoint/history').forward();
            //     },
            //     failure: function (response, request) {
            //         if (response.status == 400) {
            //             if (!Ext.isEmpty(response.responseText)) {
            //                 var json = Ext.decode(response.responseText, true);
            //                 if (json && json.errors) {
            //                     versionForm.down('#form-errors').show();
            //                     versionForm.getForm().markInvalid(json.errors);
            //                 }
            //             }
            //         }
            //     }
            // })
        } else {
            versionForm.down('#form-errors').show();
            versionForm.down('#mc-combo').markInvalid(Uni.I18n.translate('readingtypesmanagment.bulk.thisfieldisrequired', 'MDC', 'This field is required'));
        }
    }
})
;

