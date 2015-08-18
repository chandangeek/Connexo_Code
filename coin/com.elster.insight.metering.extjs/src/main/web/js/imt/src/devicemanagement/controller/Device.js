Ext.define('Imt.devicemanagement.controller.Device', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.devicemanagement.model.Device',
        'Ext.container.Container'
    ],
    stores: [
        'Imt.devicemanagement.store.Device'
    ],
    views: [
        'Imt.devicemanagement.view.Setup'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#device-overview-link'},
        {ref: 'attributesPanel', selector: '#device-attributes-panel'},
        {ref: 'deviceAttributesDeviceLink', selector: '#usagePointTechnicalAttributesDeviceLink'},
        {ref: 'usagePointTechnicalAttributesDeviceDates', selector: '#usagePointTechnicalAttributesDeviceDates'}
    ],

    init: function () {
    },

    showDevice: function (metername) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceModel = me.getModel('Imt.devicemanagement.model.Device'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            actualModel,
            actualForm;
       
        pageMainContent.setLoading(true);

        deviceModel.load(metername, {
            success: function (record) {
                me.getApplication().fireEvent('deviceLoaded', record);
                var widget = Ext.widget('device-management-setup', {router: router});

                actualForm = Ext.widget('deviceAttributesFormMain', {router: router});
                actualModel = Ext.create('Imt.devicemanagement.model.Device', record.data);
  

                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(actualModel.get('mRID'));
                me.getAttributesPanel().add(actualForm);
                actualForm.getForm().loadRecord(actualModel);
                pageMainContent.setLoading(false);
             
            }
        });
    }
});

