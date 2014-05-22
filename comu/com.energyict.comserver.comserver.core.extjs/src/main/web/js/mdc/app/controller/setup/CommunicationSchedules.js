Ext.define('Mdc.controller.setup.CommunicationSchedules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
    ],

    views: [
        'Mdc.view.setup.communicationschedule.CommunicationSchedulesSetup',
        'Mdc.view.setup.communicationschedule.CommunicationSchedulesGrid',
        'Mdc.view.setup.communicationschedule.CommunicationSchedulePreview',
        'Mdc.view.setup.communicationschedule.CommunicationScheduleEdit',
        'Mdc.view.setup.communicationschedule.AddCommunicationTaskWindow'
    ],

    stores: [
        'CommunicationSchedules',
        'CommunicationTasks'
    ],

    refs: [
        {ref: 'communicationSchedulesGrid', selector: '#communicationSchedulesGrid'},
        {ref: 'communicationSchedulePreview', selector: '#communicationSchedulePreview'},
        {ref: 'communicationSchedulePreviewForm', selector: '#communicationSchedulePreviewForm'},
        {ref: 'communicationScheduleEditForm', selector: '#communicationScheduleEditForm'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'communicationTaskGrid', selector: '#communicationTaskGrid'}
    ],

    record: null,

    init: function () {
        this.control({
            '#CommunicationSchedulesSetup button[action = createCommunicationSchedule]': {
                click: this.createCommunicationScheduleHistory
            },
            '#communicationSchedulesGrid actioncolumn': {
                editItem: this.editCommunicationScheduleHistory,
                deleteItem: this.deleteCommunicationSchedule
            },
            '#communicationSchedulesGrid': {
                selectionchange: this.previewCommunicationSchedule
            },
            '#createEditButton[action=createCommunicationSchedule]': {
                click: this.saveCommunicationSchedule
            },
            '#createEditButton[action=editCommunicationSchedule]': {
                click: this.saveCommunicationSchedule
            },
            '#addCommunicationTaskButton[action=addCommunicationTask]': {
                click: this.addCommunicationTask
            },
            '#communicationSchedulePreview menuitem[action=deleteCommunicationSchedule]': {
                click: this.deleteCommunicationScheduleFromPreview
            },
            '#addCommunicationTaskButtonForm button[action=addAction]': {
                click: this.addCommunicationTasksToSchedule
            }


        });

    },

    showCommunicationSchedules: function(){
        var widget = Ext.widget('communicationSchedulesSetup');
        this.overviewBreadCrumb();
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    createCommunicationScheduleHistory: function(){
        location.href = '#/administration/communicationschedules/create';
    },

    editCommunicationScheduleHistory: function(record){
        location.href = '#/administration/communicationschedules/'+ record.get('id') +'/edit';
    },

    showCommunicationSchedulesEditView: function(id){
        console.log('edit');
        var me = this;
        var widget = Ext.widget('communicationScheduleEdit', {
            edit: id !== undefined,
            returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        if(id === undefined){
            this.record = Ext.create(Mdc.model.CommunicationSchedule);
            widget.down('#communicationScheduleEditCreateTitle').update('<h1>' + Uni.I18n.translate('communicationschedule.createCommunicationSchedule', 'MDC', 'Create communication schedule')  + '</h1>');
            this.createBreadCrumb();
        } else {
            Ext.ModelManager.getModel('Mdc.model.CommunicationSchedule').load(id, {
                success: function(communicationSchedule){
                    me.record = communicationSchedule;
                    me.createBreadCrumb(id);
                    widget.down('#communicationScheduleEditCreateTitle').update('<h1>' + Uni.I18n.translate('communicationschedule.editCommunicationSchedule', 'MDC', 'Edit') + ' ' + communicationSchedule.get('name') + '</h1>');
                    widget.down('#communicationScheduleEditForm').loadRecord(communicationSchedule);
                    widget.down('#communicationScheduleEditForm').down('#comTasksOnForm').reconfigure(communicationSchedule.comTaskUsages());
                },
                failure: function(){

                }
            });
        }



    },

    previewCommunicationSchedule: function(){
        var communicationSchedules = this.getCommunicationSchedulesGrid().getSelectionModel().getSelection();
        if (communicationSchedules.length == 1) {
            this.getCommunicationSchedulePreviewForm().loadRecord(communicationSchedules[0]);
            this.getCommunicationSchedulePreview().getLayout().setActiveItem(1);
        } else {
            this.getCommunicationSchedulePreview().getLayout().setActiveItem(0);
        }
    },

    saveCommunicationSchedule: function(){
        var me=this;
        var values = this.getCommunicationScheduleEditForm().getValues();
        if (this.record) {
            this.record.set(values);
            this.record.save({
                success: function (record) {
                    location.href = '#/administration/communicationschedules';
                },
                failure: function(record,operation){
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        Ext.each(json.errors,function(error){
                                if(error.id==='temporalExpression.every'){
                                    error.id = 'temporalExpression';
                                }
                        });
                        me.getCommunicationScheduleEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    addCommunicationTask: function(){
        var me=this,
            store = Ext.data.StoreManager.lookup('CommunicationTasks');
        store.load({
            callback: function(records){
                var widget = Ext.widget('addCommunicationTaskWindow');
                widget.show();
                widget.down('#communicationTaskGrid').getSelectionModel().select(me.record.comTaskUsages().data.items);

            }
        });
    },

    deleteCommunicationSchedule: function(communicationSchedule){
        this.showWarning(communicationSchedule)
    },

    showWarning: function(communicationSchedule){
        var me = this;
        Ext.MessageBox.show({
            msg: Uni.I18n.translate('communicationschedule..removeCommunicationSchedule', 'MDC', 'This schedule will no longer be available.'),
            title: Uni.I18n.translate('communicationschedule.delete', 'MDC', 'Remove') + ' ' + communicationSchedule.get('name') + '?',
            config: {
                communicationScheduleToDelete: communicationSchedule,
                me: me
            },
            buttons: Ext.MessageBox.YESNO,
            fn: me.removeCommunicationSchedule,
            icon: Ext.MessageBox.WARNING
        });
    },

    deleteCommunicationScheduleFromPreview: function () {
        this.showWarning(this.getCommunicationSchedulesGrid().getSelectionModel().getSelection()[0]);
    },


    removeCommunicationSchedule: function (btn, text, opt) {
        if (btn === 'yes') {
            var communicationScheduleToDelete = opt.config.communicationScheduleToDelete;
            var me = opt.config.me;
            communicationScheduleToDelete.destroy({
                callback: function () {
                    location.href = '#administration/communicationschedules';
                }
            });

        }
    },

    addCommunicationTasksToSchedule: function(){
        var selection = this.getCommunicationTaskGrid().getSelectionModel().getSelection();
        this.record.comTaskUsages().removeAll();
        this.record.comTaskUsages().add(selection);
        this.getCommunicationScheduleEditForm().down('#comTasksOnForm').reconfigure(this.record.comTaskUsages());
        this.getCommunicationTaskGrid().up('.window').close();
    },

    overviewBreadCrumb: function () {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('communicationschedule.communicationSchedules', 'MDC', 'Communication schedules'),
            href: 'communicationschedules'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#/administration'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    createBreadCrumb: function (id) {
        debugger;
        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: id===undefined?Uni.I18n.translate('communicationschedule.create', 'MDC', 'Create communication schedule'):Uni.I18n.translate('communicationschedule.edit', 'MDC', 'Edit communication schedule'),
            href: 'create'
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('communicationschedule.communicationSchedules', 'MDC', 'Communication schedules'),
            href: 'communicationschedules'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#/administration'
        });
        breadcrumbParent.setChild(breadcrumbChild).setChild(breadcrumbChild2);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    }




});
