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
        'Mdc.usagepointmanagement.view.AddUsagePoint',
        'Mdc.usagepointmanagement.view.Setup'
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
                var widget = Ext.widget('usage-point-management-setup', {router: router, usagePointId: record.get('name')});
                widget.down('usagePointAttributesFormMain').loadRecord(record);
                widget.down('metrology-configuration').loadRecord(record);
                serviceCategories.load({
                    callback: function () {
                        me.getApplication().fireEvent('changecontentevent', widget);
                        pageMainContent.setLoading(false);
                    }
                });
            },
            failure: function () {
                pageMainContent.setLoading(false);
            }
        });
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
                    usagePointId: record.get('name')
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
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.savedxx', 'MDC', 'Usage point saved'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointManagement.addedxx', 'MDC', 'Usage point added'));
                }
                router.getRoute('usagepoints/usagepoint').forward({usagePointId: encodeURIComponent(record.get('name'))});
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

