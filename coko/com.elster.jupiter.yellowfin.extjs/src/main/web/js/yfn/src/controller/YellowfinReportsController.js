Ext.define('Yfn.controller.YellowfinReportsController', {
    extend: 'Ext.app.Controller',
    requires: [
        'Yfn.view.ReportView'
    ],
    stores:[
        'ReportInfos'
    ],

    showReport: function(report) {

        var widget = Ext.widget('reportView');
        widget.reportName = report;
        this.getApplication().fireEvent('changecontentevent', widget);

    },

    showDeviceConfig: function() {
        this.showReport('DeviceConfig');
    },

    showDeviceGatewayTopology: function() {
        this.showReport('DeviceGatewayTopology');
    },

    showCommunicationPerformance: function() {
        this.showReport('CommunicationPerformance');
    }



});