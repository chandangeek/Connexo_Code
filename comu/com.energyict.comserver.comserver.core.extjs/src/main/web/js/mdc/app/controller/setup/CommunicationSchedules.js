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
        {ref: 'communicationScheduleView', selector: '#CommunicationSchedulesSetup'},
        {ref: 'communicationSchedulePreviewForm', selector: '#communicationSchedulePreviewForm'},
        {ref: 'communicationScheduleEditForm', selector: '#communicationScheduleEditForm'},
        {ref: 'communicationTaskGrid', selector: '#communicationTaskGridFromSchedule'},
        {ref: 'comTaskPanel', selector: '#comTaskPanel'},
        {ref: 'addCommunicationTaskPreview', selector: '#addCommunicationTaskPreview'},
        {ref: 'comTaskCommands', selector: '#comtaskCommands'},
        {ref: 'addComTaskWindow', selector: '#addCommunicationTaskWindow'}
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
            },
            '#addCommunicationTaskButtonForm button[action=cancelAction]': {
                click: this.cancelAddCommunicationTasksToSchedule
            },
            '#communicationTaskGridFromSchedule': {
                selectionchange: this.previewComTask
            },
            '#communicationScheduleEditForm #scheduleField': {
                change: this.setEditFormSummary
            },
            '#communicationScheduleEditForm #startDate': {
                change: this.setEditFormSummary
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
            returnLink: me.getController('Uni.controller.history.Router').getRoute('administration/communicationschedules').buildUrl()
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.down('#card').getLayout().setActiveItem(0);
        if (id === undefined) {
            this.record = Ext.create(Mdc.model.CommunicationSchedule);
            widget.down('#communicationScheduleEditForm').setTitle(Uni.I18n.translate('communicationschedule.addCommunicationSchedule', 'MDC', 'Add communication schedule'));
            me.initComTaskStore();
        } else {
            Ext.ModelManager.getModel('Mdc.model.CommunicationSchedule').load(id, {
                success: function (communicationSchedule) {
                    me.getApplication().fireEvent('loadCommunicationSchedule', communicationSchedule);
                    me.record = communicationSchedule;
                    widget.down('#communicationScheduleEditForm').setTitle(Uni.I18n.translate('communicationschedule.editCommunicationSchedule', 'MDC', 'Edit') + ' ' + communicationSchedule.get('name'));
                    widget.down('#communicationScheduleEditForm').loadRecord(communicationSchedule);
                    widget.down('#noComTasksSelectedMsg').hide();
                    widget.down('#comTasksOnForm').show();
                    widget.down('#communicationScheduleEditForm').down('#comTasksOnForm').reconfigure(communicationSchedule.comTaskUsages());
                    if (communicationSchedule.get('isInUse')){
                        widget.down('#addCommunicationTaskButton').disable();
                    } else {
                        widget.down('#addCommunicationTaskButton').enable();
                    }
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
                if (this.comTaskStore !== undefined && toRemove !== undefined) {
                    this.comTaskStore.remove(toRemove);
                }
            }
        });
    },

    previewCommunicationSchedule: function () {
        var communicationSchedule = this.getCommunicationSchedulesGrid().getSelectionModel().getLastSelected(),
            preview = this.getCommunicationSchedulePreview(),
            previewForm = this.getCommunicationSchedulePreviewForm();

        preview.setTitle(communicationSchedule.get('name'));
        previewForm.loadRecord(communicationSchedule);
        previewForm.down('#comTaskPreviewContainer').removeAll();
        if (communicationSchedule.comTaskUsages().data.items.length === 0) {
            previewForm.down('#comTaskPreviewContainer').add({
                xtype: 'displayfield'
            });
        } else {
            Ext.each(communicationSchedule.comTaskUsages().data.items, function (comTaskUsage) {
                previewForm.down('#comTaskPreviewContainer').add({
                    xtype: 'displayfield',
                    value: '<a>' + comTaskUsage.get('name') + '</a>'
                })
            });
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
                            if (error.id === 'nextExecutionSpecs.temporalExpression.every') {
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
        var grid =  this.getCommunicationTaskGrid(),
            preview = this.getAddCommunicationTaskPreview(),
            allItemsRadioField = grid.down('radiogroup').down('radiofield[inputValue=allItems]');

        grid.getSelectionModel().deselectAll();
        !grid.isAllSelected() && allItemsRadioField.setValue(true);

        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(1);
        this.getAddCommunicationTaskPreview().setVisible(false);
    },

    deleteComTask: function (comTask) {
        var me = this,
            form = this.getCommunicationScheduleEditForm(),
            recordsAlreadyPresented = [],
            hasComTasks;

        me.record.comTaskUsages().remove(comTask);
        me.record.comTaskUsages().each(function (record) {
            recordsAlreadyPresented.push(record);
        });
        me.comTaskStore.add(comTask);
        me.comTaskStore.load({
            callback: function() {
                me.comTaskStore.remove(recordsAlreadyPresented);
            }
        });
        hasComTasks = me.record.comTaskUsages().getCount() ? true : false;
        form.down('#noComTasksSelectedMsg').setVisible(!hasComTasks);
        form.down('#comTasksOnForm').setVisible(hasComTasks);
    },

    deleteCommunicationSchedule: function (communicationSchedule) {
        this.showWarning(communicationSchedule)
    },

    showWarning: function (communicationSchedule) {
        var me = this;
        var msg = '';
        if (communicationSchedule.get('isInUse')) {
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
            var me = this,
                page = me.getCommunicationScheduleView(),
                communicationScheduleToDelete = opt.config.communicationScheduleToDelete,
                store = this.getCommunicationSchedulesGrid().getStore(),
                gridToolbarTop = me.getCommunicationSchedulesGrid().down('pagingtoolbartop');

            page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
            communicationScheduleToDelete.destroy({
                callback: function (model, operation) {
                    page.setLoading(false);
                    if (operation.wasSuccessful()) {
                        gridToolbarTop.totalCount = 0;
                        store.loadPage(1);
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationschedule.removed', 'MDC', 'Communication schedule successfully removed'));
                    }
                }
            });
        }
    },

    addCommunicationTasksToSchedule: function () {
        var grid =  this.getCommunicationTaskGrid(),
            form = this.getCommunicationScheduleEditForm(),
            hasComTasks,
            selection;

        grid.isAllSelected() && grid.getSelectionModel().selectAll();

        selection = grid.getSelectionModel().getSelection();
        this.record.comTaskUsages().add(selection);
        this.comTaskStore.remove(selection);
        this.getCommunicationScheduleEditForm().down('#comTasksOnForm').reconfigure(this.record.comTaskUsages());
        hasComTasks = this.record.comTaskUsages().getCount() ? true : false;
        form.down('#noComTasksSelectedMsg').setVisible(!hasComTasks);
        form.down('#comTasksOnForm').setVisible(hasComTasks);
        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(0);
    },

    cancelAddCommunicationTasksToSchedule: function () {
        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(0);
    },

    previewComTask: function (a, selection) {
        var me = this;
        if (selection[selection.length - 1] != undefined) {
            Ext.Ajax.request({
                url: '/api/cts/comtasks/' + selection[selection.length - 1].data.id,
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
    },

    setEditFormSummary: function () {
        var form = this.getCommunicationScheduleEditForm(),
            schedule = form.down('#scheduleField').getValue(),
            startDate = form.down('#startDate').getValue(),
            summaryField = form.down('#communicationScheduleSummary'),
            scheduleFormatted = Mdc.util.ScheduleToStringConverter.convert(schedule).split(' ');

        if (startDate && schedule) {
            scheduleFormatted[0] = scheduleFormatted[0].toLowerCase();

            summaryField.setValue('<b>'
                + Uni.I18n.translate('communicationschedule.repeat', 'MDC', 'Repeat') + ' '
                + scheduleFormatted.join(' ')
                + ' ' + Uni.I18n.translate('communicationschedule.startingFrom', 'MDC', 'Starting from').toLowerCase() + ' '
                + Uni.I18n.formatDate('communicationschedule.startingDateFormat', new Date(startDate), 'MDC', 'F d, Y \\a\\t H:i:s')
                + '</b>');

            this.setEditFormPreview(startDate, schedule);
        }
    },

    setEditFormPreview: function (startDate, schedule) {
        var store = this.getCommunicationScheduleEditForm().down('#communicationSchedulePreviewGrid').getStore(),
            storeData = [],
            startOf = schedule.every.timeUnit.slice(0, -1) === 'week' ? 'isoWeek' : schedule.every.timeUnit.slice(0, -1);

        startDate = moment(startDate);

        storeData.push({
            date: startDate.clone().toDate()
        });

        for (var i = 1; i < 5; i++) {
            if (!schedule.lastDay) {
                storeData.push({
                    date: startDate.clone().startOf(startOf).add(schedule.every.timeUnit, schedule.every.count * i).add(schedule.offset.timeUnit, schedule.offset.count).toDate()
                });
            } else {
                storeData.push({
                    date: startDate.clone().endOf('month').startOf('day').add(schedule.every.timeUnit, schedule.every.count * i).add(schedule.offset.timeUnit, schedule.offset.count).toDate()
                });
            }
        }

        store.loadData(storeData);
    }
});
