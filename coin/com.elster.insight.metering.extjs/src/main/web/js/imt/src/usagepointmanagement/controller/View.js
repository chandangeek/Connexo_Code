Ext.define('Imt.usagepointmanagement.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Ext.container.Container'
    ],
    stores: [
        'Imt.usagepointmanagement.store.MeterActivations',
        'Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets',
        'Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets',
        'Imt.metrologyconfiguration.store.MetrologyConfiguration',
        'Imt.usagepointmanagement.store.ServiceCategories',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.usagepointmanagement.store.PhaseCodes',
        'Imt.usagepointmanagement.store.BypassStatuses',
        'Imt.usagepointmanagement.store.measurementunits.Voltage',
        'Imt.usagepointmanagement.store.measurementunits.Amperage',
        'Imt.usagepointmanagement.store.measurementunits.Power',
        'Imt.usagepointmanagement.store.measurementunits.Volume',
        'Imt.usagepointmanagement.store.measurementunits.Pressure',
        'Imt.usagepointmanagement.store.measurementunits.PressureExtended',
        'Imt.usagepointmanagement.store.measurementunits.Capacity'
    ],
    views: [
        'Imt.usagepointmanagement.view.Setup',
        'Imt.usagepointmanagement.view.MetrologyConfigurationSetup',
        'Imt.usagepointmanagement.view.landingpageattributes.UsagePointTechnicalAttributesPanel'
    ],
    refs: [
        {ref: 'associatedDevices', selector: 'associated-devices'},
        {ref: 'associatedMetrologyConfiguration', selector: 'associated-metrology-configuration'},
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'attributesPanel', selector: '#usage-point-main-attributes-panel'},
        {ref: 'usagePointAttributes', selector: '#usage-point-attributes-panel'},
        {ref: 'usagePointTechnicalAttributesDeviceLink', selector: '#usagePointTechnicalAttributesDeviceLink'},
        {ref: 'usagePointTechnicalAttributesDeviceDates', selector: '#usagePointTechnicalAttributesDeviceDates'},
        {ref: 'mcAttributesPanel', selector: '#up-metrology-configuration-attributes-panel'},
        {ref: 'generalForm', selector: '#editable-form-general'},
        {ref: 'overview', selector: 'usage-point-management-setup'}
    ],

    init: function () {
        this.control({
            'usage-point-management-setup inline-editable-set-property-form': {
                saveClick: this.saveUsagePointAttributes
            },
            'usage-point-management-setup usage-point-main-attributes-panel': {
                saveClick: this.saveUsagePointAttributes
            },
            'usage-point-management-setup usage-point-technical-attributes-panel': {
                saveClick: this.saveUsagePointAttributes
            }
        });
    },

    showUsagePoint: function (mRID) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load();
        usagePointModel.load(mRID, {
            success: function (record) {
                    me.getApplication().fireEvent('usagePointLoaded', record);
                    var widget = Ext.widget('usage-point-management-setup', {router: router, parent: record.getData()});
                    me.parent = record.getData();

                    me.getApplication().fireEvent('changecontentevent', widget);
                    me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load({
                        callback: function () {
                            me.initAttributes(record);
                        }
                    });
                    me.getOverviewLink().setText(record.get('mRID'));
                    if (record.get('metrologyConfiguration') && record.get('metrologyConfiguration').name) {
                        widget.down('#fld-mc-name').setValue(record.get('metrologyConfiguration').name);
                    }

                    pageMainContent.setLoading(false);
            }
        });
    },

    initAttributes: function(record){
        var me = this,
            customAttributesModelUsagePoint = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
            customAttributesStoreUsagePoint = me.getStore('Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets'),
            customAttributesStoreMetrology = me.getStore('Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets');


        customAttributesStoreUsagePoint.getProxy().setUrl(record.get('mRID'));
        customAttributesModelUsagePoint.getProxy().setUrl(record.get('mRID'));
        customAttributesStoreMetrology.getProxy().setUrl(record.get('mRID'));

        Ext.suspendLayouts();

        me.getUsagePointAttributes().setLoading(true);
        me.getAttributesPanel().add({
            xtype: 'usage-point-main-attributes-panel',
            record: record
        });

        if(record.get('techInfo')){
            me.getAttributesPanel().add({
                xtype: 'usage-point-technical-attributes-panel',
                record: record,
                category: record.get('serviceCategory')
            });
        }
        Ext.resumeLayouts(true);

        customAttributesStoreUsagePoint.load(function () {
            me.getOverview().down('#custom-attribute-sets-placeholder-form-id').loadStore(this);
            me.getUsagePointAttributes().setLoading(false);
        });

        me.getAssociatedMetrologyConfiguration().setLoading(true);
        customAttributesStoreMetrology.load(function () {
            me.getOverview().down('#metrology-custom-attribute-sets-placeholder-form-id').loadStore(this);
            me.getAssociatedMetrologyConfiguration().setLoading(false);
        });
    },

    saveUsagePointAttributes: function(form, record){
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        record.save({
            success: function (record, response, success) {
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePoint.acknowledge.updateSuccess', 'IMT', 'Usage point saved'));
            },
            failure: function (record, response, success) {
                var responseText = Ext.decode(response.response.responseText, true);
                if (responseText && Ext.isArray(responseText.errors)) {
                    form.markInvalid(responseText.errors);
                }
            }
        });
    }
});

