Ext.define('Yfn.controller.setup.GenerateReportWizard', {
    extend: 'Ext.app.Controller',
    views: [
        'Yfn.view.generatereport.Step1',
        'Yfn.view.generatereport.Step2',
        'Yfn.view.generatereport.Step3',
        'Yfn.view.generatereport.Navigation',
        'Yfn.view.generatereport.Browse',
        'Yfn.view.generatereport.Wizard'
    ],
    requires: [
        'Yfn.privileges.Yellowfin',
        'Uni.view.window.Wizard',
        'Yfn.view.generatereport.RadioGroup',
        'Uni.form.filter.FilterCombobox',
        'Uni.form.field.DateTime',
        'Yfn.view.controls.MultiSelectCombo'
    ],

    stores: [
        'Yfn.store.ReportFilterInfos',
        'Yfn.store.ReportFilterListItems',
        'Yfn.store.ReportInfos',
        'Yfn.store.DeviceGroupInfos'
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
            ref: 'generateReportLink',
            selector: 'generatereport-wizard-step3 #wizard-generatereport-link'

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
                beforeactivate: this.activateStep2
            },
            'generatereport-wizard-step3': {
                activate: this.activateStep3
            }
        });
    },

    moveToStep: function (step) {
        var me = this;
        var layout = this.getWizard().getLayout();
        layout.setActiveItem(step - 1);
        me.updateButtonsState();
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
        var link = me.getGenerateReportLink();
        //me.generateReportWizardWidget.setLoading(Uni.I18n.translate('generatereport.preparingReport', 'YFN', 'Preparing report. Please wait ...'));

        var href = '#/reports/view?reportUUID='+me.selectedReportUUID+'&filter='+encodeURIComponent(Ext.JSON.encode(me.selectedFilterValues))+
            '&params='+encodeURIComponent(me.getController('Uni.controller.history.Router').queryParams.params) +
            '&search='+(me.getController('Uni.controller.history.Router').queryParams.search ? true:false);
        link.getEl().dom.href = href;
        link.getEl().dom.target = '_blank';
        link.getEl().dom.click();
        Ext.util.History.back();
        return;
    },

    cancelClick: function () {
        this.generateReportWizardWidget = null;
        Ext.util.History.back();
    },

    showGenerateReportWizard: function () {
        this.generateReportWizardWidget = Ext.widget('generatereport-browse');
        this.getApplication().fireEvent('changecontentevent', this.generateReportWizardWidget);
        this.loadReportTypes();
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
        var me = this;
        var step1 = me.getStep1();
        var step1Form = step1.down('form').getForm();
        var step1Values = step1Form.getFieldValues();
        if( !step1Values.reportUUID || step1Values.reportUUID.length == 0) {
            step1.down('#step1-generatereport-errors').setVisible(true);
            return false;
        }
        step1.down('#step1-generatereport-errors').setVisible(false);
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
            me.getWizard().setLoading(true);//Uni.I18n.translate('generatereport.loadingReports', 'YFN', 'Loading reports. Please wait ...'));
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
                        reportGroup.disabled = selectedReportGroup && (selectedReportGroup.toUpperCase() != subCategory.toUpperCase());

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

                //Ext.suspendLayouts();

                for (var key in allReports) {
                    if (allReports.hasOwnProperty(key)) {
                        var reportGroup = allReports[key];
                        me.addReportGroup(reportGroup);
                    }
                }
                me.getWizard().setLoading(false);
                //Ext.resumeLayouts(true);
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
            allowBlank:false,
            columnWidth: 0.5
        });

        reportGroupsContainer.add(widget);
    },

    loadReportFilters: function (reportUUID) {
        var me = this;

        var reportRecord = me.reportsStore.findRecord("reportUUID", reportUUID);

        var router = me.getController('Uni.controller.history.Router');
        var step2 = me.getStep2();
        var step2Form = step2.down('form');

        var reportDescription = step2.down('#report-description');

        reportDescription.setFieldLabel(Uni.I18n.translate('generatereport.reportNameTitle', 'YFN', 'Report')+': ' + reportRecord.get('name'));
        reportDescription.setValue( reportRecord.get('description'));


        var mandatoryFiltersPanel = step2.down('#report-mandatory-filters');
        mandatoryFiltersPanel.setVisible(false);
        mandatoryFiltersPanel.removeAll();
        var optionalFiltersPanel = step2.down('#report-optional-filters');
        optionalFiltersPanel.removeAll();
        optionalFiltersPanel.setVisible(false);
        var optionalFiltersPanelTitle = step2.down('#report-optional-filters-title');
        optionalFiltersPanelTitle.setVisible(false);


        me.reportFilters = Ext.JSON.decode(decodeURIComponent(router.queryParams.filter)) || {};



        var reportFiltersStore = Ext.create('Yfn.store.ReportFilterInfos', {});
        if (reportFiltersStore) {
            var proxy = reportFiltersStore.getProxy();
            proxy.setExtraParam('reportUUID', reportUUID);
            me.getWizard().setLoading(true);
            //Ext.suspendLayouts();
            reportFiltersStore.load(function (records) {
                var hasPrompts = false;
                var hasFilters = false;

                Ext.each(records, function (filterRecord) {
                    var filterType = filterRecord.get('filterType');
                    var filterName = filterRecord.get('filterName');
                    var filterOmittable = filterRecord.get('filterOmittable');// && filterName != 'GROUPNAME';
                    var filterDescription = filterRecord.get('filterDisplayName') || filterName;
                    var initialValue = me.reportFilters[filterName];

                    var  formFields = me.createFilterControls(filterRecord, filterOmittable ? "filter": "prompt",initialValue );

                    formFields = Ext.isArray(formFields) ? formFields : [formFields];

                    formFields.unshift({
                        xtype: 'container',
                        itemId:'report-mandatory-filters-title',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        padding: '0 0 20 0',
                        items: [
                            {
                                xtype: 'label',
                                itemId: 'report-mandatory-filters-label',
                                text: filterDescription + ' ' + me.translateFilterType(filterType)
                            },
                            {
                                xtype: 'button',
                                hidden: filterOmittable,
                                iconCls: 'uni-form-item-label-required',
                                ui: 'blank',
                                disabled: true,
                                shadow: false,
                                margin: '5 0 0 0',
                                width: 16,
                                tabIndex: -1
                            }

                        ]
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
                        optionalFiltersPanel.add(fieldContainer);
                        optionalFiltersPanel.setVisible(true);
                        optionalFiltersPanelTitle.setVisible(true);
                    }
                    else {
                        hasPrompts = true;
                        mandatoryFiltersPanel.add(fieldContainer);
                        mandatoryFiltersPanel.setVisible(true);
                    }
                });
                me.getWizard().setLoading(false);
                // Ext.resumeLayouts(true);
            });

        }

    },
    activateStep1: function (component) {
        var me = this;
        me.selectedReportUUID = null;
        return true;
    },
    activateStep2: function (component) {
        var me = this;
        var step2 = me.getStep2();
        var me = this;
        var step1 = me.getStep1();
        var step1Form = step1.down('form').getForm();
        var step1Values = step1Form.getFieldValues();
        if( step1Values.reportUUID) {
            if (me.selectedReportUUID != step1Values.reportUUID) {
                me.selectedReportUUID = step1Values.reportUUID;
                me.loadReportFilters(me.selectedReportUUID);
            }
            return true;
        }
        me.moveToStep(1);
        return false;
    },

    activateStep3: function (component) {
        this.populateSummaryStep();
        return true;
    },
    createFilterControls: function (filterRecord, fieldType, initialValue) {
        var me = this;
        var filterType = filterRecord.get('filterType');
        var filterDisplayType = filterRecord.get('filterDisplayType');
        var filterAllowPrompt = filterRecord.get('filterAllowPrompt');


        if(filterType == "INLIST" || filterType == "NOTINLIST")
            return me.createMultiSelectListControls(filterRecord, fieldType, initialValue);
        switch (filterDisplayType) {
            case "TIMESTAMP":
                if((filterType == "BETWEEN") || (filterType == "NOTBETWEEN"))
                    return me.createDateTimeBetweenControls(filterRecord, fieldType, initialValue);
                else
                    return me.createDateTimeControls(filterRecord, fieldType, initialValue);
            case "DATE":
                if((filterType == "BETWEEN") || (filterType == "NOTBETWEEN"))
                    return me.createDateBetweenControls(filterRecord, fieldType, initialValue);
                else
                    return me.createDateControls(filterRecord, fieldType, initialValue);
            case "TEXT":
                if(filterAllowPrompt)
                    return me.createSingleSelectListControls(filterRecord, fieldType, initialValue);
            default:
                return me.createTextControls(filterRecord, fieldType, initialValue);
        }
    },
    createDateTimeControls: function (filterRecord, fieldType, defaultValue) {

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
                        allowBlank: fieldType == 'filter'//,
                        //disabled: fieldType != 'filter' && defaultValue
                    },
                    dateTimeSeparatorConfig: {
                        margin: '0 50 0 10'//,
                        //disabled:fieldType != 'filter' && defaultValue
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
                }
            ];

        return controls;
    },
    createDateControls: function (filterRecord, fieldType, defaultValue) {

        var me = this;
        fieldType = fieldType || 'filter';
        var filterType = filterRecord.get('filterType');
        var filterName = filterRecord.get('filterName');
        var date = (defaultValue && defaultValue.from) || me.getDefaultDateValue(filterRecord.get('filterDefaultValue1'));
        defaultValue = date ? moment(date).startOf('day').toDate() : null;

        var controls =
            [
                {
                    xtype: 'datefield',
                    padding: 0,
                    margin: 0,
                    width: '100%',
                    name: filterName,
                    fieldType:fieldType,
                    record:filterRecord,
                    allowBlank: fieldType == 'filter',
                    value: defaultValue,
                    //disabled:fieldType != 'filter' && defaultValue,
                    initC3omponent: function () {
                        this.callParent(arguments);

                        if (this.value) {
                            this.setValue(this.value);
                        }
                    },
                    getFieldValue : function (){
                        var rawValue = this.getValue();
                        if(rawValue)
                            return Ext.Date.format(rawValue,'Y-m-d');
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
                }
            ];

        return controls;
    },
    createDateTimeBetweenControls: function (filterRecord, fieldType, defaultValue) {
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
                            //disabled:fieldType != 'filter' && defaultValue,
                            value: (defaultValue && defaultValue.from) || me.getDefaultDateValue(filterRecord.get('filterDefaultValue1')),
                            dateConfig:{
                                allowBlank: fieldType == 'filter'
                            },
                            dateTimeSeparatorConfig: {
                                margin: '0 50 0 10'//,
                                //disabled:fieldType != 'filter' && defaultValue
                            },
                            name: 'from'
                        },
                        {
                            xtype: 'date-time',
                            //disabled:fieldType != 'filter' && defaultValue,
                            value:(defaultValue && defaultValue.to) ||  me.getDefaultDateValue(filterRecord.get('filterDefaultValue2')),
                            dateConfig:{
                                allowBlank: fieldType == 'filter'
                            },
                            dateTimeSeparatorConfig: {
                                margin: '0 50 0 10'//,
                                //disabled:fieldType != 'filter' && defaultValue
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
                            from:  Ext.Date.format(this.query('date-time')[0].getValue(),'Y-m-d H:i:s'),
                            to:Ext.Date.format(this.query('date-time')[1].getValue(),'Y-m-d H:i:s')
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
    createDateBetweenControls: function (filterRecord, fieldType, defaultValue) {
        var me = this;
        fieldType = fieldType || 'filter';
        var filterType = filterRecord.get('filterType');
        var filterName = filterRecord.get('filterName');
        var fromDate = (defaultValue && defaultValue.from) || me.getDefaultDateValue(filterRecord.get('filterDefaultValue1'));
        fromDate = fromDate ? moment(fromDate).startOf('day').toDate() : null;
        var toDate = (defaultValue && defaultValue.to) ||  me.getDefaultDateValue(filterRecord.get('filterDefaultValue2'));
        toDate = toDate ? moment(toDate).startOf('day').toDate() : null;

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
                            xtype: 'datefield',
                            allowBlank: fieldType == 'filter',
                            value: fromDate,
                            width: '100%',
                            name: 'from',
                            //disabled:fieldType != 'filter' && defaultValue,
                            initC3omponent: function () {
                                this.callParent(arguments);

                                if (this.value) {
                                    this.setValue(this.value);
                                }
                            }
                        },
                        {
                            xtype: 'datefield',
                            value : toDate,
                            //disabled:fieldType != 'filter' && defaultValue,
                            allowBlank: fieldType == 'filter',
                            width: '100%',
                            name: 'to',
                            initC3omponent: function () {
                                this.callParent(arguments);

                                if (this.value) {
                                    this.setValue(this.value);
                                }
                            }
                        }
                    ],
                    setValue : function(value){

                    },
                    getRawValue : function(){
                        return {
                            from:this.query('datefield')[0].getValue(),
                            to:this.query('datefield')[1].getValue()
                        };
                    },
                    getValue : function(){
                        return {
                            from:  Ext.Date.format(this.query('datefield')[0].getValue(),'Y-m-d'),
                            to:Ext.Date.format(this.query('datefield')[1].getValue(),'Y-m-d')
                        };
                    },
                    getFieldValue : function (){
                        return this.getValue();
                    },
                    getFieldDisplayValue : function(){
                        var rawValue = this.getRawValue();
                        return Ext.String.format("{0} - {1}",
                            rawValue.from ? Ext.Date.format(rawValue.from, 'n/j/Y') : '',
                            rawValue.to ? Ext.Date.format(rawValue.to, 'n/j/Y') : ''
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
                    allowBlank: fieldType == 'filter',
                    value: defaultValue || filterRecord.get('filterDefaultValue1'),
                    //disabled:fieldType != 'filter' && defaultValue,
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
        var filterOmittable = filterRecord.get('filterOmittable');

        defaultValue = Ext.isString(defaultValue) && defaultValue.split(', ') || defaultValue ;
        defaultValue = _.isArray(defaultValue) && _.compact(defaultValue) || defaultValue;

        var store =  filterName.toUpperCase() == 'GROUPNAME'
            ? Ext.create('Yfn.store.DeviceGroupInfos',{})
            : Ext.create('Yfn.store.ReportFilterListItems',{});
        store.getProxy().setExtraParam('reportUUID', me.selectedReportUUID);
        store.getProxy().setExtraParam('filterId', filterRecord.get('id'));

        var storeLoaded = false;
        if( !filterOmittable && _.isArray(defaultValue)){

            for(var i=0;i<defaultValue.length;i++){
                storeLoaded = true;
                store.add(
                    {
                        value1: defaultValue[i],
                        value2: defaultValue[i],
                        name: defaultValue[i]
                    });
            }
        }

        var controls =
            [
                {
                    xtype: 'multiselect-combo',
                    //xtype: 'uni-filter-combo',
                    name: filterName,
                    loadStore:!storeLoaded,
                    displayField: 'value1',
                    allowBlank: fieldType == 'filter',
                    fieldType:fieldType,
                    record:filterRecord,
                    //disabled:fieldType != 'filter' && defaultValue,
                    value: defaultValue || (filterRecord.get('filterDefaultValue1').length ? filterRecord.get('filterDefaultValue1').split('|'): null),
                    valueField: 'value2',
                    store: store,
                    getFieldValue : function(){
                        return this.getValue();
                    },
                    getFieldDisplayValue : function(){
                        var value = this.getFieldValue();
                        if(value && value.toString().indexOf("__##SEARCH_RESULTS##__")!=-1){
                            return Uni.I18n.translate('generatereport.searchResults', 'YFN', 'Search results')
                        }
                        return value;
                    }
                }
            ];
        return controls;
    },
    createSingleSelectListControls: function (filterRecord, fieldType, defaultValue) {
        var me = this;
        fieldType = fieldType || 'filter';
        var filterType = filterRecord.get('filterType');
        var filterName = filterRecord.get('filterName');

        var store =  filterName.toUpperCase() == 'GROUPNAME'
            ? Ext.create('Yfn.store.DeviceGroupInfos',{})
            : Ext.create('Yfn.store.ReportFilterListItems',{});

        store.getProxy().setExtraParam('reportUUID', me.selectedReportUUID);
        store.getProxy().setExtraParam('filterId', filterRecord.get('id'));
        var controls =
            [
                {
                    xtype: 'combobox',
                    name: filterName,
                    loadStore:true,
                    displayField: 'value1',
                    allowBlank: fieldType == 'filter',
                    fieldType:fieldType,
                    record:filterRecord,
                    //disabled:fieldType != 'filter' && defaultValue,
                    value: defaultValue || (filterRecord.get('filterDefaultValue1').length ? filterRecord.get('filterDefaultValue1').split('|'): null),
                    valueField: 'value2',
                    store: store,
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

    populateSummaryStep : function() {
        var me = this;

        var step3 = me.getStep3();
        var summaryContainer = step3.down('#wizard-summary');
        summaryContainer.removeAll();
        me.selectedFilterValues = {};

        var reportRecord = me.reportsStore.findRecord("reportUUID", me.selectedReportUUID);

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
                labelStyle: 'color:#cccccc',
                fieldLabel: Uni.I18n.translate('generatereport.wizard.mandatoryFilters', 'YFN', 'Mandatory filters')
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
                value: Uni.I18n.translate('generatereport.reportNoMandatoryFilters', 'YFN', 'No mandatory filters')
            });
        }

        var filters = me.getWizard().query('[fieldType = filter]');

        fieldsContainer = summaryContainer.add({
            xtype: 'fieldcontainer',
            labelAlign: 'left',
            labelStyle: 'color:#cccccc',
            fieldLabel: Uni.I18n.translate('generatereport.wizard.optionalFilters', 'YFN', 'In report filters')
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
                value: Uni.I18n.translate('generatereport.reportNoOptionalFilters', 'YFN', 'No optional filters')
            });
        }

    },
    translateFilterType : function(filterType){
        switch(filterType){
            case 'EQUAL': return Uni.I18n.translate('generatereport.filterTypeEQUAL', 'YFN', 'Equal to');
            case 'NOTEQUAL': return Uni.I18n.translate('generatereport.filterTypeNOTEQUAL', 'YFN', 'Different from');
            case 'GREATER': return Uni.I18n.translate('generatereport.filterTypeGREATER', 'YFN', 'Greater than');
            case 'GREATEREQUAL': return Uni.I18n.translate('generatereport.filterTypeGREATEREQUAL', 'YFN', 'Greater than or equal to');
            case 'LESS': return Uni.I18n.translate('generatereport.filterTypeLESS', 'YFN', 'Less than');
            case 'LESSEQUAL': return Uni.I18n.translate('generatereport.filterTypeLESSEQUAL', 'YFN', 'Less than or equal to');
            case 'BETWEEN': return Uni.I18n.translate('generatereport.filterTypeBETWEEN', 'YFN', 'Between');
            case 'NOTBETWEEN': return Uni.I18n.translate('generatereport.filterTypeNOTBETWEEN', 'YFN', 'Not Between');
            case 'INLIST': return Uni.I18n.translate('generatereport.filterTypeINLIST', 'YFN', 'In List');
            case 'NOTINLIST': return Uni.I18n.translate('generatereport.filterTypeNOTINLIST', 'YFN', 'Not In List');
            case 'ISNULL': return Uni.I18n.translate('generatereport.filterTypeISNULL', 'YFN', 'Is Null');
            case 'ISNOTNULL': return Uni.I18n.translate('generatereport.filterTypeISNOTNULL', 'YFN', 'Is Not Null');
            case 'EQUALCOLUMN': return Uni.I18n.translate('generatereport.filterTypeEQUALCOLUMN', 'YFN', 'Equals Column');
            case 'NOTEQUALCOLUMN': return Uni.I18n.translate('generatereport.filterTypeNOTEQUALCOLUMN', 'YFN', 'Different from Column');
            case 'GREATERCOLUMN': return Uni.I18n.translate('generatereport.filterTypeGREATERCOLUMN', 'YFN', 'Greater than Column');
            case 'GREATEREQUALCOLUMN': return Uni.I18n.translate('generatereport.filterTypeGREATEREQUALCOLUMN', 'YFN', 'Greater than or Equal to Column');
            case 'LESSCOLUMN': return Uni.I18n.translate('generatereport.filterTypeLESSCOLUMN', 'YFN', 'Less than Column');
            case 'LESSEQUALCOLUMN': return Uni.I18n.translate('generatereport.filterTypeLESSEQUALCOLUMN', 'YFN', 'Less than or Equal to Column');
            case 'MINIMUMDATE': return Uni.I18n.translate('generatereport.filterTypeMINIMUMDATE', 'YFN', 'Minimum Date');
            case 'MAXIMUMDATE': return Uni.I18n.translate('generatereport.filterTypeMAXIMUMDATE', 'YFN', 'Maximum Date');
            case 'LINKFILTER': return Uni.I18n.translate('generatereport.filterTypeLINKFILTER', 'YFN', 'Link to Filter');
            case 'CONTAINS': return Uni.I18n.translate('generatereport.filterTypeCONTAINS', 'YFN', 'Contains');
            case 'NOTCONTAINS': return Uni.I18n.translate('generatereport.filterTypeNOTCONTAINS', 'YFN', 'Does not contain');
            default: return filterType;
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
