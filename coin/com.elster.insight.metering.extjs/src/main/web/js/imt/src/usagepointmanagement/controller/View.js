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
        'Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets',
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
        {ref: 'attributesPanel', selector: '#usage-point-attributes-panel'},
        {ref: 'usagePointTechnicalAttributesDeviceLink', selector: '#usagePointTechnicalAttributesDeviceLink'},
        {ref: 'usagePointTechnicalAttributesDeviceDates', selector: '#usagePointTechnicalAttributesDeviceDates'},
        {ref: 'mcAttributesPanel', selector: '#up-metrology-configuration-attributes-panel'}
    ],

    init: function () {
    },

    showUsagePoint: function (mRID) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            customAttributesStoreUsagePoint = me.getStore('Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets'),
            customAttributesModelUsagePoint = me.getStore('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
            customAttributesStoreMetrology = me.getStore('Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets'),
            customAttributesModelMetrology = me.getStore('Imt.customattributesonvaluesobjects.model.AttributeSetOnMetrologyConfiguration'),
            actualModel,
            actualForm;
       
        pageMainContent.setLoading(true);

        customAttributesStoreUsagePoint.getProxy().setUrl(mRID);
        customAttributesModelUsagePoint.getProxy().setUrl(mRID);
        customAttributesStoreMetrology.getProxy().setUrl(mRID); //TODO Put metrology id
        customAttributesModelMetrology.getProxy().setUrl(mRID); //TODO Put metrology id

        usagePointModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);
                var widget = Ext.widget('usage-point-management-setup', {router: router});

                actualModel = Ext.create('Imt.usagepointmanagement.model.UsagePoint', record.data);

                switch (record.get('serviceCategory')) {
                    case('ELECTRICITY'):
                    {
                        actualForm = Ext.create('Imt.usagepointmanagement.view.UsagePointAttributesFormTechnicalElectricity');
                    }
                        break;
                    default:
                    {
                        actualForm = Ext.create('Imt.usagepointmanagement.view.UsagePointAttributesFormMain');
                    }
                }

                Ext.suspendLayouts();


                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(actualModel.get('mRID'));
                me.getAttributesPanel().add(actualForm);
                actualForm.getForm().loadRecord(actualModel);
                customAttributesStoreMetrology.load(function () {
                    widget.down('#metrology-custom-attribute-sets-placeholder-form-id').loadStore(this);
                });
                customAttributesStoreUsagePoint.load(function () {
                    widget.down('#custom-attribute-sets-placeholder-form-id').loadStore(this);
                });

                Ext.resumeLayouts(true);

                var associatedMetrologyConfiguration = me.getAssociatedMetrologyConfiguration();
                associatedMetrologyConfiguration.down('#associatedMetrologyConfiguration').removeAll();

                if (  record.get('metrologyConfiguration') === null ||  record.get('metrologyConfiguration') === "" ) {
                	associatedMetrologyConfiguration.down('#associatedMetrologyConfiguration').add(
                            {
                                xtype: 'component',
                                cls: 'x-form-display-field',
                                html: '-'
                            });
                } else {
                	associatedMetrologyConfiguration.down('#associatedMetrologyConfiguration').add(
                    {
                        xtype: 'component',
                        cls: 'x-form-display-field',
                        autoEl: {
                            tag: 'a',
//                            href: router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl({mRID: mRID, mcid: record.get('metrologyConfiguration').id}),
                            href: router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: record.get('metrologyConfiguration').id}),
                            html: record.get('metrologyConfiguration').name
                        }
                    });
                }
                
                var store = me.getStore('Imt.usagepointmanagement.store.MeterActivations'),
                		associatedDevices = me.getAssociatedDevices();
                store.getProxy().setExtraParam('usagePointMRID', mRID);
                store.load({
                    callback: function () {
                        store.each(function (item) {
                            if (!item.get('end')) {
                            	associatedDevices.down('#associatedDevicesLinked').removeAll();
                            	associatedDevices.down('#associatedDevicesLinked').add(
                                    {
                                        xtype: 'component',
                                        cls: 'x-form-display-field',
                                        autoEl: {
                                            tag: 'a',
                                            href: router.getRoute('usagepoints/view/device').buildUrl({mRID: mRID, deviceMRID: item.get('meter').mRID}),
                                            html: item.get('meter').mRID
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        value: 'from ' + Uni.DateTime.formatDateTimeShort(new Date(item.get('start')))
                                    }
                                );
                            } else {
                            	associatedDevices.down('#associatedDevicesHistory').show();
                            	associatedDevices.down('#associatedDevicesSeparator').show();
                            	associatedDevices.down('#associatedDevicesHistory').add(0,

                                    {
                                        xtype: 'component',
                                        cls: 'x-form-display-field',
                                        autoEl: {
                                            tag: 'a',
                                            href: router.getRoute('usagepoints/view/device').buildUrl({mRID: mRID, deviceMRID: item.get('meter').mRID}),
                                            html: item.get('meter').mRID
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        value: 'from ' + Uni.DateTime.formatDateTimeShort(new Date(item.get('start'))) + ' to ' + Uni.DateTime.formatDateTimeShort(new Date(item.get('end')))
                                    }
                                );
                            }
                        });
                        pageMainContent.setLoading(false);
                    }
                });


            }
        });




    },
    showMetrologyConfiguration: function (mRID, id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            linkedStore = Ext.getStore('Imt.metrologyconfiguration.store.LinkedValidationRulesSet'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            actualModel,
            actualForm;

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
});

