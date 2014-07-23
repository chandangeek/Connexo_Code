Ext.define('Mdc.controller.setup.DeviceRegisterData', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterdata.MainSetup',
        'setup.deviceregisterdata.MainGrid',
        'setup.deviceregisterdata.textregisterreport.Setup',
        'setup.deviceregisterdata.textregisterreport.Grid',
        'setup.deviceregisterdata.textregisterreport.Preview',
        'setup.deviceregisterdata.valueregisterreport.Setup',
        'setup.deviceregisterdata.valueregisterreport.Grid',
        'setup.deviceregisterdata.valueregisterreport.Preview',
        'setup.deviceregisterdata.eventregisterreport.Setup',
        'setup.deviceregisterdata.eventregisterreport.Grid',
        'setup.deviceregisterdata.eventregisterreport.Preview'

    ],

    stores: [
        'RegisterData',
        'NumericalRegisterData',
        'EventRegisterData',
        'TextRegisterData'
    ],

    refs: [
        {ref: 'deviceEventRegisterReportPreview', selector: '#deviceEventRegisterReportPreview'},
        {ref: 'deviceValueRegisterReportPreview', selector: '#deviceValueRegisterReportPreview'},
        {ref: 'deviceTextRegisterReportPreview', selector: '#deviceTextRegisterReportPreview'}
    ],

    init: function () {
        this.control({
            '#deviceValueRegisterReportSetup #numerical-deviceRegisterReportGrid': {
                select: this.loadNumericalGridItemDetail
            }
        });
        this.control({
            '#deviceEventRegisterReportSetup #event-deviceRegisterReportGrid': {
                select: this.loadEventGridItemDetail
            }
        });
        this.control({
            '#deviceTextRegisterReportSetup #text-deviceRegisterReportGrid': {
                select: this.loadTextGridItemDetail
            }
        });
    },

    loadTextGridItemDetail: function(selectionModel, record) {
        var previewPanel = this.getDeviceTextRegisterReportPreview();
        this.loadGridItemDetail(previewPanel, record);
    },

    loadNumericalGridItemDetail: function(selectionModel, record) {
        var previewPanel = this.getDeviceValueRegisterReportPreview();
        this.loadGridItemDetail(previewPanel, record);
    },

    loadEventGridItemDetail: function(selectionModel, record) {
        var previewPanel = this.getDeviceEventRegisterReportPreview();
        this.loadGridItemDetail(previewPanel, record);
    },

    loadGridItemDetail: function (previewPanel, record) {
        var form = previewPanel.down('form');
        previewPanel.setTitle(Ext.util.Format.date(record.get('timeStamp'), 'M j, Y \\a\\t G:i'));
        form.loadRecord(record);
    },

    showDeviceRegisterDataView: function(mRID, registerId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            store,
            widget;

        viewport.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', mRID);
                model.load(registerId, {
                    success: function (register) {
                        widget = Ext.widget(register.get('type') + '-deviceregisterreportsetup', {mRID: mRID, registerId: registerId});
                        store = widget.down('#' + register.get('type') + '-deviceRegisterReportGrid').store;
                        store.getProxy().extraParams = ({mRID: mRID, registerId: registerId});
                        store.load();
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.down('#stepsMenu').setTitle(register.get('name'));
                    },
                    callback: function () {
                        viewport.setLoading(false);
                    }
                });
            }

        });
    }
});

