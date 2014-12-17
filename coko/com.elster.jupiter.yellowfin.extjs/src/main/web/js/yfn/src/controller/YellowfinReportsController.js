Ext.define('Yfn.controller.YellowfinReportsController', {
    extend: 'Ext.app.Controller',
    requires: [
        'Yfn.view.ReportView',
        'Yfn.store.ReportInfos',
        'Yfn.store.ReportFilterInfos',
        'Yfn.store.ReportFilterListItems'
    ],
    stores:[
        'ReportInfos'
    ],

    refs: [
        {
            ref: 'reportView',
            selector: 'report-view'
        },
        {
            ref: 'reportFiltersView',
            selector: 'report-view #reportFilters'
        },
        {
            ref: 'reportContentView',
            selector: 'report-view #reportContent'
        }
    ],
    init: function () {
        this.control({
            'report-view #reportContent': {
                resize: this.resizeReportPanel
            },
            'report-view #refresh-btn':{
                click:this.generateReport
            },
            'report-view #export-report-btn menucheckitem[action=export]':{
                click:this.exportReport
            }

        });
    },
    reportUUID : null,
    reportFilters:null,
    reportInfo:null,
    filterValues:{},
    reportOptions:null,
    reportId:0,

    showReport: function(report) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        me.reportUUID = router.queryParams.reportUUID;
        var widget = Ext.widget('report-view');
        this.getApplication().fireEvent('changecontentevent', widget);
        this.loadReportFilters(me.reportUUID);
    },

    resizeReportPanel: function(component) {
        if(!component)
            return;
        //console.log(component.getHeight());
        //console.log(component.getWidth());
        // resize yellowfin report
        var options =  {};
        options.element =  component.getEl().dom;
        options.height = component.getHeight() - 38;
        options.width = component.getWidth()-3;
        options.showTitle = false;
        if (options.showTitle)
            options.height -= 30;


        if (Ext.DomQuery.select('.yfReportTitleOuter').length>0) {
            var yfReportTitleOuter = Ext.DomQuery.select('.yfReportTitleOuter')[0];
            yfReportTitleOuter.style.width = '100%';
        }

        if (Ext.DomQuery.select('.yfReportShareInput').length>0) {
            var yfReportShareInput = Ext.DomQuery.select('.yfReportShareInput')[0];
            yfReportShareInput.style.width = Math.round(component.getWidth() * 0.9) + 'px';
        }

        if (Ext.DomQuery.select('.yfReportOuterContainer').length>0) {
            var yfReportOuterContainer = Ext.DomQuery.select('.yfReportOuterContainer')[0];
            yfReportOuterContainer.style.height = component.getHeight() + 'px';
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
            //yfReportFooter.style.width = '100%';
            yfReportFooter.style.width = options.width+5 + 'px';
        }



    },

    exportReport:function(button){
        var me = this;
        eval("javascript:yellowfin.reports.exportReport("+me.reportId+", '"+button.exportType+"')");
    },
    loadReportFilters: function (reportUUID) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var wizard = me.getController('Yfn.controller.setup.GenerateReportWizard');
        wizard.selectedReportUUID = reportUUID;
        var reportFiltersView = me.getReportFiltersView();

        me.reportFilters = Ext.JSON.decode(decodeURIComponent(router.queryParams.filter)) || {};
        reportFiltersView.setLoading(true);

        var reportPromptsContainer = reportFiltersView.down('#reportPrompts');
        var reportFiltersContainer = reportFiltersView.down('#reportFilters');

        var reportsStore = Ext.create('Yfn.store.ReportInfos', {});
        reportsStore.getProxy().setExtraParam('reportUUID',reportUUID);
        reportsStore.load(function(records){
            if(records.length>0){
                reportInfo = records[0];
                reportFiltersView.setTitle(reportInfo.get('name'));
                me.reportId = reportInfo.get('reportId')

                //var reportDescription = reportFiltersView.down('#report-description');

                //reportDescription.setFieldLabel(Uni.I18n.translate('generatereport.reportNameTitle', 'YFN', reportInfo.get('name')));
                //reportDescription.setValue( reportRecord.get('description'));


            }
        });
        reportFiltersView.setLoading(true);
        me.filterValues = {};

        var reportFiltersStore = Ext.create('Yfn.store.ReportFilterInfos', {});
        if (reportFiltersStore) {
            var proxy = reportFiltersStore.getProxy();
            proxy.setExtraParam('reportUUID', reportUUID);
            reportFiltersStore.load(function (records) {

                Ext.each(records, function (filterRecord) {
                    var filterOmittable = filterRecord.get('filterOmittable');
                    var filterType = filterRecord.get('filterType');
                    var filterName = filterRecord.get('filterName');
                    var filterDescription = filterRecord.get('filterDisplayName') || filterName;
                    me.filterValues[filterRecord.get('id')] = me.getFilterValue(filterRecord, me.reportFilters[filterName]);

                    if(!filterOmittable){
                        reportPromptsContainer.setVisible(true);
                        reportPromptsContainer.add({
                            xtype: 'displayfield',
                            labelAlign: 'top',
                            labelWidth:150,
                            width:300,
                            fieldLabel: filterRecord.get('filterDisplayName') + ' ' + wizard.translateFilterType(filterRecord.get('filterType')),
                            value: me.reportFilters[filterName]
                        });
                    }
                    else{
                        var formFields = wizard.createFilterControls(filterRecord, filterOmittable ? "filter": "prompt",me.reportFilters[filterName] );

                        formFields = Ext.isArray(formFields) ? formFields : [formFields];
                        formFields.unshift(
                            {
                                xtype: 'displayfield',
                                labelAlign: 'left',
                            labelWidth: 200,
                            fieldLabel: filterDescription + ' ' + wizard.translateFilterType(filterType)
                            });

                        var fieldContainer = {
                            xtype: 'container',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            columnWidth: 0.5,
                            maxWidth: 250,
                            margin:'0 10 0 0',
                            items:formFields
                        };
                        reportFiltersContainer.setVisible(true);
                        reportFiltersContainer.add(fieldContainer);
                    }


                });
                reportFiltersView.setLoading(false);

                me.generateReport();
            });
        }

    },
    generateReport : function(){
        var me = this;
        var reportContent = me.getReportContentView();
        var data;

        me.getFilterValues();
        console.log(me.filterValues);

        Ext.Ajax.request({
            url: '/api/yfn/user/token',
            method: 'POST',
            async: false,
            success: function(response){
                data = Ext.JSON.decode(response.responseText);
            }
        });

       // var filters = me.convertToYellowfinJSAPIFilters();


        var display;
        try{
            display = yellowfin.reports.reportOptions['r'+me.reportId].display;
        }
        catch(e){}


        me.reportOptions = {
            reportUUID: me.reportUUID,
            display:display,
            element: reportContent.getEl().dom,
            showFilters:false,
            showTitle:true,
            filters:me.filterValues,
            width:reportContent.getWidth()-3,
            height:reportContent.getHeight()-38,
            showExport:true,
            showSeries:true,
            showInfo:false,
            token: data.token,
            fitTableWidth:false
        }

        yellowfin.loadReport(me.reportOptions);


        me.resizeReportPanel(reportContent);

    },
    getFilterValues : function(){
        var me = this;
        var reportFiltersView = me.getReportFiltersView();

        var filters = reportFiltersView.query('[fieldType = filter]');

        for (var filter in filters) {
            if (filters.hasOwnProperty(filter)) {
                var field = filters[filter];
                var filterRecord = field.record;
                me.filterValues[filterRecord.get('id')] = me.getFilterValue(filterRecord, field.getFieldValue());
            }
        }
    },

    getFilterValue:function(filterRecord, queryValue){
        var filterType = filterRecord.get('filterType');
        var filterDisplayType = filterRecord.get('filterDisplayType');
        if(filterType == "BETWEEN")
            return [queryValue.from, queryValue.to];
        if(filterType == "INLIST" || filterType == "NOTINLIST") {
            if (Ext.isArray(queryValue))
                return queryValue;
            else if (Ext.isString(queryValue)) {
                return queryValue.split(',');
            }
        }
        return queryValue;
    }

});