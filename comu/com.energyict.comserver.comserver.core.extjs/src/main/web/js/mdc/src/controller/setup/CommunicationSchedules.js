/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

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
        'Mdc.view.setup.communicationschedule.AddCommunicationTaskWindow'
    ],

    stores: [
        'CommunicationSchedules',
        'CommunicationTasksForCommunicationSchedule'
    ],

    refs: [
        {ref: 'communicationScheduleEdit', selector: '#communicationScheduleEdit'},
        {ref: 'communicationSchedulesGrid', selector: '#communicationSchedulesGrid'},
        {ref: 'communicationSchedulePreview', selector: '#communicationSchedulePreview'},
        {ref: 'communicationScheduleView', selector: '#CommunicationSchedulesSetup'},
        {ref: 'communicationSchedulePreviewForm', selector: '#communicationSchedulePreviewForm'},
        {ref: 'communicationScheduleEditForm', selector: '#communicationScheduleEditForm'},
        {ref: 'communicationTaskGrid', selector: '#communicationTaskGridFromSchedule'},
        {ref: 'communicationTaskSelectionGrid', selector: '#communicationTaskGridFromSchedule'},
        {ref: 'comTaskPanel', selector: '#comTaskPanel'},
        {ref: 'comTaskCommands', selector: '#comtaskCommands'},
        {ref: 'addComTaskWindow', selector: '#addCommunicationTaskWindow'}
    ],

    record: null,
    alreadyAddedComTasks: [],
    cloneRecord: null,

    init: function () {
        this.control({
            '#CommunicationSchedulesSetup button[action = createCommunicationSchedule]': {
                click: this.createCommunicationScheduleHistory
            },
            '#communicationSchedulesGrid actioncolumn': {
                editCommunicationSchedule: this.editCommunicationScheduleHistory,
                deleteCommunicationSchedule: this.deleteCommunicationSchedule,
                cloneCommunicationSchedule: this.cloneCommunicationSchedule
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
            '#communicationSchedulePreview menuitem[action=cloneCommunicationSchedule]': {
                click: this.cloneCommunicationSchedule
            },
            'addCommunicationTaskWindow #addCommunicationTasksToSchedule': {
                click: this.addCommunicationTasksToSchedule
            },
            'addCommunicationTaskWindow #cancelAddCommunicationTasksToSchedule': {
                click: this.cancelAddCommunicationTasksToSchedule
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
        var me = this,
            store = Ext.data.StoreManager.lookup('CommunicationSchedules'),
            widget = Ext.widget('communicationSchedulesSetup');

        me.getApplication().fireEvent('changecontentevent', widget);

        store.load({
            callback: function (records) {
                me.getCommunicationSchedulesGrid().getSelectionModel().doSelect(0);

            }
        });
    },

    createCommunicationScheduleHistory: function () {
        location.href = '#/administration/communicationschedules/add';
    },

    editCommunicationScheduleHistory: function (record) {

        location.href = '#/administration/communicationschedules/' + encodeURIComponent(this.getCommunicationSchedulesGrid().getSelectionModel().getSelection()[0].get('id')) + '/edit';
    },

    showCommunicationSchedulesEditView: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('communicationScheduleEdit', {
                mode: id !== undefined ? 'edit' : 'add',
                returnLink: router.getRoute('administration/communicationschedules').buildUrl(),
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        widget.down('#card').getLayout().setActiveItem(0);
        if (id === undefined) {
            me.mode = 'create';
            this.record = Ext.create(Mdc.model.CommunicationSchedule);
            widget.down('#communicationScheduleEditForm').setTitle(Uni.I18n.translate('communicationSchedule.add', 'MDC', 'Add shared communication schedule'));
            me.initComTaskStore(widget);
            widget.down('#startDate').setValue(new Date().setHours(0, 0, 0, 0));
        } else {
            me.mode = 'edit';
            Ext.ModelManager.getModel('Mdc.model.CommunicationSchedule').load(id, {
                success: function (communicationSchedule) {
                    me.getApplication().fireEvent('loadCommunicationSchedule', communicationSchedule);
                    me.record = communicationSchedule;
                    widget.down('#communicationScheduleEditForm').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", communicationSchedule.get('name')));
                    widget.down('#communicationScheduleEditForm').loadRecord(communicationSchedule);
                    widget.down('#noComTasksSelectedMsg').hide();
                    widget.down('#comTasksOnForm').show();
                    widget.down('#communicationScheduleEditForm').down('#comTasksOnForm').reconfigure(communicationSchedule.comTaskUsages());
                    if (communicationSchedule.get('isInUse')) {
                        widget.down('#addCommunicationTaskButton').disable();
                        widget.down('#comTasksOnForm').columns[1].setVisible(false);
                        widget.down('uni-form-info-message[name=warning]').show();
                        widget.down('#editConnectionMethodMRIDField').disable();
                    }
                    me.initComTaskStore(widget);
                    communicationSchedule.comTaskUsages().each(function (record) {
                        me.alreadyAddedComTasks.push(record);
                    });
                }
            });
        }
    },

    initComTaskStore: function (widget) {
        this.comTaskStore = Ext.data.StoreManager.lookup('CommunicationTasksForCommunicationSchedule');
        if (this.record.get('id') === null && this.mode !== 'clone') {
            this.comTaskStore.setProxy({
                type: 'rest',
                url: '/api/cts/comtasks',
                reader: {
                    type: 'json',
                    root: 'data'
                },
                pageParam: false,
                startParam: false,
                limitParam: false
            });
        } else {
            this.comTaskStore.setProxy({
                type: 'rest',
                url: '/api/scr/schedules/{id}/comTasks',
                reader: {
                    type: 'json',
                    root: 'data'
                },
                pageParam: false,
                startParam: false,
                limitParam: false
            });
            this.comTaskStore.getProxy().setExtraParam('id', this.record.get('id'));
            this.comTaskStore.getProxy().setExtraParam('filter', Ext.encode([
                {
                    property: 'available',
                    value: true
                }
            ]));
        }
        this.comTaskStore.load(function () {
            widget.setLoading(false);
        });
    },

    previewCommunicationSchedule: function () {
        var communicationSchedule = this.getCommunicationSchedulesGrid().getSelectionModel().getLastSelected(),
            preview = this.getCommunicationSchedulePreview(),
            previewForm = this.getCommunicationSchedulePreviewForm(),
            taskList = '';

        preview.setTitle(Ext.String.htmlEncode(communicationSchedule.get('name')));
        previewForm.loadRecord(communicationSchedule);
        previewForm.down('#comTaskPreviewContainer').removeAll();
        Ext.each(communicationSchedule.comTaskUsages().data.items, function (comTaskUsage) {
            taskList += Ext.String.htmlEncode(comTaskUsage.get('name')) + '<br/>'
        });
        previewForm.down('#comTaskPreviewContainer').add({
            xtype: 'displayfield',
            value: taskList,
            htmlEncode: false
        })
    },

    saveCommunicationSchedule: function () {
        var me = this,
            values = this.getCommunicationScheduleEditForm().getValues(),
            editView = this.getCommunicationScheduleEdit(),
            router = me.getController('Uni.controller.history.Router'),
            backUrl = router.getRoute('administration/communicationschedules').buildUrl();


        if (this.record && this.getCommunicationScheduleEditForm().getForm().isValid()) {
            this.record.set(values);
            me.getCommunicationScheduleEditForm().getForm().clearInvalid();
            me.hideErrorPanel();
            editView.setLoading(true);
            this.record.save({
                backUrl: backUrl,
                success: function (record) {
                    location.href = backUrl;
                    if (me.mode == 'edit') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationschedule.saved', 'MDC', 'Shared communication schedule saved'));
                    } else if (me.mode === 'clone') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationschedule.cloned', 'MDC', 'Shared communication schedule cloned'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationschedule.added', 'MDC', 'Shared communication schedule added'));
                    }
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        Ext.each(json.errors, function (error) {
                            switch (error.id) {
                                case 'nextExecutionSpecs.temporalExpression.every':
                                    error.id = 'temporalExpression';
                                    break;
                                case 'comTaskUsages':
                                    error.id = 'comTaskUsageErrors';
                                    break;
                            }
                        });
                        me.getCommunicationScheduleEditForm().getForm().markInvalid(json.errors);
                        me.showErrorPanel();
                    }
                },
                callback: function () {
                    editView.setLoading(false);
                }
            });
        } else {
            me.showErrorPanel();
        }
    },

    addCommunicationTask: function () {
        var grid = this.getCommunicationTaskGrid(),
            allItemsRadioField = grid.down('radiogroup').down('radiofield[inputValue=allItems]'),
            addButton = this.getCommunicationScheduleEdit().down('#card').down('#addCommunicationTasksToSchedule');

        grid.getSelectionModel().deselectAll();
        !grid.isAllSelected() && allItemsRadioField.setValue(true);
        if (grid.getStore().getCount() === 0) {
            addButton.hide();
        } else {
            addButton.show();
        }

        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(1);
    },

    deleteComTask: function (comTask) {
        var me = this,
            form = this.getCommunicationScheduleEditForm(),
            editView = this.getCommunicationScheduleEdit(),
            grid = this.getCommunicationTaskGrid(),
            recordsAlreadyPresented = [],
            hasComTasks;

        Ext.suspendLayouts();
        me.record.comTaskUsages().remove(comTask);
        me.comTaskStore.add(comTask);
        me.comTaskStore.fireEvent('load', me.comTaskStore.getRange());
        hasComTasks = me.record.comTaskUsages().getCount() ? true : false;
        form.down('#noComTasksSelectedMsg').setVisible(!hasComTasks);
        form.down('#comTasksOnForm').setVisible(hasComTasks);
        Ext.resumeLayouts(true);
    },

    deleteCommunicationSchedule: function (communicationSchedule) {
        this.showWarning(communicationSchedule)
    },

    showWarning: function (communicationSchedule) {
        var me = this;
        var msg = Uni.I18n.translate('communicationschedule.deleteCommunicationSchedule', 'MDC', 'This schedule will no longer be available.');
        Ext.create('Uni.view.window.Confirmation').show({
            msg: msg,
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [communicationSchedule.get('name')]),
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
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationSchedule.removed', 'MDC', 'Shared communication schedule removed'));
                    }
                }
            });
        }
    },

    cloneCommunicationSchedule: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        me.cloneRecord = me.getCommunicationSchedulesGrid().getSelectionModel().getSelection()[0];
        router.getRoute('administration/communicationschedules/clone').forward();
    },

    showCommunicationSchedulesCloneView: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget;
        if (me.cloneRecord == null) {
            window.location.replace(router.getRoute('notfound').buildUrl());
            return;
        }
        widget = Ext.widget('communicationScheduleEdit', {
            mode: 'clone',
            returnLink: router.getRoute('administration/communicationschedules').buildUrl(),
            router: router
        });

        me.record = me.cloneRecord.copy();
        me.cloneRecord.comTaskUsages().each(function (record) {
            me.record.comTaskUsages().add(record.copy());
            me.alreadyAddedComTasks.push(record);
        });
        widget.down('#card').getLayout().setActiveItem(0);
        me.record.set('name', null);
        me.record.set('mRID', null)
        me.mode = 'clone';
        me.getApplication().fireEvent('loadCommunicationSchedule', me.cloneRecord);
        widget.down('#communicationScheduleEditForm').setTitle(Uni.I18n.translate('general.clonex', 'MDC', "Clone '{0}'", me.cloneRecord.get('name')));
        widget.down('#communicationScheduleEditForm').loadRecord(me.record);
        widget.down('#noComTasksSelectedMsg').hide();
        widget.down('#comTasksOnForm').show();
        widget.down('#communicationScheduleEditForm').down('#comTasksOnForm').reconfigure(me.record.comTaskUsages());
        me.initComTaskStore(widget);
        me.cloneRecord = null;
        me.record.set('id', null);

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    addCommunicationTasksToSchedule: function () {
        var grid = this.getCommunicationTaskGrid(),
            form = this.getCommunicationScheduleEditForm(),
            hasComTasks,
            selection;

        Ext.suspendLayouts();
        grid.isAllSelected() && grid.getSelectionModel().selectAll();

        selection = grid.getSelectionModel().getSelection();
        this.record.comTaskUsages().add(selection);
        this.comTaskStore.remove(selection);
        this.getCommunicationScheduleEditForm().down('#comTasksOnForm').reconfigure(this.record.comTaskUsages());
        hasComTasks = this.record.comTaskUsages().getCount() ? true : false;
        form.down('#noComTasksSelectedMsg').setVisible(!hasComTasks);
        form.down('#comTasksOnForm').setVisible(hasComTasks);
        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(0);
        Ext.resumeLayouts(true);
    },

    cancelAddCommunicationTasksToSchedule: function () {
        this.getCommunicationScheduleEdit().down('#card').getLayout().setActiveItem(0);
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
                + Uni.DateTime.formatDateLong(new Date(startDate))
                + ' ' + Uni.I18n.translate('general.lowercase.at', 'MDC', 'at') + ' '
                + Uni.DateTime.formatTimeLong(new Date(startDate))
                + '</b>');

            this.setEditFormPreview(startDate, schedule);
        }
    },

    setEditFormPreview: function (startDate, schedule) {
        var store = this.getCommunicationScheduleEditForm().down('#communicationSchedulePreviewGrid').getStore(),
            storeData = [],
            lastScheduledDate,
            offsetStart,
            intervalInMillis;

        if (schedule.every.timeUnit === 'minutes') {
            startDate = moment(startDate);
            intervalInMillis = moment.duration(schedule.every.count, schedule.every.timeUnit);
            lastScheduledDate = moment(Math.floor(startDate / intervalInMillis) * intervalInMillis);
        }
        else if (schedule.every.timeUnit === 'hours') {
            var beginOfDay,
                lastScheduledHour;
            startDate = moment(startDate);
            beginOfDay = moment(startDate).startOf('day');
            lastScheduledHour = Math.floor(startDate.hours() / schedule.every.count) * schedule.every.count;
            lastScheduledDate = beginOfDay.add(lastScheduledHour, 'hour');
        }
        else if (schedule.every.timeUnit === 'days') {
            lastScheduledDate = moment(startDate).clone().startOf('day');
        } else if (schedule.every.timeUnit === 'weeks') {
            var startOf = schedule.every.timeUnit.slice(0, -1) === 'week' ? 'isoWeek' : schedule.every.timeUnit.slice(0, -1);
            lastScheduledDate = moment(startDate).clone().startOf(startOf);
        } else if (schedule.every.timeUnit === 'months') {
            lastScheduledDate = moment(startDate).clone().startOf('month');
        }
        offsetStart = startDate > lastScheduledDate ? 1:0;
        for (var i = offsetStart; i < offsetStart + 5; i++) {
            if (!schedule.lastDay) {
                storeData.push({
                    date: lastScheduledDate.clone().add(schedule.every.timeUnit, schedule.every.count * i).add(schedule.offset.timeUnit, schedule.offset.count).toDate()
                });
            } else {
                storeData.push({
                    date: lastScheduledDate.clone().endOf('month').startOf('day').add(schedule.every.timeUnit, schedule.every.count * (i - 1)).endOf('month').startOf('day').add(schedule.offset.timeUnit, schedule.offset.count).toDate()
                });
            }
        }

        store.loadData(storeData);
    },

    showErrorPanel: function () {
        this.getCommunicationScheduleEditForm().down('#communicationScheduleEditFormErrors').show();
    },

    hideErrorPanel: function () {
        this.getCommunicationScheduleEditForm().down('#communicationScheduleEditFormErrors').hide();
    }

});
