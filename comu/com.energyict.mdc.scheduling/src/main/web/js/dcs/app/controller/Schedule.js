Ext.define('Dcs.controller.Schedule', {
    extend: 'Ext.app.Controller',

    stores: [
        'DataCollectionSchedules'
    ],

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
    ],

    models: [
        'DataCollectionSchedule'
    ],

    views: [
        'scheduling.DataCollectionScheduleBrowse'
    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'dataCollectionScheduleList', selector: 'dataCollectionScheduleList'},
        {ref: 'dataCollectionSchedulePreview', selector: 'dataCollectionSchedulePreview'},
        {ref: 'dataCollectionScheduleForm',selector: 'dataCollectionSchedulePreview #dataCollectionScheduleForm'},
        {ref: 'dataCollectionSchedulePreviewTitle',selector: 'dataCollectionSchedulePreview #dataCollectionSchedulePreviewTitle'}
    ],

    init: function () {
        this.initMenu();

        this.control(
            {
            '#dataCollectionScheduleList': {
                selectionchange: this.previewDataCollectionSchedule
            }

        });
    },

    initMenu: function () {

    },


    showDataCollectionSchedules: function () {
        this.initMenu();
        var widget = Ext.widget('dataCollectionScheduleBrowse');
        this.getApplication().getController('Dcs.controller.Main').showContent(widget);
        this.createRuleSetsBreadCrumbs();
    },

    createRuleSetsBreadCrumbs: function() {
        var me = this;

        var breadcrumbs = me.getBreadCrumbs();

        var breadcrumbDataCollectionSchedules = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Data collection schedules'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Administration',
            href: '#administration_dcs'
        });
        breadcrumbParent.setChild(breadcrumbDataCollectionSchedules);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);

    },

    previewDataCollectionSchedule: function (grid, record) {
        var selectedDataCollectionSchedules = this.getDataCollectionScheduleList().getSelectionModel().getSelection();
        if (selectedDataCollectionSchedules.length == 1) {
            this.getDataCollectionScheduleForm().loadRecord(selectedDataCollectionSchedules[0]);
            var dataCollectionScheduleName = this.getDataCollectionScheduleForm().form.findField('name').getSubmitValue();
            this.getDataCollectionSchedulePreview().getLayout().setActiveItem(1);
            this.getDataCollectionSchedulePreviewTitle().update('<h4>' + dataCollectionScheduleName + '</h4>');
        } else {
            this.getDataCollectionSchedulePreview().getLayout().setActiveItem(0);
        }
    }


});
