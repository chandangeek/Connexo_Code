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

    loadReportFilters: function (reportUUID) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var wizard = me.getController('Yfn.controller.setup.GenerateReportWizard');
        wizard.selectedReportUUID = reportUUID;
        var reportFiltersView = me.getReportFiltersView();

        me.reportFilters = Ext.JSON.decode(decodeURIComponent(router.queryParams.filter));
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
                            fieldLabel: filterRecord.get('filterDisplayName') + ' ' + me.translateFilterType(filterRecord.get('filterType')),
                            value: me.reportFilters[filterName]
                        });
                    }
                    else{
                        var formFields = wizard.createFilterControls(filterRecord, filterOmittable ? "filter": "prompt",me.reportFilters[filterName] );

                        formFields = Ext.isArray(formFields) ? formFields : [formFields];
                        formFields.unshift({
                            xtype: 'displayfield',
                            labelAlign: 'left',
                            labelWidth:300,
                            fieldLabel: filterDescription + ' ' + me.translateFilterType(filterType)
                        });

                        var fieldContainer = {
                            xtype: 'container',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            columnWidth: 0.5,
                            maxWidth: 250,
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

        console.log(display);

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
        var filterId = filterRecord.get('filterDisplayType');

        if(filterType == "BETWEEN")
            return [queryValue.from, queryValue.to];
        else return queryValue;
    },
    translateFilterType : function(filterType){
        switch(filterType){
            case 'EQUAL': return Uni.I18n.translate('generatereport.reportTypeEQUAL', 'YFN', 'Equal to');
            case 'NOTEQUAL': return Uni.I18n.translate('generatereport.reportTypeNOTEQUAL', 'YFN', 'Different from');
            case 'GREATER': return Uni.I18n.translate('generatereport.reportTypeGREATER', 'YFN', 'Greater than');
            case 'GREATEREQUAL': return Uni.I18n.translate('generatereport.reportTypeGREATEREQUAL', 'YFN', 'Greater than or equal to');
            case 'LESS': return Uni.I18n.translate('generatereport.reportTypeLESS', 'YFN', 'Less than');
            case 'LESSEQUAL': return Uni.I18n.translate('generatereport.reportTypeLESSEQUAL', 'YFN', 'Less than or equal to');
            case 'BETWEEN': return Uni.I18n.translate('generatereport.reportTypeBETWEEN', 'YFN', 'Between');
            case 'NOTBETWEEN': return Uni.I18n.translate('generatereport.reportTypeNOTBETWEEN', 'YFN', 'Not Between');
            case 'INLIST': return Uni.I18n.translate('generatereport.reportTypeINLIST', 'YFN', 'In List');
            case 'NOTINLIST': return Uni.I18n.translate('generatereport.reportTypeNOTINLIST', 'YFN', 'Not In List');
            case 'ISNULL': return Uni.I18n.translate('generatereport.reportTypeISNULL', 'YFN', 'Is Null');
            case 'ISNOTNULL': return Uni.I18n.translate('generatereport.reportTypeISNOTNULL', 'YFN', 'Is Not Null');
            case 'EQUALCOLUMN': return Uni.I18n.translate('generatereport.reportTypeEQUALCOLUMN', 'YFN', 'Equals Column');
            case 'NOTEQUALCOLUMN': return Uni.I18n.translate('generatereport.reportTypeNOTEQUALCOLUMN', 'YFN', 'Different from Column');
            case 'GREATERCOLUMN': return Uni.I18n.translate('generatereport.reportTypeGREATERCOLUMN', 'YFN', 'Greater than Column');
            case 'GREATEREQUALCOLUMN': return Uni.I18n.translate('generatereport.reportTypeGREATEREQUALCOLUMN', 'YFN', 'Greater than or Equal to Column');
            case 'LESSCOLUMN': return Uni.I18n.translate('generatereport.reportTypeLESSCOLUMN', 'YFN', 'Less than Column');
            case 'LESSEQUALCOLUMN': return Uni.I18n.translate('generatereport.reportTypeLESSEQUALCOLUMN', 'YFN', 'Less than or Equal to Column');
            case 'MINIMUMDATE': return Uni.I18n.translate('generatereport.reportTypeMINIMUMDATE', 'YFN', 'Minimum Date');
            case 'MAXIMUMDATE': return Uni.I18n.translate('generatereport.reportTypeMAXIMUMDATE', 'YFN', 'Maximum Date');
            case 'LINKFILTER': return Uni.I18n.translate('generatereport.reportTypeLINKFILTER', 'YFN', 'Link to Filter');
                return filterType;
        }
    }


});