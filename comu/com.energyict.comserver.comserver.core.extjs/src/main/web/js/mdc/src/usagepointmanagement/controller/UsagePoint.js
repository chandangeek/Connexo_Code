Ext.define('Mdc.usagepointmanagement.controller.UsagePoint', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.usagepointmanagement.model.UsagePointComplete',
        'Ext.container.Container'
    ],
    stores: [],
    views: [
        'Mdc.usagepointmanagement.view.Setup'
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
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePointComplete'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            actualModel,
            actualForm;

        pageMainContent.setLoading(true);

        usagePointModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);
                var widget = Ext.widget('usage-point-management-setup', {router: router});

                switch (record.get('serviceCategory')) {
                    case('ELECTRICITY'):
                    {
                        actualForm = Ext.create('Mdc.usagepointmanagement.view.UsagePointAttributesFormTechnicalElectricity');
                        actualModel = Ext.create('Mdc.usagepointmanagement.model.UsagePointElectricity', record.data);
                    }
                        break;
                    default:
                    {
                        actualForm = Ext.create('Mdc.usagepointmanagement.view.UsagePointAttributesFormMain');
                        actualModel = Ext.create('Mdc.usagepointmanagement.model.UsagePoint', record.data);
                    }
                }

                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(actualModel.get('mRID'));
                me.getAttributesPanel().add(actualForm);
                actualForm.getForm().loadRecord(actualModel);

                var store = me.getStore('Mdc.usagepointmanagement.store.MeterActivations'),
                    metrologyConfiguration = me.getMetrologyConfiguration();
                store.getProxy().setExtraParam('mRID', mRID);
                store.load({
                    callback: function () {
                        var meterActivations = store.data.items;
                        for (var i = meterActivations.length - 1; i >= 0; i--) {
                            var meterActivation = meterActivations[i].data,
                                fromToDate,
                                activationLabel;
                            if (i === meterActivations.length - 1) {
                                if (meterActivation.version === 1) {
                                    activationLabel = Uni.I18n.translate('usagePointManagement.linkedDevices', 'MDC', 'Linked device');
                                    metrologyConfiguration.down('#activationsArea').removeAll();
                                }
                                else {
                                    metrologyConfiguration.down('#activationsArea').add({
                                        xtype: 'menuseparator',
                                        margin: '0 0 20 0'
                                    });
                                    activationLabel = Uni.I18n.translate('usagePointManagement.history', 'MDC', 'History');
                                }
                            }
                            else if (i === meterActivations.length - 2)
                                if (activationLabel != Uni.I18n.translate('usagePointManagement.history', 'MDC', 'History'))
                                    activationLabel = Uni.I18n.translate('usagePointManagement.history', 'MDC', 'History');
                                else activationLabel = ' ';
                            else activationLabel = ' ';
                            if (meterActivation.start) fromToDate = 'from ' + Uni.DateTime.formatDateTimeShort(new Date(meterActivation.start));
                            if (meterActivation.end) fromToDate += ' to ' + Uni.DateTime.formatDateTimeShort(new Date(meterActivation.end));
                            metrologyConfiguration.down('#activationsArea').add(
                                {
                                    labelAlign: 'right',
                                    xtype: 'fieldcontainer',
                                    labelWidth: 100,
                                    fieldLabel: activationLabel,
                                    layout: {
                                        type: 'vbox'
                                    },
                                    items: [
                                        {
                                            xtype: 'component',
                                            cls: 'x-form-display-field',
                                            width: 100,
                                            autoEl: {
                                                tag: 'a',
                                                href: '#/devices/' + meterActivation.meter.mRID,
                                                html: meterActivation.meter.mRID
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            value: fromToDate
                                        }
                                    ]
                                }
                            );
                            if (i === meterActivations.length - 1 && meterActivation.version === 1 && i > 0)
                                metrologyConfiguration.down('#activationsArea').add({
                                    xtype: 'menuseparator',
                                    margin: '0 0 20 0'
                                });
                        }
                        pageMainContent.setLoading(false);
                    }
                });
            }
        });
    }
});

