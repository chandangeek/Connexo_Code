Ext.define('Mdc.controller.setup.CommunicationSchedules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification'
    ],

    views: [
        'Mdc.view.setup.communicationschedule.CommunicationSchedulesSetup',
        'Mdc.view.setup.communicationschedule.CommunicationSchedulesGrid',
        'Mdc.view.setup.communicationschedule.CommunicationSchedulePreview',
        'Mdc.view.setup.communicationschedule.CommunicationScheduleEdit',
        'Mdc.view.setup.communicationschedule.AddCommunicationTaskWindow',
        'Mdc.view.setup.communicationschedule.AddCommunicationTaskPreview'
    ],

    stores: [
        'CommunicationSchedules',
        'CommunicationTasks'
    ],

    refs: [
        {ref: 'communicationScheduleEdit', selector: '#communicationScheduleEdit'},
        {ref: 'communicationSchedulesGrid', selector: '#communicationSchedulesGrid'},
        {ref: 'communicationSchedulePreview', selector: '#communicationSchedulePreview'},
        {ref: 'communicationSchedulePreviewForm', selector: '#communicationSchedulePreviewForm'},
        {ref: 'communicationScheduleEditForm', selector: '#communicationScheduleEditForm'},
        {ref: 'communicationTaskGrid', selector: '#communicationTaskGridFromSchedule'},
        {ref: 'comTaskPanel', selector: '#comTaskPanel'},
        {ref: 'addCommunicationTaskPreview', selector: '#addCommunicationTaskPreview'},
        {ref: 'comTaskCommands', selector: '#comtaskCommands'}
    ],

    record: null,

    init: function () {
        this.control({
            '#CommunicationSchedulesSetup button[action = createCommunicationSchedule]': {
                click: this.createCommunicationScheduleHistory
            },
            '#communicationSchedulesGrid actioncolumn': {
                editCommunicationSchedule: this.editCommunicationScheduleHistory,
                deleteCommunicationSchedule: this.deleteCommunicationSchedule
            },
            '#comTasksOnForm actioncolumn': {
                deleteComTask: this.deleteComTask
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
            '#communicationSchedulePreview menuitem[action=editCommunicationSchedule]': {
                click: this.editCommunicationScheduleHistory
            },
            '#addCommunicationTaskButtonForm button[action=addAction]': {
                click: this.addCommunicationTasksToSchedule
            }, '#addCommunicationTaskButtonForm button[action=cancelAction]': {
                click: this.cancelAddCommunicationTasksToSchedule
            },
            '#communicationTaskGridFromSchedule': {
                selectionchange: this.previewComTask
            }
        });

    },

    showCommunicationSchedules: function () {
        var me = this;
        var store = Ext.data.StoreManager.lookup('CommunicationSchedules');
        store.load({
            callback: function (records) {
                var widget = Ext.widget('communicationSchedulesSetup');
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getCommunicationSchedulesGrid().getSelectionModel().doSelect(0);

            }
        });
    },

    createCommunicationScheduleHistory: function () {
        location.href = '#/administration/communicationschedules/create';
    },

    editCommunicationScheduleHistory: function (record) {

        location.href = '#/administration/communicationschedules/' + this.getCommunicationSchedulesGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    showCommunicationSchedulesEditView: function (id) {
        var me = this;
        var widget = Ext.widget('communicationScheduleEdit', {
            edit: id !== undefined,
            returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.down('#card').getLayout().setActiveItem(0);
        if (id === undefined) {
            this.record = Ext.create(Mdc.model.CommunicationSchedule);
            widget.down('#communicationScheduleEditCreateTitle').update('<h1>' + Uni.I18n.translate('communicationschedule.addCommunicationSchedule', 'MDC', 'Add communication schedule') + '</h1>');
            me.initComTaskStore();
        } else {
            Ext.ModelManager.getModel('Mdc.model.CommunicationSchedule').load(id, {
                success: function (communicationSchedule) {
                    me.getApplication().fireEvent('loadCommunicationSchedule', communicationSchedule);
                    me.record = communicationSchedule;
                    widget.down('#communicationScheduleEditCreateTitle').update('<h1>' + Uni.I18n.translate('communicationschedule.editCommunicationSchedule', 'MDC', 'Edit') + ' ' + communicationSchedule.get('name') + '</h1>');
                    widget.down('#communicationScheduleEditForm').loadRecord(communicationSchedule);
                    me.getComTaskPanel().getLayout().setActiveItem(1);
                    widget.down('#communicationScheduleEditForm').down('#comTasksOnForm').reconfigure(communicationSchedule.comTaskUsages());
                    me.initComTaskStore(communicationSchedule.comTaskUsages());
                },
                failure: function () {

                }
            });
        }

    },

    initComTaskStore: function (toRemove) {
        this.comTaskStore = Ext.data.StoreManager.lookup('CommunicationTasks');
        if (this.record.get('id') === null) {
            this.comTaskStore.setProxy({
                type: 'rest',
                url: '../../api/cts/comtasks',
                reader: {
                    type: 'json',
                    root: 'data'
                }
            });
        } else {
            this.comTaskStore.setProxy({
                type: 'rest',
                url: '../../api/scr/schedules/{id}/comTasks',
                reader: {
                    type: 'json',
                    root: 'data'
                }
            });
            this.comTaskStore.getProxy().setExtraParam('id', this.record.get('id'));
            this.comTaskStore.getProxy().setExtraParam('filter', Ext.encode([
                {
                    property: 'available',
                    value: true
                }
            ]));
        }
        this.comTaskStore.load({
            callback: function (records) {
                if(this.comTaskStore!== undefined && toRemove !== undefined){
                    this.comTaskStore.remove(toRemove);
                }

            }
        });
    },

    previewCommunicationSchedule: function () {
        var communicationSchedules = this.getCommunicationSchedulesGrid().getSelectionModel().getSelection();
        var me = this;
        if (communicationSchedules.length == 1) {
            this.getCommunicationSchedulePreviewForm().loadRecord(communicationSchedules[0]);
            me.getCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').removeAll();
            if (communicationSchedules[0].comTaskUsages().data.items.length === 0) {
                me.getCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').add({
                    xtype: 'displayfield'
                });
            } else {
                Ext.each(communicationSchedules[0].comTaskUsages().data.items, function (comTaskUsage) {
                    me.getCommunicationSchedulePreviewForm().down('#comTaskPreviewContainer').add({
                        xtype: 'displayfield',
                        value: '<a>' + comTaskUsage.get('name') + '</a>'
                    })
                });
            }
        }
    },

    saveCommunicationSchedule: function () {
        var me = this;
        var values = this.getCommunicationScheduleEditForm().getValues();
        if (this.record) {
            this.record.set(values);
            this.record.save({
                success: function (record) {
                    location.href = '#/administration/communicationschedules';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        Ext.each(json.errors, function (error) {
                            if (error.id === 'temporalExpression.every') {
                                error.id = 'temporalExpression';
                            }
                        });
                        me.getCommunicationScheduleEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    addCommunicationTask: function () {
        var me = this;
        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(1);
        this.getAddCommunicationTaskPreview().setVisible(false);
    },

    deleteComTask: function (comTask) {
        this.record.comTaskUsages().remove(comTask);
        this.comTaskStore.add(comTask);
        if (this.record.comTaskUsages().getCount() > 0) {
            this.getComTaskPanel().getLayout().setActiveItem(1);
        } else {
            this.getComTaskPanel().getLayout().setActiveItem(0);
        }
    },

    deleteCommunicationSchedule: function (communicationSchedule) {
        this.showWarning(communicationSchedule)
    },

    showWarning: function (communicationSchedule) {
        var me = this;
        var msg = '';
        if(communicationSchedule.get('isInUse')){
            msg = Uni.I18n.translate('communicationschedule.deleteInUseCommunicationSchedule', 'MDC', 'This schedule will no longer be available here and on the devices it has been added to. If this schedule is still running, it will be deleted after data collection is complete.');
        } else {
           msg = Uni.I18n.translate('communicationschedule.deleteCommunicationSchedule', 'MDC', 'This schedule will no longer be available.');
        }
        Ext.create('Uni.view.window.Confirmation').show({
            msg: msg,
            title: Uni.I18n.translate('communicationschedule.delete', 'MDC', 'Remove') + ' ' + communicationSchedule.get('name') + '?',
            config: {
                communicationScheduleToDelete: communicationSchedule,
                me: me
            },
            scope: me,
            fn: me.removeCommunicationSchedule
        });
    },

    deleteCommunicationScheduleFromPreview: function () {
        this.showWarning(this.getCommunicationSchedulesGrid().getSelectionModel().getSelection()[0]);
    },

    removeCommunicationSchedule: function (btn, text, opt) {
        if (btn === 'confirm') {
            var communicationScheduleToDelete = opt.config.communicationScheduleToDelete;
            var store = this.getCommunicationSchedulesGrid().getStore();
            store.remove(communicationScheduleToDelete);
            store.sync();
        }
    },

    addCommunicationTasksToSchedule: function () {
        var selection = this.getCommunicationTaskGrid().getSelectionModel().getSelection();
        this.record.comTaskUsages().add(selection);
        this.comTaskStore.remove(selection);
        this.getCommunicationScheduleEditForm().down('#comTasksOnForm').reconfigure(this.record.comTaskUsages());
        if (this.record.comTaskUsages().getCount() > 0) {
            this.getComTaskPanel().getLayout().setActiveItem(1);
        } else {
            this.getComTaskPanel().getLayout().setActiveItem(0);
        }
        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(0);
    },

    cancelAddCommunicationTasksToSchedule: function(){
        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(0);
    },

    previewComTask: function(a,selection){
        var me = this;
        if(selection[selection.length-1]!= undefined){
            Ext.Ajax.request({
                url: '/api/cts/comtasks/' + selection[selection.length-1].data.id,
                success: function (response) {
                    var rec = Ext.decode(response.responseText),
                        str = '';
                    me.getAddCommunicationTaskPreview().setTitle(rec.name);
                    Ext.Array.each(rec.commands, function (command) {
                        str += command.action.charAt(0).toUpperCase() + command.action.slice(1) + ' ' + command.category.charAt(0).toUpperCase() + command.category.slice(1) + '<br/>';
                    });
                    me.getComTaskCommands().setValue(str);
                    me.getAddCommunicationTaskPreview().setVisible(true);
                }
            });
        }
    }
});
