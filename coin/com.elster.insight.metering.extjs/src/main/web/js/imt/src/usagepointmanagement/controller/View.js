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
        {ref: 'metrologyConfiguration', selector: 'metrology-configuration'},
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

                var store = me.getStore('Imt.usagepointmanagement.store.MeterActivations'),
                    metrologyConfiguration = me.getMetrologyConfiguration();
                store.getProxy().setExtraParam('usagePointMRID', mRID);
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
                                            href: router.getRoute('administration/device').buildUrl({mRID: item.get('meter').mRID}),
                                            html: item.get('meter').mRID
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        value: 'from ' + Uni.DateTime.formatDateTimeShort(new Date(item.get('start')))
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
                                            href: router.getRoute('administration/device').buildUrl({mRID: item.get('meter').mRID}),
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

