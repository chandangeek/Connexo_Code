Ext.define('Mdc.usagepointmanagement.controller.History', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.MetrologyConfigurationVersion',
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
        {ref: 'addVersionPanel', selector: 'add-metrology-configuration-version'}
    ],

    init: function () {
        this.control({
            'metrology-configuration-history-tab metrology-configuration-history-grid': {
                select: this.selectVersion
            },
            'add-metrology-configuration-version #usage-point-add-button': {
                click: this.addMetrologyConfigurationVersion
            }
        });
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
                        var widget = Ext.widget('usage-point-history-setup', {router: router, mRID: record.get('mRID')});
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
                        var widget = Ext.widget('usage-point-history-setup', {router: router, mRID: record.get('mRID')});
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
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));

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
                me.getApplication().fireEvent('changecontentevent', widget);
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
        
    },

    addMetrologyConfigurationVersion: function(btn){
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionForm = me.getAddVersionPanel().down('#add-version-form'),
            versionRecord = Ext.create('Mdc.usagepointmanagement.model.MetrologyConfigurationVersion'),
            configCombo = versionForm.down('#mc-combo'),
            metrologyConfig = configCombo.findRecordByValue(configCombo.getValue());
        versionForm.down('#form-errors').hide();
        versionForm.getForm().clearInvalid();
        versionForm.updateRecord(versionRecord);
        if(metrologyConfig){
            versionRecord.set('metrologyConfiguration', metrologyConfig.getData());
            me.usagePoint.set('metrologyConfigurationVersion', versionRecord.getData());
            Ext.Ajax.request({
                url: Ext.String.format("/api/mtr/usagepoints/{0}/metrologyconfiguration",me.usagePoint.get('mRID')),
                method: 'PUT',
                jsonData: me.usagePoint.getRecordData(),
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.version.added', 'MDC', "Metrology configuration version added"));
                    router.getRoute('usagepoints/usagepoint/history').forward();
                },
                failure: function (response, request) {
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
            })
        } else {
            versionForm.down('#form-errors').show();
            versionForm.down('#mc-combo').markInvalid(Uni.I18n.translate('readingtypesmanagment.bulk.thisfieldisrequired', 'MDC', 'This field is required'));
        }
    }
});

