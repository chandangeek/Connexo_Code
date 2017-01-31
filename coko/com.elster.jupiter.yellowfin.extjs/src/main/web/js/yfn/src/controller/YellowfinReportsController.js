/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.controller.YellowfinReportsController', {
    extend: 'Ext.app.Controller',
    requires: [
        'Yfn.privileges.Yellowfin',
        'Yfn.view.ReportView',
        'Yfn.store.ReportInfos',
        'Yfn.store.ReportFilterInfos',
        'Yfn.store.ReportFilterListItems'
    ],
    searchEnabled: false,

    stores: [
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
            'report-view #refresh-btn': {
                click: this.generateReport
            },
            'report-view #export-report-btn menucheckitem[action=export]': {
                click: this.exportReport
            }

        });

        var navigationController = this.getController('Uni.controller.Navigation');
        var breadcrumbs = navigationController.getBreadcrumbs();
        breadcrumbs.setVisible(false);
        var navigationMenu = navigationController.getNavigationMenu();
        navigationMenu.setVisible(false);
        var navigationHeader = navigationMenu.up('viewport').down('navigationHeader');
        navigationHeader.setVisible(false);

    },
    reportUUID: null,
    reportFilters: null,
    reportInfo: null,
    filterValues: {},
    reportOptions: null,
    reportId: 0,
    generateReportWizardWidget: null,

    showReport: function (report) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        me.reportUUID = router.queryParams.reportUUID;
        var widget = Ext.widget('report-view');
        this.getApplication().fireEvent('changecontentevent', widget);
        this.generateReportWizardWidget = Ext.widget('generatereport-browse');
        this.createAdHocGroup(me.reportUUID);
    },

    resizeReportPanel: function (component) {
        if (!component)
            return;
        // resize yellowfin report
        var options = {};
        options.element = component.getEl().dom;
        options.height = component.getHeight() - 38;
        options.width = component.getWidth() - 3;
        options.showTitle = false;
        if (options.showTitle)
            options.height -= 30;


        if (Ext.DomQuery.select('.yfReportTitleOuter').length > 0) {
            var yfReportTitleOuter = Ext.DomQuery.select('.yfReportTitleOuter')[0];
            yfReportTitleOuter.style.width = '100%';
        }

        if (Ext.DomQuery.select('.yfReportShareInput').length > 0) {
            var yfReportShareInput = Ext.DomQuery.select('.yfReportShareInput')[0];
            yfReportShareInput.style.width = Math.round(component.getWidth() * 0.9) + 'px';
        }

        if (Ext.DomQuery.select('.yfReportOuterContainer').length > 0) {
            var yfReportOuterContainer = Ext.DomQuery.select('.yfReportOuterContainer')[0];
            yfReportOuterContainer.style.height = component.getHeight() + 'px';
            yfReportOuterContainer.style.width = '100%';
        }

        if (Ext.DomQuery.select('.yfReport').length > 0) {
            var yfReport = Ext.DomQuery.select('.yfReport')[0];
            yfReport.style.height = options.height + 'px';
            yfReport.style.width = options.width + 'px';
        }

        if (Ext.DomQuery.select('.yfLogon').length > 0) {
            var yfLogon = Ext.DomQuery.select('.yfLogon')[0];
            yfLogon.style.height = options.height + 'px';
            yfLogon.style.width = options.width + 'px';
        }

        if (Ext.DomQuery.select('.yfReportFooter').length > 0) {
            var yfReportFooter = Ext.DomQuery.select('.yfReportFooter')[0];
            //yfReportFooter.style.width = '100%';
            yfReportFooter.style.width = options.width + 5 + 'px';
        }


    },

    exportReport: function (button) {
        var me = this;
        eval("javascript:yellowfin.reports.exportReport(" + me.reportId + ", '" + button.exportType + "')");
    },

    createAdHocGroup: function (reportUUID) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');

        me.generateReportWizardWidget.setLoading(Uni.I18n.translate('generatereport.preparingReport', 'YFN', 'Preparing report. Please wait ...'));
        if (router.queryParams.search == 'false') {
            // refresh group
            var filter = Ext.JSON.decode(decodeURIComponent(router.queryParams.filter)) || {};
            var selectedGroups = filter['GROUPNAME'];
            if (_.isArray(selectedGroups)) {
                var groups = [];
                for (var i = 0; i < selectedGroups.length; i++) {
                    groups.push({name: selectedGroups[i]});
                }

                Ext.Ajax.request({
                    url: '/api/yfn/cachegroups/dynamic',
                    method: 'POST',
                    timeout: 180000,
                    //async: false,
                    jsonData: {
                        total: groups.length,
                        groups: groups
                    },
                    success: function () {
                        me.generateReportWizardWidget.setLoading(false);
                        // load report
                        me.loadReportFilters(reportUUID);
                    },
                    failure: function (response, opts) {
                        me.generateReportWizardWidget.setLoading(false);
                    }
                });
            }
            else { // NO device groups are required or provided
                me.generateReportWizardWidget.setLoading(false);
                // load report
                me.loadReportFilters(reportUUID);
            }
        }
        else {
            var searchCriteria = {};
            Ext.apply(searchCriteria, Ext.JSON.decode(router.queryParams.params));

            var url = '/api/ddr/cachegroups/adhoc?' + router.queryParamsToString(searchCriteria);
            Ext.Ajax.request({
                url: url,
                method: 'POST',
                params: searchCriteria,
                async: false,
                success: function (response) {
                    data = Ext.JSON.decode(response.responseText);
                    me.GROUPNAME = data.name;

                    // load report
                    me.loadReportFilters(reportUUID);
                }
            });
        }
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
        reportsStore.getProxy().setExtraParam('reportUUID', reportUUID);
        reportsStore.load(function (records) {
            if (records.length > 0) {
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
                    var filterDisplayType = filterRecord.get('filterDisplayType');
                    var filterDescription = filterRecord.get('filterDisplayName') || filterName;
                    me.filterValues[filterRecord.get('id')] = me.getFilterValue(filterRecord,
                        ((filterName == 'GROUPNAME') && (me.reportFilters[filterName])) == '__##SEARCH_RESULTS##__' ? me.GROUPNAME : me.reportFilters[filterName]);

                    var value = me.filterValues[filterRecord.get('id')];
                    if (value && value.toString().indexOf("__##SEARCH_RESULTS##__") != -1) {
                        value = Uni.I18n.translate('generatereport.searchResults', 'YFN', 'Search results')
                    }

                    if (filterDisplayType == "DATE") {
                        if (filterType == "BETWEEN")
                            value = (value[0] ? Uni.DateTime.formatDateLong(Ext.Date.parse(value[0], "Y-m-d")) : '') +
                                ' - ' +
                                (value[1] ? Uni.DateTime.formatDateLong(Ext.Date.parse(value[1], "Y-m-d")) : '');
                        else
                            value = value ? Uni.DateTime.formatDateLong(Ext.Date.parse(value, "Y-m-d")) : '';
                    }
                    if (filterDisplayType == "TIMESTAMP") {
                        if (filterType == "BETWEEN")
                            value = (value[0] ? Uni.DateTime.formatDateTime(Ext.Date.parse(value[0], "Y-m-d H:i:s"), Uni.DateTime.LONG, Uni.DateTime.SHORT) : '') +
                                ' - ' +
                                (value[1] ? Uni.DateTime.formatDateTime(Ext.Date.parse(value[1], "Y-m-d H:i:s"), Uni.DateTime.LONG, Uni.DateTime.SHORT) : '');
                        else
                            value = value ? Uni.DateTime.formatDateTime(Ext.Date.parse(value, "Y-m-d H:i:s"), Uni.DateTime.LONG, Uni.DateTime.SHORT) : '';
                    }


                    if (!filterOmittable) {
                        reportPromptsContainer.setVisible(true);
                        reportPromptsContainer.add({
                            xtype: 'displayfield',
                            labelAlign: 'top',
                            labelWidth: 150,
                            width: 300,
                            fieldLabel: filterRecord.get('filterDisplayName') + ' ' + wizard.translateFilterType(filterRecord.get('filterType')),
                            value: value//me.reportFilters[filterName]
                        });
                    }
                    else {
                        var formFields = wizard.createFilterControls(filterRecord, filterOmittable ? "filter" : "prompt", me.reportFilters[filterName]);

                        formFields = Ext.isArray(formFields) ? formFields : [formFields];
                        formFields.unshift(
                            {
                                xtype: 'label',
                                padding: '0 0 10 0',
                                text: filterDescription + ' ' + wizard.translateFilterType(filterType)
                            });

                        var fieldContainer = {
                            xtype: 'container',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            columnWidth: 0.5,
                            maxWidth: 250,
                            margin: '0 10 0 0',
                            items: formFields
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
    generateReport: function (data) {
        var me = this;
        var reportContent = me.getReportContentView();
        if (typeof data == "undefined") {
            Ext.Ajax.request({
                url: '/api/yfn/user/token',
                method: 'POST',
                async: false,
                success: function (response) {
                    data = Ext.JSON.decode(response.responseText);
                    if (typeof yellowfin == "undefined") {
                        var url = data.url.charAt(data.url.length - 1) === "/" ? data.url + 'JsAPI' : data.url + '/JsAPI';
                        Ext.Loader.injectScriptElement(url, function () {
                            yellowfin.baseURL = url;
                            Ext.Loader.injectScriptElement(url + '?api=reports', function () {
                                me.generateReport(data);
                            }, function () {
                            });
                        }, function () {

                        });
                        return;
                    }
                }
            });
        }

        me.getFilterValues();

        var display;
        try {
            display = yellowfin.reports.reportOptions['r' + me.reportId].display;
        }
        catch (e) {
        }


        me.reportOptions = {
            reportUUID: me.reportUUID,
            display: display,
            element: reportContent.getEl().dom,
            showFilters: false,
            showTitle: true,
            filters: me.filterValues,
            width: reportContent.getWidth() - 3,
            height: reportContent.getHeight() - 38,
            showExport: true,
            showSeries: true,
            showInfo: false,
            token: data.token,
            fitTableWidth: false
        }
        if (typeof yellowfin == "undefined") {
            return;//
        }
        yellowfin.loadReport(me.reportOptions);


        me.resizeReportPanel(reportContent);

    },
    getFilterValues: function () {
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

    getFilterValue: function (filterRecord, queryValue) {
        var filterType = filterRecord.get('filterType');
        var filterDisplayType = filterRecord.get('filterDisplayType');
        if (filterType == "BETWEEN")
            return [queryValue.from, queryValue.to];
        if (filterType == "INLIST" || filterType == "NOTINLIST") {
            if (Ext.isArray(queryValue))
                return queryValue;
            else if (Ext.isString(queryValue)) {
                return queryValue.split(',');
            }
        }
        return queryValue;
    }

});