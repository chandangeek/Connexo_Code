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
        'Imt.usagepointmanagement.view.MetrologyConfigurationSetup',
        'Imt.usagepointmanagement.view.UsagePointAttributesFormMain',
        'Imt.usagepointmanagement.view.UsagePointAttributesFormTechnicalElectricity'
    ],
    refs: [
        {ref: 'associatedDevices', selector: 'associated-devices'},
        {ref: 'associatedMetrologyConfiguration', selector: 'associated-metrology-configuration'},
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'attributesPanel', selector: '#usage-point-attributes-panel'},
        {ref: 'usagePointTechnicalAttributesDeviceLink', selector: '#usagePointTechnicalAttributesDeviceLink'},
        {ref: 'usagePointTechnicalAttributesDeviceDates', selector: '#usagePointTechnicalAttributesDeviceDates'},
        {ref: 'mcAttributesPanel', selector: '#up-metrology-configuration-attributes-panel'},
        {ref: 'generalForm', selector: '#editable-form-general'},
        {ref: 'overview', selector: 'usage-point-management-setup'}
    ],

    init: function () {
        //this.control({
        //    '#usage-point-attributes-panel button[action=save]': {
        //        click: this.saveUsagePointAttributes
        //    }
        //});
    },

    showUsagePoint: function (mRID) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            actualModel,
            actualForm;
       
        pageMainContent.setLoading(true);

        usagePointModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);
                var widget = Ext.widget('usage-point-management-setup', {router: router});


                me.getApplication().fireEvent('changecontentevent', widget);
                me.initAttributes(record);
                //me.getOverviewLink().setText(actualModel.get('mRID'));
                //me.getAttributesPanel().add(actualForm);
                //generalForm.getForm().loadRecord(actualModel);
                //actualForm.getForm().loadRecord(actualModel);
                //me.getGeneralForm().loadRecord(me.record);





                //var associatedMetrologyConfiguration = me.getAssociatedMetrologyConfiguration();
                //associatedMetrologyConfiguration.down('#associatedMetrologyConfiguration').removeAll();

//                if (  record.get('metrologyConfiguration') === null ||  record.get('metrologyConfiguration') === "" ) {
//                	associatedMetrologyConfiguration.down('#associatedMetrologyConfiguration').add(
//                            {
//                                xtype: 'component',
//                                cls: 'x-form-display-field',
//                                html: '-'
//                            });
//                } else {
//                	associatedMetrologyConfiguration.down('#associatedMetrologyConfiguration').add(
//                    {
//                        xtype: 'component',
//                        cls: 'x-form-display-field',
//                        autoEl: {
//                            tag: 'a',
////                            href: router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl({mRID: mRID, mcid: record.get('metrologyConfiguration').id}),
//                            href: router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: record.get('metrologyConfiguration').id}),
//                            html: record.get('metrologyConfiguration').name
//                        }
//                    });
//                }
                
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
            customAttributesStoreUsagePoint = me.getStore('Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets'),
            customAttributesModelUsagePoint = me.getStore('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
            customAttributesStoreMetrology = me.getStore('Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets'),
            customAttributesModelMetrology = me.getStore('Imt.customattributesonvaluesobjects.model.AttributeSetOnMetrologyConfiguration');


        customAttributesStoreUsagePoint.getProxy().setUrl(record.get('mRID'));
        customAttributesModelUsagePoint.getProxy().setUrl(record.get('mRID'));
        customAttributesStoreMetrology.getProxy().setUrl(record.get('mRID')); //TODO Put metrology id
        customAttributesModelMetrology.getProxy().setUrl(record.get('mRID')); //TODO Put metrology id

        Ext.suspendLayouts();
        me.getAttributesPanel().add({
            xtype: 'usage-point-main-attributes-panel',
            record: record
        });
        me.getAttributesPanel().add({
            xtype: 'usage-point-main-attributes-panel',
            record: record,
            category: record.get('serviceCategory')
        });

        customAttributesStoreMetrology.load(function () {
            me.getOverview().down('#metrology-custom-attribute-sets-placeholder-form-id').loadStore(this);
        });
        customAttributesStoreUsagePoint.load(function () {
            me.getOverview().down('#custom-attribute-sets-placeholder-form-id').loadStore(this);
        });

        Ext.resumeLayouts(true);
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

    //saveUsagePointAttributes: function(){
    //    var me = this,
    //        router = me.getController('Uni.controller.history.Router'),
    //        usagePointModel = me.getModel('Imt.usagepointmanagement.model.UsagePoint');
    //    var widget = Ext.widget('usage-point-management-setup', {router: router});
    //    //console.log(me.record);
    //    //me.getGeneralForm().updateRecord(me.record);
    //    //console.log(me.getGeneralForm().getRecord());
    //    //var pencilBtns = Ext.ComponentQuery.query('#edit-field');
    //    //Ext.each(pencilBtns, function(btn){
    //    //    //btn.setDisabled(disabled);
    //    //    console.log(btn.getValue());
    //    //    console.log(btn);
    //    //});
    //    //var record = me.getGeneralForm().getValues();
    //    //me.record.set(record);
    //    me.getGeneralForm().getRecord().save();
    //
    //    //console.log(widget.down('usagePointAttributesFormMain'));
    //    //me.record.save();
    //
    //}
});

