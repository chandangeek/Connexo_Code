Ext.define('Imt.usagepointmanagement.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Ext.container.Container'
    ],
    stores: [
        'Imt.usagepointmanagement.store.MeterActivations'
    ],
    views: [
        'Imt.usagepointmanagement.view.Setup'
    ],
    refs: [
        {ref: 'associatedDevices', selector: 'associated-devices'},
        {ref: 'associatedMetrologyConfiguration', selector: 'associated-metrology-configuration'},
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'attributesPanel', selector: '#usage-point-attributes-panel'},
        {ref: 'usagePointTechnicalAttributesDeviceLink', selector: '#usagePointTechnicalAttributesDeviceLink'},
        {ref: 'usagePointTechnicalAttributesDeviceDates', selector: '#usagePointTechnicalAttributesDeviceDates'}
    ],

    init: function () {
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

                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(actualModel.get('mRID'));
                me.getAttributesPanel().add(actualForm);
                actualForm.getForm().loadRecord(actualModel);

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
                                            href: router.getRoute('usagepoints/device').buildUrl({mRID: item.get('meter').mRID}),
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
                                            href: router.getRoute('usagepoints/device').buildUrl({mRID: item.get('meter').mRID}),
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
    }
});

