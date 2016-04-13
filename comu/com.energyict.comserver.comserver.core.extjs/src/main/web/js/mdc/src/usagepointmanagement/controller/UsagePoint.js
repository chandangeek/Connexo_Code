Ext.define('Mdc.usagepointmanagement.controller.UsagePoint', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Ext.container.Container'
    ],
    stores: [
        'Mdc.usagepointmanagement.store.MeterActivations',
        'Mdc.usagepointmanagement.store.ServiceCategories'
    ],
    views: [
        'Mdc.usagepointmanagement.view.Setup',
        'Mdc.usagepointmanagement.view.AddUsagePoint'
    ],
    refs: [
        {ref: 'metrologyConfiguration', selector: 'metrology-configuration'},
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'attributesPanel', selector: '#usage-point-attributes-panel'},
        {ref: 'usagePointTechnicalAttributesDeviceLink', selector: '#usagePointTechnicalAttributesDeviceLink'},
        {ref: 'usagePointTechnicalAttributesDeviceDates', selector: '#usagePointTechnicalAttributesDeviceDates'},
        {ref: 'usagepointActionsMenu', selector: 'usage-point-management-setup #usagePointActionMenu'},
        {ref: 'addUsagePointPanel', selector: 'add-usage-point-setup'}
    ],

    init: function () {
        this.control({
            'add-usage-point-form #usagePointAddButton': {
                click: this.saveUsagePoint
            },
            'edit-usage-point-form #usagePointSaveButton': {
                click: this.saveUsagePoint
            }
        });
    },

    showUsagePoint: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePoint'),
            serviceCategories = me.getStore('Mdc.usagepointmanagement.store.ServiceCategories'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);

        usagePointModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);
                var widget = Ext.widget('usage-point-management-setup', {router: router, mRID: record.get('mRID')});
                widget.down('usagePointAttributesFormMain').loadRecord(record);
                serviceCategories.load({
                    callback: function () {
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.loadMeterActivations(id);
                    }
                });
            },
            failure: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    loadMeterActivations: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            store = me.getStore('Mdc.usagepointmanagement.store.MeterActivations'),
            metrologyConfiguration = me.getMetrologyConfiguration();
        store.getProxy().setExtraParam('usagePointId', id);
        Ext.suspendLayouts();
        store.load({
            callback: function () {
                store.each(function (item) {
                    if (!item.get('end')) {
                        metrologyConfiguration.down('#metrologyLinkedDevice').removeAll();
                        metrologyConfiguration.down('#metrologyLinkedDevice').add(
                            {
                                xtype: 'component',
                                cls: 'x-form-display-field',
                                autoEl: {
                                    tag: 'a',
                                    href: router.getRoute('devices/device').buildUrl({mRID: item.get('meter').mRID}),
                                    html: item.get('meter').mRID,
                                    itemId: 'up-device-link'
                                }
                            },
                            {
                                xtype: 'displayfield',
                                value: Ext.String.format(Uni.I18n.translate('usagePointManagement.fromDate', 'MDC', "from '{0}'")
                                    , Uni.DateTime.formatDateTimeShort(new Date(item.get('start'))))
                            }
                        );
                    } else {
                        metrologyConfiguration.down('#metrologyHistory').show();
                        metrologyConfiguration.down('#metrologySeparator').show();
                        metrologyConfiguration.down('#metrologyHistory').add(0,

                            {
                                xtype: 'component',
                                cls: 'x-form-display-field',
                                autoEl: {
                                    tag: 'a',
                                    href: router.getRoute('devices/device').buildUrl({mRID: item.get('meter').mRID}),
                                    html: item.get('meter').mRID
                                }
                            },
                            {
                                xtype: 'displayfield',
                                value: Ext.String.format(Uni.I18n.translate('usagePointManagement.fromToDate', 'MDC', "from {0} to {1}")
                                    , Uni.DateTime.formatDateTimeShort(new Date(item.get('start')))
                                    , Uni.DateTime.formatDateTimeShort(new Date(item.get('end'))))
                            }
                        );
                    }
                });
                pageMainContent.setLoading(false);
            }
        });
        Ext.resumeLayouts(true);
    },

    showAddUsagePoint: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        me.usagePoint = null;
        pageMainContent.setLoading(true);
        var widget = Ext.widget('add-usage-point-setup', {router: router, edit: false});
        me.getApplication().fireEvent('changecontentevent', widget);
        pageMainContent.setLoading(false);
    },

    showEditUsagePoint: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePoint'),
            serviceCategories = me.getStore('Mdc.usagepointmanagement.store.ServiceCategories'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);

        usagePointModel.load(id, {
            success: function (record) {
                me.usagePoint = record;
                me.getApplication().fireEvent('usagePointLoaded', record);
                me.getApplication().fireEvent('editUsagePointLoaded', record);
                var widget = Ext.widget('add-usage-point-setup', {
                    router: router,
                    edit: true,
                    mRID: record.get('mRID')
                });
                widget.down('#add-edit-form').loadRecord(record);
                serviceCategories.load({
                    callback: function () {
                        me.getApplication().fireEvent('changecontentevent', widget);
                        pageMainContent.setLoading(false);
                    }
                });
            }
        });
    },

    saveUsagePoint: function (btn) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            isEdit = btn.action == 'save',
            usagePointModel = me.usagePoint || Ext.create('Mdc.usagepointmanagement.model.UsagePoint');

        me.getAddUsagePointPanel().down('#add-edit-form').updateRecord(usagePointModel);
        me.getAddUsagePointPanel().down('#add-edit-form').getForm().clearInvalid();
        usagePointModel.save({
            backUrl: isEdit ? router.getRoute('usagepoints/usagepoint').buildUrl() : null,
            success: function (record) {
                if (isEdit) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.saved', 'MDC', "Usage point '{0}' saved.", record.get('mRID')));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.added', 'MDC', "Usage point '{0}' added.", record.get('mRID')));
                }
                router.getRoute('usagepoints/usagepoint').forward({usagePointId: record.get('mRID')});
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            me.getAddUsagePointPanel().down('uni-form-error-message').show();
                            me.getAddUsagePointPanel().down('#add-edit-form').getForm().markInvalid(json.errors);
                        }
                    }
                }
            }
        })
    }
});

