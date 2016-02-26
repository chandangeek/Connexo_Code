Ext.define('Imt.usagepointmanagement.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.usagepointmanagement.service.AttributesMaps',
        'Ext.container.Container'
    ],
    stores: [
        'Imt.usagepointmanagement.store.MeterActivations',
        'Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets',
        'Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets',
        'Imt.metrologyconfiguration.store.MetrologyConfiguration'
    ],
    views: [
        'Imt.usagepointmanagement.view.Setup',
        'Imt.usagepointmanagement.view.MetrologyConfigurationSetup'
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
            }
        });
    },

    showUsagePoint: function (mRID) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);

        usagePointModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);
                var widget = Ext.widget('usage-point-management-setup', {router: router, parent: record.getData()});
                me.parent = record.getData();

                me.getApplication().fireEvent('changecontentevent', widget);
                me.initAttributes(record);
                me.getOverviewLink().setText(record.get('mRID'));
                if(record.get('metrologyConfiguration') && record.get('metrologyConfiguration').name){
                    widget.down('#fld-mc-name').setValue(record.get('metrologyConfiguration').name);
                }



                //var store = me.getStore('Imt.usagepointmanagement.store.MeterActivations'),
                //		associatedDevices = me.getAssociatedDevices();
                //store.getProxy().setExtraParam('usagePointMRID', mRID);
                //store.load({
                //    callback: function () {
                //        store.each(function (item) {
                //            if (!item.get('end')) {
                //            	associatedDevices.down('#associatedDevicesLinked').removeAll();
                //            	associatedDevices.down('#associatedDevicesLinked').add(
                //                    {
                //                        xtype: 'component',
                //                        cls: 'x-form-display-field',
                //                        autoEl: {
                //                            tag: 'a',
                //                            href: router.getRoute('usagepoints/view/device').buildUrl({mRID: mRID, deviceMRID: item.get('meter').mRID}),
                //                            html: item.get('meter').mRID
                //                        }
                //                    },
                //                    {
                //                        xtype: 'displayfield',
                //                        value: 'from ' + Uni.DateTime.formatDateTimeShort(new Date(item.get('start')))
                //                    }
                //                );
                //            } else {
                //            	associatedDevices.down('#associatedDevicesHistory').show();
                //            	associatedDevices.down('#associatedDevicesSeparator').show();
                //            	associatedDevices.down('#associatedDevicesHistory').add(0,
                //
                //                    {
                //                        xtype: 'component',
                //                        cls: 'x-form-display-field',
                //                        autoEl: {
                //                            tag: 'a',
                //                            href: router.getRoute('usagepoints/view/device').buildUrl({mRID: mRID, deviceMRID: item.get('meter').mRID}),
                //                            html: item.get('meter').mRID
                //                        }
                //                    },
                //                    {
                //                        xtype: 'displayfield',
                //                        value: 'from ' + Uni.DateTime.formatDateTimeShort(new Date(item.get('start'))) + ' to ' + Uni.DateTime.formatDateTimeShort(new Date(item.get('end')))
                //                    }
                //                );
                //            }
                //        });
                //        pageMainContent.setLoading(false);
                //    }
                //});
                pageMainContent.setLoading(false);
            }
        });
    },

    initAttributes: function(record){
        var me = this,
            customAttributesStoreUsagePoint = me.getStore('Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets'),
            customAttributesModelUsagePoint = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
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
        me.getAttributesPanel().add({
            xtype: 'usage-point-main-attributes-panel',
            record: record,
            category: record.get('serviceCategory')
        });
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

    showMetrologyConfiguration: function (mRID, id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            linkedStore = Ext.getStore('Imt.metrologyconfiguration.store.LinkedValidationRulesSet'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        linkedStore.getProxy().setUrl(id);
    	linkedStore.load(function () {
            if (this.getCount() === 0) {
               this.add({id:'0', name:'-'});
            }
        });
	
        pageMainContent.setLoading(true);

        metrologyConfigurationModel.load(id, {

            success: function (record) {
                me.getApplication().fireEvent('metrologyConfigurationLoaded', record);
                var widget = Ext.widget('up-metrology-configuration-setup', {router: router});
                var actualModel = Ext.create('Imt.metrologyconfiguration.model.MetrologyConfiguration', record.data);
                var actualForm = Ext.create('Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm', {router: router});
                
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getMcAttributesPanel().add(actualForm);
                actualForm.getForm().loadRecord(actualModel);
                pageMainContent.setLoading(false);
            }
        });
    },

    saveUsagePointAttributes: function(form, record){
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        form.updateRecord();
        record.set('parent', me.parent);

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

