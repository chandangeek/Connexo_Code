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
        'Dsh.view.widget.common.SideFilterCombo',
        'Dsh.view.widget.common.SideFilterDateTime',
        'Dsh.view.widget.common.DateTimeField',
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
            ref: 'step1FormErrorMessage',
            selector: 'devicegroup-wizard-step1 #step1-adddevicegroup-errors'
        },
        {
            ref: 'step1FormNameErrorMessage',
            selector: 'devicegroup-wizard-step1 #step1-adddevicegroup-name-errors'
        },
        {
            ref: 'step2FormErrorMessage',
            selector: 'devicegroup-wizard-step2 uni-form-error-message'
        }
    ],

    generateReportWizardWidget: null,
    selectedReportID:null,

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


            'generatereport-wizard-step1':{
                activate:this.activateStep1
            },
            'generatereport-wizard-step2':{
                activate:this.activateStep2
            },
            'generatereport-wizard-step3':{
                activate:this.activateStep3
            },
            'generatereport-wizard-step4':{
                activate:this.activateStep4
            }
        });
    },

    moveToStep : function(step){
        var layout = this.getWizard().getLayout();
        layout.setActiveItem(step-1);
    },
    backClick: function () {
        var layout = this.getWizard().getLayout(),
            currentCmp = layout.getActiveItem();
        this.getNavigationMenu().movePrevStep();
        this.changeContent(layout.getPrev(), currentCmp);
    },

    nextClick: function () {
        var layout = this.getWizard().getLayout(),
            currentCmp = layout.getActiveItem();

        //this.getStep1FormErrorMessage().setVisible(false);
        //this.getStep1FormNameErrorMessage().setVisible(false);
        this.getNavigationMenu().moveNextStep();
        this.changeContent(layout.getNext(), layout.getActiveItem());
        //this.getStep2FormErrorMessage().setVisible(false);
    },

    confirmClick: function () {

    },

    finishClick: function () {

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

    loadReportTypes : function(){
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var selectedReportUUID = router.queryParams.reportUUID;
        var selectedReportGroup = router.queryParams.subCategory;


        //var reportsStore = Ext.getStore('ReportInfos');
        var reportsStore = Ext.create('Yfn.store.ReportInfos',{});
        if(reportsStore) {
            var proxy = reportsStore.getProxy();
            delete proxy.extraParams.category;
            delete proxy.extraParams.subCategory;
            proxy.setExtraParam('category', 'MDC');
            //proxy.setExtraParam('subCategory', 'Device Connections');
            reportsStore.load(function (records) {
                var allReports = {};
                Ext.each(records, function (record) {
                    var reportDescription = record.get('description');
                    var reportName = record.get('name');
                    var reportUUID = record.get('reportUUID');
                    var subCategory = record.get('subCategory');

                    var reportGroup = allReports[subCategory];
                    if(!reportGroup){
                        allReports[subCategory] = {checked:false, reportsList:[]};
                        reportGroup = allReports[subCategory];
                        reportGroup.title = subCategory;
                        reportGroup.name = reportName;
                        reportGroup.disabled = selectedReportGroup && selectedReportGroup != subCategory;

                    }
                    if(reportUUID == selectedReportUUID){
                        reportGroup.checked = true;
                    }
                    if(reportName && reportName.length) {
                        reportGroup.reportsList.push(
                            {
                                boxLabel: reportName,
                                tooltip: reportDescription,
                                inputValue: reportUUID,
                                name:'reportUUID',
                                checked: reportUUID == selectedReportUUID
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
    addReportGroup : function(reportGroup){
        var me = this;
        var reportGroupsContainer = me.getReportGroupsContainer();

        var widget = Ext.widget('radio-group', {
            groupLabel: reportGroup.title,
            groupName:reportGroup.name,
            groupName:'reportGroup',
            groupItems:reportGroup.reportsList,
            groupDisabled:reportGroup.disabled,
            groupSelected:reportGroup.checked,
            columnWidth: 0.5
        });

        reportGroupsContainer.add(widget);
    },

    loadReportFilters:function(reportUUID){
        var me = this;
        var step2 = me.getStep2();
        var step2Form = step2.down('form');
        step2Form.removeAll();
        var step3 = me.getStep3();
        var step3Form = step3.down('form');
        step3form.removeAll();


        var reportFiltersStore = Ext.create('Yfn.store.ReportFilterInfos',{});
        if(reportFiltersStore) {
            var proxy = reportFiltersStore.getProxy();
            proxy.setExtraParam('reportUUID', reportUUID);
            reportFiltersStore.load(function(records){
                Ext.each(records, function (record) {

                    var filterType = record.get('filterType');
                    var filterName = record.get('filterName');
                    var filterDisplayType = record.get('filterDisplayType');
                    var filterOmittable = record.get('filterOmittable');
                    var formField = 


                });
            });
        }

        step2Form.add({
            xtype: 'side-filter-combo',
            itemId: 'device-groups',
            wTitle: Uni.I18n.translate('generate.widget.sideFilter.finishedBetween', 'YFN', 'Select a period'),
            name: 'deviceGroup',
            displayField: 'name',
            valueField: 'id',
            store: 'Dsh.store.filter.DeviceGroup',
            columnWidth: 0.5,
            maxWidth: 250
        });

        step2Form.add({
            xtype: 'side-filter-date-time',
            itemId: 'finished-between',
            wTitle: Uni.I18n.translate('generate.widget.sideFilter.finishedBetween', 'YFN', 'Select a period'),
            name: 'finishedBetween',
            columnWidth: 0.5,
            maxWidth: 250
        });

        step2Form.add({
            xtype: 'datetime-field',
            itemId: 'one',
            //label: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'YFN', 'Select a date'),
            hideEmptyLabel: false,
            padding: 0,
            margin: 0,
            name: 'finishedBetween',
            border:false,
            columnWidth: 0.5,
            maxWidth: 250
        });


        /*

        xtype: 'side-filter-combo',
            labelAlign: 'top'
    },
        items: [
            {
                itemId: 'device-group',
                name: 'deviceGroup',
                fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceGroup', 'DSH', 'Device group'),
                displayField: 'name',
                valueField: 'id',
                store: 'Dsh.store.filter.DeviceGroup'
            },

        {
            xtype: 'side-filter-date-time',
                itemId: 'finished-between',
            name: 'finishedBetween',
            wTitle: Uni.I18n.translate('connection.widget.sideFilter.finishedBetween', 'DSH', 'Finished successfully between')
        }

        */


    },
    activateStep1 : function(component){
        console.log('Step 1 selected');
    },
    activateStep2 : function(component){
        console.log('Step 2 selected');
        var me = this;
        var step1 = me.getStep1();
        var step1Form = step1.down('form').getForm();
        var step1Values = step1Form.getFieldValues();
        console.log(step1Values);
        if(me.selectedReportID != step1Values.reportUUID){
            me.selectedReportID = step1Values.reportUUID;
            me.loadReportFilters(me.selectedReportID);
        }

    },

    activateStep3 : function(component){
        console.log('Step 3 selected');
    },
    activateStep4 : function(component){
        console.log('Step 4 selected');
    }

})
