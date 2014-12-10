Ext.define('Yfn.controller.setup.GenerateReportWizard', {
    extend: 'Ext.app.Controller',
    views: [
        'Yfn.view.generatereport.Step1',
        'Yfn.view.generatereport.Step2',
        'Yfn.view.generatereport.Step3',
        'Yfn.view.generatereport.Step4',
        'Yfn.view.generatereport.Navigation',
        'Yfn.view.generatereport.Browse',
        'Yfn.view.generatereport.Wizard'
    ],
    requires: [
        'Uni.view.window.Wizard',
        'Yfn.view.generatereport.RadioGroup',
        'Uni.form.filter.FilterCombobox',
        'Uni.form.field.DateTime',
        'Dsh.store.filter.DeviceGroup'
    ],

    stores: [
        'Yfn.store.ReportFilterInfos'
    ],

    refs: [
        {
            ref: 'backButton',
            selector: 'generatereport-wizard #backButton'
        },
        {
            ref: 'nextButton',
            selector: 'generatereport-wizard #nextButton'
        },
        {
            ref: 'confirmButton',
            selector: 'generatereport-wizard #confirmButton'
        },
        {
            ref: 'finishButton',
            selector: 'generatereport-wizard #finishButton'
        },
        {
            ref: 'cancelButton',
            selector: 'generatereport-wizard #wizardCancelButton'
        },
        {
            ref: 'navigationMenu',
            selector: '#generatereportnavigation'
        },
        {
            ref: 'reportGroupsContainer',
            selector: '#step1-form'
        },
        {
            ref: 'wizard',
            selector: '#generatereportwizard'
        },
        {
            ref: 'step1',
            selector: 'generatereport-wizard-step1'
        },
        {
            ref: 'step2',
            selector: 'generatereport-wizard-step2'
        },
        {
            ref: 'step3',
            selector: 'generatereport-wizard-step3'
        },
        {
            ref: 'step4',
            selector: 'generatereport-wizard-step4'
        },
        {
            ref: 'generateReportLink',
            selector: 'generatereport-wizard-step4 #step4-generatereport-link'

        }
    ],

    generateReportWizardWidget: null,
    selectedReportUUID: null,
    reportsStore:null,
    selectedFilterValues: {},

    init: function () {
        this.control({
            'generatereport-wizard #backButton': {
                click: this.backClick
            },
            'generatereport-wizard #nextButton': {
                click: this.nextClick
            },
            'generatereport-wizard #confirmButton': {
                click: this.confirmClick
            },
            'generatereport-wizard #finishButton': {
                click: this.finishClick
            },
            'generatereport-wizard #wizardCancelButton': {
                click: this.cancelClick
            },
            'generatereport-navigation': {
                movetostep: this.moveToStep
            },


            'generatereport-wizard-step1': {
                activate: this.activateStep1
            },
            'generatereport-wizard-step2': {
                activate: this.activateStep2
            },
            'generatereport-wizard-step3': {
                activate: this.activateStep3
            },
            'generatereport-wizard-step4': {
                activate: this.activateStep4
            }
        });
    },

    moveToStep: function (step) {
        var layout = this.getWizard().getLayout();
        layout.setActiveItem(step - 1);
    },
    backClick: function () {
        var layout = this.getWizard().getLayout(),
            currentCmp = layout.getActiveItem();
        this.getNavigationMenu().movePrevStep();
        this.changeContent(layout.getPrev(), currentCmp);
    },

    nextClick: function () {
        var me=this, layout = this.getWizard().getLayout(),
            currentCmp = layout.getActiveItem();

        //this.getStep1FormErrorMessage().setVisible(false);
        //this.getStep1FormNameErrorMessage().setVisible(false);

        if(me.validatePage(currentCmp)){
            this.getNavigationMenu().moveNextStep();
            this.changeContent(layout.getNext(), layout.getActiveItem());
        }

        //this.getStep2FormErrorMessage().setVisible(false);
    },

    confirmClick: function () {

    },

    finishClick: function () {
        var me = this;
        console.log(me.selectedFilterValues);
        var link = me.getGenerateReportLink();
        var href = '../reports/index.html#/reports/showReport?reportUUID='+me.selectedReportUUID+'&filter='+encodeURIComponent(Ext.JSON.encode(me.selectedFilterValues));
        link.getEl().dom.href = href;
        link.getEl().dom.target = '_blank';
        link.getEl().dom.click();
    },

    cancelClick: function () {
        this.generateReportWizardWidget = null;
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups').forward();
    },

    showGenerateReportWizard: function () {
        //if (this.generateReportWizardWidget == null) {
        this.generateReportWizardWidget = Ext.widget('generatereport-browse');
        //}

        this.loadReportTypes();
        this.getApplication().fireEvent('changecontentevent', this.generateReportWizardWidget);
    },


    changeContent: function (nextCmp, currentCmp) {
        var layout = this.getWizard().getLayout();
        layout.setActiveItem(nextCmp);
        this.updateButtonsState(nextCmp);
    },

    updateButtonsState: function (activePage) {
        var me = this,
            wizard = me.getWizard(),
            layout = wizard.getLayout(),
            backBtn = me.getBackButton(),
            nextBtn = me.getNextButton(),
            finishBtn = me.getFinishButton(),
            cancelBtn = me.getCancelButton();

        var isFirst = !(layout.getPrev());
        var isLast = !(layout.getNext());
        backBtn.setDisabled(isFirst);
        backBtn.setVisible(!isFirst);
        nextBtn.setDisabled(isLast);
        nextBtn.setVisible(!isLast);
        finishBtn.setDisabled(!isLast);
        finishBtn.setVisible(isLast);
    },

    validatePage : function(page){
        var form = page.down("form") ;
        form = form && form.getForm();
        return (!form || form.isValid());
    },

    loadReportTypes: function () {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var selectedReportUUID = router.queryParams.reportUUID;
        var selectedReportGroup = router.queryParams.subCategory;

        me.reportsStore = me.reportsStore || Ext.create('Yfn.store.ReportInfos', {});
        if (me.reportsStore) {
            var proxy = me.reportsStore.getProxy();
            delete proxy.extraParams.category;
            delete proxy.extraParams.subCategory;
            proxy.setExtraParam('category', 'MDC');
            //proxy.setExtraParam('subCategory', 'Device Connections');
            me.reportsStore.load(function (records) {
                var allReports = {};
                Ext.each(records, function (record) {
                    var reportDescription = record.get('description');
                    var reportName = record.get('name');
                    var reportUUID = record.get('reportUUID');
                    var subCategory = record.get('subCategory');

                    var reportGroup = allReports[subCategory];
                    if (!reportGroup) {
                        allReports[subCategory] = {checked: false, reportsList: []};
                        reportGroup = allReports[subCategory];
                        reportGroup.title = subCategory;
                        reportGroup.name = reportName;
                        reportGroup.disabled = selectedReportGroup && selectedReportGroup != subCategory;

                    }
                    if (reportUUID == selectedReportUUID) {
                        reportGroup.checked = true;
                    }
                    if (reportName && reportName.length) {
                        reportGroup.reportsList.push(
                            {
                                boxLabel: reportName,
                                tooltip: reportDescription,
                                inputValue: reportUUID,
                                name: 'reportUUID',
                                fieldType:'title',
                                checked: reportUUID == selectedReportUUID,
                                record:record
                            });
                    }
                });

                Ext.suspendLayouts();

                for (var key in allReports) {
                    if (allReports.hasOwnProperty(key)) {
                        var reportGroup = allReports[key];
                        me.addReportGroup(reportGroup);
                    }
                }
                Ext.resumeLayouts(true);
            });
        }

    },
    addReportGroup: function (reportGroup) {
        var me = this;
        var reportGroupsContainer = me.getReportGroupsContainer();

        var widget = Ext.widget('radio-group', {
            groupLabel: reportGroup.title,
            groupName: reportGroup.name,
            groupName: 'reportGroup',
            groupItems: reportGroup.reportsList,
            groupDisabled: reportGroup.disabled,
            groupSelected: reportGroup.checked,
            columnWidth: 0.5
        });

        reportGroupsContainer.add(widget);
    },

    loadReportFilters: function (reportUUID) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var step2 = me.getStep2();
        var step2Form = step2.down('form');
        step2Form.removeAll();
        var step3 = me.getStep3();
        var step3Form = step3.down('form');
        step3Form.removeAll();

        var reportFiltersStore = Ext.create('Yfn.store.ReportFilterInfos', {});
        if (reportFiltersStore) {
            var proxy = reportFiltersStore.getProxy();
            proxy.setExtraParam('reportUUID', reportUUID);
            step2Form.setLoading(true);
            step3Form.setLoading(true);
            reportFiltersStore.load(function (records) {
                var hasPrompts = false;
                var hasFilters = false;

                Ext.each(records, function (filterRecord) {
                    var filterOmittable = filterRecord.get('filterOmittable');
                    var filterType = filterRecord.get('filterType');
                    var filterName = filterRecord.get('filterName');
                    var filterDescription = filterRecord.get('filterDisplayName') || filterName;
                    var initialValue = router.queryParams[filterName];

                    var  formFields = me.createFilterControls(filterRecord, filterOmittable ? "filter": "prompt",initialValue );

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
                        padding:20,
                        items:formFields
                    };
                    if (filterOmittable) {
                        hasFilters = true;
                        step3Form.add(fieldContainer);
                    }
                    else {
                        hasPrompts = true;
                        step2Form.add(fieldContainer);
                    }
                });
                step3.down('#info-no-fields').setVisible(!hasFilters);
                step2.down('#info-no-fields').setVisible(!hasPrompts);
                step2Form.setLoading(false);
                step3Form.setLoading(false);
            });
        }

    },
    activateStep1: function (component) {
        console.log('Step 1 selected');
        return true;
    },
    activateStep2: function (component) {
        console.log('Step 2 selected');
        var me = this;
        var step1 = me.getStep1();
        var step1Form = step1.down('form').getForm();
        var step1Values = step1Form.getFieldValues();
        console.log(step1Values);
       // if (me.selectedReportUUID != step1Values.reportUUID) {
            me.selectedReportUUID = step1Values.reportUUID;
            me.loadReportFilters(me.selectedReportUUID);
        //}

        return true;

    },

    activateStep3: function (component) {
        console.log('Step 3 selected');
        return true;
    },
    activateStep4: function (component) {
        console.log('Step 4 selected');

        this.populateSummaryStep();
        return true;
    },
    createFilterControls: function (filterRecord, fieldType, initialValue) {
        var me = this;
        var filterType = filterRecord.get('filterType');
        var filterDisplayType = filterRecord.get('filterDisplayType');

        if(filterType == "INLIST" || filterType == "NOTINLIST")
            return me.createMultiSelectListControls(filterRecord, fieldType, initialValue);
        switch (filterDisplayType) {
            case "DATE":
                if(filterType == "BETWEEN")
                    return me.createDateBetweenControls(filterRecord, fieldType, initialValue);
                else
                    return me.createDateControls(filterRecord, fieldType, initialValue);
            case "TEXT":
                return me.createTextControls(filterRecord, fieldType, initialValue);
        }
    },
    createDateControls: function (filterRecord, fieldType, defaultValue) {

        var me = this;
        fieldType = fieldType || 'filter';
        var filterType = filterRecord.get('filterType');
        var filterName = filterRecord.get('filterName');

        var controls =
            [
                {
                    xtype: 'date-time',
                    padding: 0,
                    margin: 0,
                    name: filterName,
                    fieldType:fieldType,
                    record:filterRecord,
                    dateConfig:{
                        allowBlank: fieldType == 'filter'
                    },
                    dateTimeSeparatorConfig: {
                        margin: '0 50 0 10'
                    },
                    value:defaultValue ||  me.getDefaultDateValue(filterRecord.get('filterDefaultValue1')),
                    border: false,
                    getFieldValue : function (){
                        var rawValue = this.getValue();
                        if(rawValue)
                            return Ext.Date.format(rawValue,'c');
                        else
                            return null;
                    },
                    getFieldDisplayValue : function(){
                        var rawValue = this.getValue();
                        if(rawValue)
                            return Ext.Date.format(rawValue,'n/j/Y g:i A');
                        else
                            return "";
                    }
                },
            ];

        return controls;
    },
    createDateBetweenControls: function (filterRecord, fieldType, defaultValue) {
        var me = this;
        fieldType = fieldType || 'filter';
        var filterType = filterRecord.get('filterType');
        var filterName = filterRecord.get('filterName');

        var controls =
            [
                {
                    xtype: 'fieldset',
                    fieldType:fieldType,
                    record:filterRecord,
                    border: false,
                    margin:0,
                    padding:0,
                    defaults: {
                        border: false
                    },
                    items: [
                        {
                            xtype: 'date-time',
                            value: (defaultValue && defaultValue.from) || me.getDefaultDateValue(filterRecord.get('filterDefaultValue1')),
                            dateConfig:{
                                allowBlank: fieldType == 'filter'
                            },
                            dateTimeSeparatorConfig: {
                                margin: '0 50 0 10'
                            },
                            name: 'from'
                        },
                        {
                            xtype: 'date-time',
                            value:(defaultValue && defaultValue.to) ||  me.getDefaultDateValue(filterRecord.get('filterDefaultValue2')),
                            dateConfig:{
                                allowBlank: fieldType == 'filter'
                            },
                            dateTimeSeparatorConfig: {
                                margin: '0 50 0 10'
                            },
                            name: 'to'
                        }
                    ],
                    setValue : function(value){

                    },
                    getRawValue : function(){
                        return {
                            from:this.query('date-time')[0].getValue(),
                            to:this.query('date-time')[1].getValue()
                        };
                    },
                    getValue : function(){
                        return {
                            from:  Ext.Date.format(this.query('date-time')[0].getValue(),'c'),
                            to:Ext.Date.format(this.query('date-time')[1].getValue(),'c')
                        };
                    },
                    getFieldValue : function (){
                        return this.getValue();
                    },
                    getFieldDisplayValue : function(){
                        var rawValue = this.getRawValue();
                        return Ext.String.format("{0} - {1}",
                            rawValue.from ? Ext.Date.format(rawValue.from, 'n/j/Y g:i A') : '',
                            rawValue.to ? Ext.Date.format(rawValue.to, 'n/j/Y g:i A') : ''
                        );
                    }
                }
            ];
        return controls;
    },
    createTextControls: function (filterRecord, fieldType, defaultValue) {
        fieldType = fieldType || 'filter';
        var filterType = filterRecord.get('filterType');
        var filterName = filterRecord.get('filterName');

        var controls =
            [
                {
                    xtype: 'textfield',
                    fieldType:fieldType,
                    record:filterRecord,
                    value: defaultValue || filterRecord.get('filterDefaultValue1'),
                    name: filterName,
                    getFieldValue : function(){
                        return this.getValue();
                    },
                    getFieldDisplayValue : function(){
                        return this.getFieldValue();
                    }
                }
            ];
        return controls;
    },
    createMultiSelectListControls: function (filterRecord, fieldType, defaultValue) {
        var me = this;
        fieldType = fieldType || 'filter';
        var filterType = filterRecord.get('filterType');
        var filterName = filterRecord.get('filterName');

        var store =  Ext.create('Yfn.store.ReportFilterListItems',{});
        store.getProxy().setExtraParam('reportUUID', me.selectedReportUUID);
        store.getProxy().setExtraParam('filterId', filterRecord.get('id'));
        var controls =
            [
                {
                    xtype: 'uni-filter-combo',
                    name: filterName,
                    loadStore:true,
                    displayField: 'value1',
                    fieldType:fieldType,
                    record:filterRecord,
                    value: defaultValue || filterRecord.get('filterDefaultValue1').split('|'),
                    valueField: 'value2',
                    getFieldValue : function(){
                        return this.getValue();
                        /*if (!_.isEmpty(this.getRawValue())) {
                            return this.getRawValue();
                        }
                        return undefined;*/
                    },
                    getFieldDisplayValue : function(){
                        return this.getFieldValue();
                    },
                    store: store
                }
            ];
        return controls;
    },

    populateSummaryStep : function() {
        var me = this;

        var step4 = me.getStep4();
        var summaryContainer = step4.down('#step4-summary');
        summaryContainer.removeAll();
        me.selectedFilterValues = {};

        var reportRecord = me.reportsStore.findRecord("reportUUID", me.selectedReportUUID);

        //me.selectedFilterValues['__REPORT_UUID__'] = me.selectedReportUUID;

        summaryContainer.add(
            {
                xtype: 'displayfield',
                labelAlign: 'left',
                fieldLabel: Uni.I18n.translate('generatereport.reportNameTitle', 'YFN', 'Report'),
                value: reportRecord.get('name')
            });
        var hasPrompts = false;
        var hasFilters = false;

        var
        fieldsContainer = summaryContainer.add({
            xtype: 'fieldcontainer',
            labelAlign: 'left',
            fieldLabel: Uni.I18n.translate('generatereport.reportPromptsTitle', 'YFN', 'Prompts')
        });
        var prompts = me.getWizard().query('[fieldType = prompt]');

        for (var prompt in prompts) {
            if (prompts.hasOwnProperty(prompt)) {
                var field = prompts[prompt];
                fieldsContainer.add({
                    xtype: 'displayfield',
                    labelAlign: 'left',
                    labelWidth:150,
                    fieldLabel: field.record.get('filterDisplayName') + ' ' + me.translateFilterType(field.record.get('filterType')),
                    value: field.getFieldDisplayValue()
                });
                hasPrompts = true;
                me.selectedFilterValues[field.record.get('filterName')] = field.getFieldValue();
            }
        }

        if(!hasPrompts) {
            fieldsContainer.add({
                xtype: 'displayfield',
                value: Uni.I18n.translate('generatereport.reportNoFilters', 'YFN', 'No prompts')
            });
        }

        var filters = me.getWizard().query('[fieldType = filter]');

        fieldsContainer = summaryContainer.add({
            xtype: 'fieldcontainer',
            labelAlign: 'left',
            fieldLabel: Uni.I18n.translate('generatereport.reportFiltersTitle', 'YFN', 'Filters')
        });

        for (var filter in filters) {
            if (filters.hasOwnProperty(filter)) {
                var field = filters[filter];
                fieldsContainer.add({
                    xtype: 'displayfield',
                    labelAlign: 'left',
                    labelWidth:150,
                    fieldLabel: field.record.get('filterDisplayName') + ' ' + me.translateFilterType(field.record.get('filterType')),
                    value: field.getFieldDisplayValue()
                });
                var value = field.getFieldValue();
                if(value) {
                    me.selectedFilterValues[field.record.get('filterName')] = value;
                }
                hasFilters = true;
            }
        }

        if(!hasFilters) {
            fieldsContainer.add({
                xtype: 'displayfield',
                value: Uni.I18n.translate('generatereport.reportNoFilters', 'YFN', 'No filters')
            });
        }

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
    },
    getDefaultDateValue:function(defaultDateValue){
        defaultDateValue = defaultDateValue || "";
        defaultDateValue = defaultDateValue.split('|')[0];  //sometime wrong configured yellofin filter
                                                            // have default value as series of values separated by '|'
        if(defaultDateValue.indexOf('SYSDATE')>=0){ // resolve default values specified as relative date
            defaultDateValue = defaultDateValue.replace('SYSDATE', 'new Date().getTime()');
            defaultDateValue = eval('new Date('+defaultDateValue+'*86400000)');
            return Ext.Date.format(defaultDateValue,'Y-m-d');
        }
       return defaultDateValue;
    }
})
