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
        'Mdc.view.setup.communicationSchedule.CommunicationScheduleEdit'
    ],

    stores: [
        'CommunicationSchedules'
    ],

    refs: [
        {ref: 'communicationSchedulesGrid', selector: '#communicationSchedulesGrid'},
        {ref: 'communicationSchedulePreview', selector: '#communicationSchedulePreview'},
        {ref: 'communicationSchedulePreviewForm', selector: '#communicationSchedulePreviewForm'},
        {ref: 'communicationScheduleEditForm', selector: '#communicationScheduleEditForm'}
    ],

    init: function () {
        this.control({
            '#CommunicationSchedulesSetup button[action = createCommunicationSchedule]': {
                click: this.createCommunicationScheduleHistory
            },
            '#communicationSchedulesGrid actioncolumn': {
//                editItem: this.editDeviceTypeHistory,
//                deleteItem: this.deleteDeviceType
            },
            '#communicationSchedulesGrid': {
                selectionchange: this.previewCommunicationSchedule
            },
            '#createEditButton[action=createCommunicationSchedule]': {
                click: this.createCommunicationSchedule
            }
        });

    },

    showCommunicationSchedules: function(){
        var widget = Ext.widget('communicationSchedulesSetup');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    createCommunicationScheduleHistory: function(){
        location.href = '#setup/communicationschedules/create';
    },

    showCommunicationSchedulesEditView: function(){
        var me = this;
        var widget = Ext.widget('communicationScheduleEdit', {
            edit: false,
            returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    previewCommunicationSchedule: function(){
        console.log('flflsdflfjkdfjkldjkfmldkdjfsdjjdqsfjklsdjkdjklfsdf');
        var communicationSchedules = this.getCommunicationSchedulesGrid().getSelectionModel().getSelection();
        if (communicationSchedules.length == 1) {
            this.getCommunicationSchedulePreviewForm().loadRecord(communicationSchedules[0]);
            this.getCommunicationSchedulePreview().getLayout().setActiveItem(1);
        } else {
            this.getCommunicationSchedulePreview().getLayout().setActiveItem(0);
        }
    },

    createCommunicationSchedule: function(){
        var me=this;
        var record = Ext.create(Mdc.model.CommunicationSchedule),
            values = this.getCommunicationScheduleEditForm().getValues();
        if (record) {
            record.set(values);
            debugger;
//            record.save({
//                success: function (record) {
//                    location.href = '#setup/devicetypes/' + me.deviceTypeId + /deviceconfigurations/ + record.get('id');
//                },
//                failure: function(record,operation){
//                    var json = Ext.decode(operation.response.responseText);
//                    if (json && json.errors) {
//                        me.getDeviceConfigurationEditForm().getForm().markInvalid(json.errors);
//                    }
//                }
//            });

        }
    }




});
