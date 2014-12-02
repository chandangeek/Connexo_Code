Ext.define('Yfn.view.ReportView', {
    extend: 'Ext.container.Container',
    alias: 'widget.reportView',
    itemId: 'reportView',

    overflowY: 'hidden',
    layout: 'fit',
    reportName: '',

    listeners: {
        afterrender: function(){

            var data;
            Ext.Ajax.request({
                url: '/api/yfn/user/login',
                method: 'POST',
                async: false,
                success: function(response){
                    data = Ext.JSON.decode(response.responseText);
                }
            });

            var reportUUID = '';
            switch(this.reportName) {
                case 'DeviceConfig':
                    reportUUID = '04521dfb-8753-475f-bc1a-331893add986';
                    break;
                case 'DeviceGatewayTopology':
                    reportUUID = '70744864-4782-4350-8424-cdf56c5ae33b';
                    break;
                case 'CommunicationPerformance':
                    reportUUID = '8d57261b-860d-47a3-8c32-19993e3754f7';
                    break;
                default:
                    reportUUID = ''
            }

            if (reportUUID.length>0){
                yellowfin.loadReport({
                    reportUUID: reportUUID,
                    element: this.getEl().dom,
                    token: data.token,
                    //username: 'admin',
                    //password: 'admin',
                    fitTableWidth:false
                });
            }
        },
        resize: function(component, width, height, oldWidth, oldHeight, eOpts) {

            // resize yellowfin report
            var options =  {};
            options.element =  this.getEl().dom;
            options.height = height - 5;
            options.width = width;
            options.showTitle = true;
            if (options.showTitle)
                options.height -= 30;


            if (Ext.DomQuery.select('.yfReportTitleOuter').length>0) {
                var yfReportTitleOuter = Ext.DomQuery.select('.yfReportTitleOuter')[0];
                yfReportTitleOuter.style.width = '100%';
            }

            if (Ext.DomQuery.select('.yfReportShareInput').length>0) {
                var yfReportShareInput = Ext.DomQuery.select('.yfReportShareInput')[0];
                yfReportShareInput.style.width = Math.round(width * 0.9) + 'px';
            }

            if (Ext.DomQuery.select('.yfReportOuterContainer').length>0) {
                var yfReportOuterContainer = Ext.DomQuery.select('.yfReportOuterContainer')[0];
                    yfReportOuterContainer.style.height = height + 'px';
                    yfReportOuterContainer.style.width = '100%';
            }

            if (Ext.DomQuery.select('.yfReport').length>0) {
                var yfReport = Ext.DomQuery.select('.yfReport')[0];
                yfReport.style.height = options.height + 'px';
                yfReport.style.width = options.width + 'px';
            }

            if (Ext.DomQuery.select('.yfLogon').length>0) {
                var yfLogon = Ext.DomQuery.select('.yfLogon')[0];
                yfLogon.style.height = options.height + 'px';
                yfLogon.style.width = options.width + 'px';
            }

            if (Ext.DomQuery.select('.yfReportFooter').length>0) {
                var yfReportFooter = Ext.DomQuery.select('.yfReportFooter')[0];
                yfReportFooter.style.width = '100%';
            }



        }
    }

});