Ext.define('Mdc.controller.setup.Comtasks', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    stores: [
        'Mdc.store.CommunicationTasks',
        'Mdc.store.CommunicationTasksCategories',
        'Mdc.store.CommunicationTasksActions',
        'Mdc.store.TimeUnits',
        'Mdc.store.LogbookTypes',
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.RegisterGroups',
        'Mdc.store.MessageCategories',
        'Mdc.store.SelectedMessageCategories'
    ],
    models: [
        'Mdc.model.CommunicationTask'
    ],
    views: [
        'Mdc.view.setup.comtasks.ComtaskSetup',
        'Mdc.view.setup.comtasks.ComtaskGrid',
        'Mdc.view.setup.comtasks.ComtaskPreview',
        'Mdc.view.setup.comtasks.ComtaskCreateEditForm',
        'Mdc.view.setup.comtasks.ComtaskCommand',
        'Mdc.view.setup.comtasks.parameters.Logbooks',
        'Mdc.view.setup.comtasks.parameters.Profiles',
        'Mdc.view.setup.comtasks.parameters.Registers',
        'Mdc.view.setup.comtasks.ComTaskAddCommandWindow',
        'Mdc.view.setup.comtasks.ComtaskOverview',
        'Mdc.view.setup.comtasks.ComtaskActions',
        'Mdc.view.setup.comtasks.ComtaskCommandCategories',
        'Mdc.view.setup.comtasks.ComtaskAddActionContainer',
        'Mdc.view.setup.comtasks.ComtaskAddCommandCategories'
    ],
    refs: [
        { ref: 'tasksView', selector: 'comtaskSetup' },
        { ref: 'itemPanel', selector: 'comtaskPreview' },
        { ref: 'comTaskOverviewForm', selector: '#mdc-comtask-overview-form' },
        { ref: 'comTaskActionsView', selector: '#mdc-comtask-actions-view' },
        { ref: 'comTaskAddActionsView', selector: '#mdc-comtask-addActions-view' },
        { ref: 'comTaskCommandCategoriesView', selector: '#mdc-comtask-commandCategories-view' },
        { ref: 'comTaskAddCommandCategoriesView', selector: 'comtaskAddCommandCategories' },
        { ref: 'tasksGrid', selector: 'comtaskGrid' },
        { ref: 'comtaskActionsGrid', selector: 'comtaskActionsGrid' },
        { ref: 'nameField', selector: '#comtaskName' },
        { ref: 'gridPagingToolbarTop', selector: 'comtaskGrid pagingtoolbartop' },
        { ref: 'taskEdit', selector: 'comtaskCreateEdit' },
        { ref: 'commandNames', selector: 'comtaskCreateEdit [name=commandnames]' },
        { ref: 'commandFields', selector: 'comtaskCreateEdit [name=commandfields]' }
    ],

    timeUnitsStore: null,
    loadProfileTypesStore: null,
    logbookTypesStore: null,
    registerGroupsStore: null,
    categoriesStore: null,
    commandCategoriesStore: null,
    ACTIONS: 'commands',            // Don't ask me why
    COMMAND_CATEGORIES: 'messages', // idem
    ERROR_MESSAGE_FIELD_REQUIRED: Uni.I18n.translate('general.required.field', 'MDC', 'This field is required'),

    init: function () {
        this.control({
            'comtaskSetup comtaskGrid': {
                select: this.showTaskDetails
            },
            'comtaskSetup comtaskGrid uni-actioncolumn': {
                menuclick: this.chooseCommunicationTasksAction
            },
            'comtaskActionMenu': {
                click: this.chooseCommunicationTasksAction
            },
            '#comTaskAddCommandWindow comtaskCommandCategoryCombo': {
                change: this.addActionCombo
            },
            '#mdc-comtask-addAction-command-category-combo': {
                change: this.addActionComboBox
            },
            'comtaskCreateEdit #addCommandsToTask': {
                click: this.showAddCommandPopUp
            },
            'comtaskCreateEdit #addAnotherCommandsButton': {
                click: this.showAddCommandPopUp
            },
            '#comTaskAddCommandWindow comtaskCommandCategoryActionCombo': {
                change: this.addCommandParameters
            },
            '#mdc-comtask-addAction-action-combo' : {
                change: this.addActionParameters
            },
            '#comTaskAddCommandWindow #addCommandToTask': {
                click: this.addCommandToModel
            },
            'comtaskAddActionContainer #mdc-comtask-addAction-actionBtn': {
                click: this.addOrEditAction
            },
            'comtaskCreateEdit comtaskCommand button[action=cancelEditCommand]': {
                click: this.cancelEdit
            },
            'comtaskCreateEdit comtaskCommand button[action=removeCommand]': {
                click: this.removeEdit
            },
            'comtaskCreateEdit #createEditTask': {
                click: this.createEdit
            },
            'communication-tasks-profilescombo': {
                afterrender: this.setTooltip
            },
            '#mdc-comtask-actions-grid': {
                select: this.showComTaskActionDetails
            },
            '#mdc-comtask-commandCategories-grid actioncolumn': {
                removeCommandCategory: this.onRemoveCommandCategory
            },
            'comTaskActions comtaskActionsGrid uni-actioncolumn': {
                menuclick: this.onActionActionMenuClick
            },
            'comtaskActionActionMenu': {
                click: this.onActionActionMenuClick
            },
            '#mdc-comtask-addCommandCategories-cancel': {
                click: this.forwardToPreviousPage
            },
            '#mdc-comtask-addCommandCategories-add': {
                click: this.addCommandCategories
            }
        });
        this.store = this.getStore('Mdc.store.CommunicationTasks');
    },

    showCommunicationTasksView: function () {
        var widget = Ext.widget('comtaskSetup', {
            router: this.getController('Uni.controller.history.Router')
        });
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    chooseCommunicationTasksAction: function (menu, item) {
        var tasksGrid = this.getTasksGrid(),
            comTask = tasksGrid ? tasksGrid.getView().getSelectionModel().getLastSelected() : menu.communicationTask

        this.performMenuAction(item.action, comTask);
    },

    performMenuAction: function(action, comTask) {
        switch (action) {
            case 'editComTask':
                var router = this.getController('Uni.controller.history.Router');
                router.getRoute('administration/communicationtasks/view/edit').forward({id: comTask.get('id')});
                break;
            case 'deleteComTask':
                this.showConfirmationPanel(comTask);
                break;
        }
    },

    onActionActionMenuClick: function(menu, item) {
        var me = this,
            actionsGrid = me.getComtaskActionsGrid(),
            actionRecord = actionsGrid.getView().getSelectionModel().getLastSelected(),
            rowIndex = actionsGrid.getStore().indexOf(actionRecord);

        switch(item.action) {
            case 'deleteComTaskAction':
                me.removeAction(actionsGrid, rowIndex, actionRecord);
                break;
            case 'editComTaskAction':
                var router = this.getController('Uni.controller.history.Router');
                router.getRoute('administration/communicationtasks/view/actions/edit').forward({
                    id: me.getComTaskActionsView().communicationTask.get('id'),
                    actionId: actionRecord.get('id')
                });
                break;
        }
    },

    showAddCommandPopUp: function (btn) {
        var categoriesStore = this.getStore('Mdc.store.CommunicationTasksCategories'),
            widget = btn.up('comtaskCreateEdit'),
            window;

        widget.setLoading(true);
        categoriesStore.load({
            scope: this,
            callback: function () {
                widget.setLoading(false);
                window = Ext.create('Mdc.view.setup.comtasks.ComTaskAddCommandWindow', {
                    title: Uni.I18n.translate('general.addAction', 'MDC', 'Add action'),
                    btnAction: 'add',
                    btnText: Uni.I18n.translate('general.add', 'MDC', 'Add')
                });
            }
        });
    },

    showTaskDetails: function (grid, record) {
        var me = this,
            itemPanel = this.getItemPanel(),
            previewForm = itemPanel.down('#comtaskPreviewFieldsPanel'),
            model = this.getModel('Mdc.model.CommunicationTask');

        itemPanel.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.setLoading(true);
        model.load(record.get('id'), {
            success: function (record) {
                itemPanel = me.getItemPanel();
                if (itemPanel) {
                    previewForm.loadRecord(record);
                    previewForm.setLoading(false);
                }
            }
        });
    },

    showConfirmationPanel: function (comTask) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", comTask.get('name')),
            msg: Uni.I18n.translate('comtask.remove.confirmation.msg', 'MDC', 'This communication task will no longer be available'),
            config: {
                me: me,
                comTask: comTask
            },
            fn: me.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            comTask = conf.config.comTask,
            tasksView = me.getTasksView(),
            comTaskOverviewForm = me.getComTaskOverviewForm(),
            loadingOwner = tasksView ? tasksView : comTaskOverviewForm;

        if (state === 'confirm') {
            loadingOwner.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
            comTask.destroy({
                success: function () {
                    me.getApplication().fireEvent('acknowledge',Uni.I18n.translate('comtasks.removeSuccessMsg', 'MDC', 'Communication task removed'));
                    if (comTaskOverviewForm) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/communicationtasks').forward();
                    }
                },
                failure: function(response){
                    var json;
                    json = Ext.decode(response.responseText, true);
                    if (json && json.message) {
                        me.getApplication().getController('Uni.controller.Error').showError(
                            Uni.I18n.translate('comtasks.removeErrorMsg', 'MDC', 'Error during removal of communication task'),
                            json.message
                        );
                    }
                },
                callback: function () {
                    loadingOwner.setLoading(false);
                }
            });
        }
    },

    showCommunicationTasksCreateEdit: function () {
        var router = this.getController('Uni.controller.history.Router'),
            widget = Ext.widget('comtaskCreateEdit'),
            taskId = router.arguments['id'];

        this.getApplication().fireEvent('changecontentevent', widget);
        this.getTaskEdit().down('toolbar').getComponent('createEditTask').setText(
            Ext.isEmpty(taskId) ? Uni.I18n.translate('general.add', 'MDC', 'Add') : Uni.I18n.translate('general.save', 'MDC', 'Save')
        );
        this.getTaskEdit().down('toolbar').getComponent('createEditTask').action = Ext.isEmpty(taskId) ? 'add' : 'save';

        if (taskId) {
            this.getTaskEdit().getCenterContainer().down().setTitle(Uni.I18n.translate('comtask.edit', 'MDC', 'Edit communication task'));
            this.loadModelToEditForm(taskId, widget);
        } else {
            this.getTaskEdit().getCenterContainer().down().setTitle(Uni.I18n.translate('comtask.create', 'MDC', 'Add communication task'));
        }
    },

    showCommunicationTaskOverview: function (comTaskId) {
        var me = this,
            taskModel = me.getModel('Mdc.model.CommunicationTask');

        taskModel.load(comTaskId, {
            success: function (communicationTask) {
                var widget = Ext.widget('comTaskOverview', {
                        router: me.getController('Uni.controller.history.Router'),
                        communicationTask: communicationTask
                    }),
                    taskName = communicationTask.get('name');

                me.getApplication().fireEvent('loadCommunicationTask', communicationTask);
                widget.down('#mdc-comtask-overview-sidemenu #mdc-comtask-sidemenu-overviewLink').setText(taskName);
                widget.down('#mdc-comtask-overview-form').loadRecord(communicationTask);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    showCommunicationTaskActions: function() {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            comTaskId = router.arguments['id'],
            taskModel = me.getModel('Mdc.model.CommunicationTask');

        taskModel.load(comTaskId, {
            success: function (communicationTask) {
                var actionsStore = new Ext.data.ArrayStore({
                        fields: [
                            {name: 'id'},
                            {name: 'category'},
                            {name: 'action'},
                            {name: 'categoryId'},
                            {name: 'actionId'},
                            {name: 'parameters'}
                        ],
                        sorters: [
                            {
                                property: 'category',
                                direction: 'ASC'
                            }
                        ]
                    }),
                    widget = Ext.widget('comTaskActions', {
                        router: router,
                        communicationTask: communicationTask,
                        actionsStore: actionsStore
                    }),
                    onCategoriesLoaded = function() {
                        if (actionsStore.getCount() < me.categoriesStore.totalCount) {
                            widget.down('#add-communication-task-action').setDisabled(false);
                        }
                    },
                    executeWhenStoreLoaded = function() {
                        me.getApplication().fireEvent('loadCommunicationTask', communicationTask);
                        widget.down('#mdc-comtask-actions-sidemenu #mdc-comtask-sidemenu-overviewLink').setText(communicationTask.get('name'));
                        me.getApplication().fireEvent('changecontentevent', widget);

                        if (Ext.isEmpty(me.categoriesStore)) {
                            me.categoriesStore = me.getStore('Mdc.store.CommunicationTasksCategories');
                            me.categoriesStore.load({
                                scope: me,
                                callback: onCategoriesLoaded
                            });
                        } else {
                            onCategoriesLoaded();
                        }
                        // Show empty grid message or select 1st action:
                        widget.down('#mdc-comtask-actions-previewContainer').updateOnChange(actionsStore.getCount() === 0);
                    };

                if (Ext.isEmpty(communicationTask.get(me.ACTIONS))) {
                    executeWhenStoreLoaded();
                } else {
                    actionsStore.on('datachanged', executeWhenStoreLoaded, me, {single: true});
                    actionsStore.add(communicationTask.get(me.ACTIONS));
                }
            }
        });
    },

    showCommunicationTaskCommandCategories : function() {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            comTaskId = router.arguments['id'],
            taskModel = me.getModel('Mdc.model.CommunicationTask');

        taskModel.load(comTaskId, {
            success: function (communicationTask) {
                var categoriesStore = new Ext.data.ArrayStore({
                        fields: [
                            {name: 'id'},
                            {name: 'name'}
                        ],
                        sorters: [
                            {
                                property: 'name',
                                direction: 'ASC'
                            }
                        ]
                    }),
                    taskName = communicationTask.get('name'),
                    widget = Ext.widget('comTaskCommandCategories', {
                        router: router,
                        communicationTask: communicationTask,
                        categoriesStore: categoriesStore
                    }),
                    executeWhenStoreLoaded = function() {
                        me.getApplication().fireEvent('loadCommunicationTask', communicationTask);
                        widget.down('#mdc-comtask-cmdcategories-sidemenu #mdc-comtask-sidemenu-overviewLink').setText(taskName);
                        widget.down('#mdc-comtask-commandCategories-grid').maxHeight = undefined;
                        me.getApplication().fireEvent('changecontentevent', widget);

                        var onCommandCategoriesLoaded = function() {
                            if (categoriesStore.getCount() < me.commandCategoriesStore.totalCount) {
                                widget.down('#add-command-category-action').setDisabled(false);
                            }
                        };

                        if (Ext.isEmpty(me.commandCategoriesStore)) {
                            me.commandCategoriesStore = me.getStore('Mdc.store.MessageCategories');
                            me.commandCategoriesStore.load({
                                scope: me,
                                callback: onCommandCategoriesLoaded
                            });
                        } else {
                            onCommandCategoriesLoaded();
                        }

                        // Show empty grid message or select 1st action:
                        widget.down('#mdc-comtask-commandCategories-previewContainer').updateOnChange(categoriesStore.getCount() === 0);
                    };

                if (Ext.isEmpty(communicationTask.get(me.COMMAND_CATEGORIES))) {
                    executeWhenStoreLoaded();
                } else {
                    categoriesStore.on('datachanged', executeWhenStoreLoaded, me, {single: true});
                    categoriesStore.add(communicationTask.get(me.COMMAND_CATEGORIES));
                }
            }
        });
    },

    showComTaskActionDetails: function (grid, actionRecord) {
        var me = this,
            afterStoreLoad = function() {
                me.doShowComTaskActionDetails(grid, actionRecord);
            };

        if (me.timeUnitsStore) {
            afterStoreLoad();
        } else {
            me.timeUnitsStore = me.getStore('Mdc.store.TimeUnits');
            me.timeUnitsStore.load(afterStoreLoad);
        }
    },

    doShowComTaskActionDetails: function(grid, actionRecord) {
        var me = this,
            preview = me.getComTaskActionsView().down('#mdc-comtask-action-preview'),
            previewForm = preview.down('#mdc-comtask-action-preview-form');

        preview.setTitle(
            Uni.I18n.translate('general.parameters.actionX', 'MDC', "Parameters of the action '{0}'",
                actionRecord.get('category') + ' - ' + actionRecord.get('action'))
        );
        previewForm.reinitialize();

        // Fill up the parameters
        var markIntervalsAsBadTime = false,
            minimumClockDifference,
            maximumClockDifference,
            maximumClockShift,
            createMeterEvents = false,
            failOnMismatch = false,
            loadProfileTypes = '',
            logbookTypes = '',
            registerGroups = '';

        switch (actionRecord.get('categoryId')) {
            case 'loadprofiles':
                var executeAfterStoreLoad = function() {
                    Ext.Array.each(actionRecord.get('parameters'), function (parameter) {
                        if (parameter.name === 'loadprofiletypeids') {
                            Ext.Array.each(parameter.value, function(value) {
                                loadProfileTypes += Ext.String.htmlEncode(me.loadProfileTypesStore.findRecord('id', value.value).get('name'));
                                loadProfileTypes += '\n'
                            });
                            if (Ext.isEmpty(loadProfileTypes)) {
                                loadProfileTypes = Uni.I18n.translate('general.none', 'MDC', 'None');
                            }
                        } else if (parameter.name === 'markintervalsasbadtime') {
                            markIntervalsAsBadTime = parameter.value;
                        } else if (parameter.name === 'minclockdiffbeforebadtime') {
                            minimumClockDifference = parameter.value
                                ? parameter.value.value + ' ' + me.timeUnitsStore.findRecord('timeUnit', parameter.value.name).get('localizedValue')
                                : parameter.value;
                        } else if (parameter.name === 'createmetereventsfromflags') {
                            createMeterEvents = parameter.value;
                        } else if (parameter.name === 'failifconfigurationmismatch') {
                            failOnMismatch = parameter.value;
                        }
                    });

                    previewForm.addParameter(
                        Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
                        loadProfileTypes
                    );
                    previewForm.addParameter(
                        Uni.I18n.translate('comtask.mark.intervals.as.bad.time','MDC','Mark intervals as bad time'),
                        markIntervalsAsBadTime ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No')
                    );
                    if (markIntervalsAsBadTime) {
                        previewForm.addParameter(
                            Uni.I18n.translate('comtask.minimum.clock.difference','MDC','Minimum clock difference'),
                            minimumClockDifference
                        );
                    }
                    previewForm.addParameter(
                        Uni.I18n.translate('comtask.meter.events.from.status.flag','MDC','Meter events from status flag'),
                        createMeterEvents ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No')
                    );
                    previewForm.addParameter(
                        Uni.I18n.translate('comtask.fail.profile.configuration.doesnt.match','MDC',"Fail if profile configuration doesn't match"),
                        failOnMismatch ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No')
                    );
                };
                if (Ext.isEmpty(me.loadProfileTypesStore)) {
                    me.loadProfileTypesStore = me.getStore('Mdc.store.LoadProfileTypes');
                    me.loadProfileTypesStore.getProxy().pageParam = false;
                    me.loadProfileTypesStore.getProxy().startParam = false;
                    me.loadProfileTypesStore.getProxy().limitParam = false;
                    me.loadProfileTypesStore.load(executeAfterStoreLoad);
                } else {
                    executeAfterStoreLoad();
                }
                break;

            case 'logbooks':
                var executeAfterStoreLoad = function() {
                        Ext.Array.each(actionRecord.get('parameters'), function (parameter) {
                            if (parameter.name === 'logbooktypeids') {
                                Ext.Array.each(parameter.value, function(value) {
                                    logbookTypes += Ext.String.htmlEncode(me.logbookTypesStore.findRecord('id', value.value).get('name'));
                                    logbookTypes += '\n'
                                });
                                if (Ext.isEmpty(logbookTypes)) {
                                    logbookTypes = Uni.I18n.translate('general.none', 'MDC', 'None');
                                }
                                previewForm.addParameter(
                                    Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                                    logbookTypes
                                );
                                return false;
                            }
                        });
                    };
                if (Ext.isEmpty(me.logbookTypesStore)) {
                    me.logbookTypesStore = me.getStore('Mdc.store.LogbookTypes');
                    me.logbookTypesStore.getProxy().pageParam = false;
                    me.logbookTypesStore.getProxy().startParam = false;
                    me.logbookTypesStore.getProxy().limitParam = false;
                    me.logbookTypesStore.load(executeAfterStoreLoad);
                } else {
                    executeAfterStoreLoad();
                }
                break;

            case 'registers':
                var executeAfterStoreLoad = function() {
                        Ext.Array.each(actionRecord.get('parameters'), function (parameter) {
                            if (parameter.name === 'registergroupids') {
                                Ext.Array.each(parameter.value, function(value) {
                                    registerGroups += Ext.String.htmlEncode(me.registerGroupsStore.findRecord('id', value.value).get('name'));
                                    registerGroups += '\n'
                                });
                                if (Ext.isEmpty(registerGroups)) {
                                    registerGroups = Uni.I18n.translate('general.none', 'MDC', 'None');
                                }
                                previewForm.addParameter(
                                    Uni.I18n.translate('comtask.register.groups','MDC','Register groups'),
                                    registerGroups
                                );
                                return false;
                            }
                        });
                    };
                if (Ext.isEmpty(me.registerGroupsStore)) {
                    me.registerGroupsStore = me.getStore('Mdc.store.RegisterGroups');
                    me.registerGroupsStore.getProxy().pageParam = false;
                    me.registerGroupsStore.getProxy().startParam = false;
                    me.registerGroupsStore.getProxy().limitParam = false;
                    me.registerGroupsStore.load(executeAfterStoreLoad);
                } else {
                    executeAfterStoreLoad();
                }
                break;

            case 'clock':
                Ext.Array.each(actionRecord.get('parameters'), function (parameter) {
                    if (parameter.name === 'minimumclockdifference') {
                        minimumClockDifference = parameter.value.value + ' ' +
                            me.timeUnitsStore.findRecord('timeUnit', parameter.value.name).get('localizedValue');
                    } else if (parameter.name === 'maximumclockdifference') {
                        maximumClockDifference = parameter.value.value + ' ' +
                            me.timeUnitsStore.findRecord('timeUnit', parameter.value.name).get('localizedValue');
                    } else if (parameter.name === 'maximumclockshift') {
                        maximumClockShift = parameter.value.value + ' ' +
                            me.timeUnitsStore.findRecord('timeUnit', parameter.value.name).get('localizedValue');
                    }
                });

                previewForm.addParameter(
                    Uni.I18n.translate('comtask.minimum.clock.difference', 'MDC', 'Minimum clock difference'),
                    minimumClockDifference
                );
                previewForm.addParameter(
                    Uni.I18n.translate('comtask.maximum.clock.difference', 'MDC', 'Maximum clock difference'),
                    maximumClockDifference
                );
                if (actionRecord.get('actionId') === 'synchronize') {
                    previewForm.addParameter(
                        Uni.I18n.translate('comtask.maximum.clock.shift', 'MDC', 'Maximum clock shift'),
                        minimumClockDifference
                    );
                }
                break;

            default:
                previewForm.addParameter(' ', Uni.I18n.translate('general.none', 'MDC', 'None'));
                break;
        }
    },

    addCommandCategories: function() {
        var me = this,
            view = this.getComTaskAddCommandCategoriesView(),
            currentComTaskRecord = view.communicationTask,
            grid = view.down('#mdc-comtask-addCommandCategories-grid'),
            selection = grid.getView().getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.setLoading(true);
            for (i=0; i<selection.length; i++) {
                currentComTaskRecord.get(me.COMMAND_CATEGORIES).push({
                    id : selection[i].get('id'),
                    name: selection[i].get('name')
                });
            }
            currentComTaskRecord.save({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('commandCategory.added.success.msg', 'MDC', 'Command categories added'));
                    view.setLoading(false);
                    me.forwardToPreviousPage();
                },
                callback: function () {
                    view.setLoading(false);
                    me.forwardToPreviousPage();
                }
            });
        }
    },

    onRemoveCommandCategory: function(grid, rowIndex, commandCategoryRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = me.getComTaskCommandCategoriesView(),
            comTaskRecord = view.communicationTask,
            backUrl = router.getRoute('administration/communicationtasks/view/commandcategories').buildUrl();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('commandCategory.remove.msg', 'MDC', 'This command category will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", commandCategoryRecord.get('name')),
            scope: me,
            fn: function (state) {
                if (state === 'confirm') {
                    var item2Remove = null;
                    view.setLoading(Uni.I18n.translate('general.removing', 'DLC', 'Removing...'));
                    Ext.Array.each(comTaskRecord.get(me.COMMAND_CATEGORIES), function(message) {
                        if (message.id === commandCategoryRecord.get('id')) {
                            item2Remove = message;
                            return false;
                        }
                    });
                    if (item2Remove) {
                        Ext.Array.remove(comTaskRecord.get(me.COMMAND_CATEGORIES), item2Remove);
                    }
                    comTaskRecord.save({
                        backUrl: backUrl,
                        success: function () {
                            window.location.href = backUrl;
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('commandCategory.remove.success.msg', 'DLC', 'Command category removed'));
                            view.setLoading(false);
                            grid.getStore().removeAt(rowIndex);
                            view.down('#add-command-category-action').setDisabled(false);
                        },
                        callback: function () {
                            view.setLoading(false);
                        }
                    });
                }
            }
        });
    },

    editAction: function(grid, rowIndex, actionRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            comTaskId = router.arguments['id'],
            actionId = parseInt(router.arguments['actionId']),
            view = me.getComTaskActionsView(),
            currentCommunicationTask,
            widget,
            backUrl = router.getRoute('administration/communicationtasks/view/actions').buildUrl(),
            onEveryhingLoaded = function() {
                widget = Ext.widget('comtaskAddActionContainer', {
                    router: router,
                    cancelRoute: 'administration/communicationtasks/view/actions',
                    communicationTask: currentCommunicationTask,
                    btnAction: 'editComTaskAction',
                    btnText: Uni.I18n.translate('general.save', 'MDC', 'Save')
                });
                me.getApplication().fireEvent('loadCommunicationTask', currentCommunicationTask);
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#mdc-comtask-addAction-form').setTitle(Uni.I18n.translate('general.editAction', 'MDC', 'Edit action'));

                if ( Ext.isEmpty(actionRecord) || actionRecord.id != actionId ) {
                    Ext.Array.each(currentCommunicationTask.get(me.ACTIONS), function(action) {
                        if (action.id === actionId) {
                            actionRecord = action;
                            return false;
                        }
                    });
                }
                me.loadCommandToWindow(widget, actionRecord);
            };

        if (Ext.isEmpty(view)) { // fresh enter via this url
            me.getModel('Mdc.model.CommunicationTask').load(comTaskId, {
                success: function (communicationTask) {
                    currentCommunicationTask = communicationTask;
                    if (Ext.isEmpty(me.categoriesStore)) {
                        me.categoriesStore = me.getStore('Mdc.store.CommunicationTasksCategories');
                        me.categoriesStore.load({
                            scope: me,
                            callback: onEveryhingLoaded
                        });
                    } else {
                        onEveryhingLoaded();
                    }
                }
            });
        } else {
            currentCommunicationTask = view.communicationTask;
            onEveryhingLoaded();
        }
    },

    removeAction: function(grid, rowIndex, actionRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = me.getComTaskActionsView(),
            comTaskRecord = view.communicationTask,
            backUrl = router.getRoute('administration/communicationtasks/view/actions').buildUrl();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comtask.action.remove.msg', 'MDC', 'This action will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", actionRecord.get('category') + ' - ' + actionRecord.get('action')),
            scope: me,
            fn: function (state) {
                if (state === 'confirm') {
                    var item2Remove = null;
                    view.setLoading(Uni.I18n.translate('general.removing', 'DLC', 'Removing...'));
                    Ext.Array.each(comTaskRecord.get(me.ACTIONS), function(action) {
                        if ( action.categoryId === actionRecord.get('categoryId') &&
                             action.actionId === actionRecord.get('actionId') ) {
                            item2Remove = action;
                            return false;
                        }
                    });
                    if (item2Remove) {
                        Ext.Array.remove(comTaskRecord.get(me.ACTIONS), item2Remove);
                    }
                    comTaskRecord.save({
                        backUrl: backUrl,
                        success: function () {
                            window.location.href = backUrl;
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comtask.action.remove.success.msg', 'DLC', 'Action removed'));
                            view.setLoading(false);
                            grid.getStore().removeAt(rowIndex);
                            view.down('#add-communication-task-action').setDisabled(grid.getStore().getCount() === me.categoriesStore.totalCount);
                        },
                        callback: function () {
                            view.setLoading(false);
                        }
                    });
                }
            }
        });
    },

    showCommunicationTaskActionAdd: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget,
            taskModel = me.getModel('Mdc.model.CommunicationTask'),
            comTaskId = router.arguments['id'],
            currentCommunicationTask = null,
            //filter = function(categoryRecord) {
            //    var match = false;
            //    Ext.Array.each(currentCommunicationTask.get(me.ACTIONS), function(actionRecord) {
            //        match = match || (actionRecord.categoryId === categoryRecord.get('id'));
            //    });
            //    return !match;
            //},
            onCategoriesLoaded = function() {
                //me.categoriesStore.removeFilter(); // remove all previous filters
                //me.categoriesStore.filterBy(filter, me); // apply the one desired
                widget = Ext.widget('comtaskAddActionContainer', {
                    router: router,
                    cancelRoute: 'administration/communicationtasks/view/actions',
                    communicationTask: currentCommunicationTask,
                    btnAction: 'addComTaskAction',
                    btnText: Uni.I18n.translate('general.add', 'MDC', 'Add')
                    //categoriesStore: me.categoriesStore //categoriesForCombo
                });
                me.getApplication().fireEvent('loadCommunicationTask', currentCommunicationTask);
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#mdc-comtask-addAction-form').setTitle(Uni.I18n.translate('general.addAction', 'MDC', 'Add action'));
            };

        taskModel.load(comTaskId, {
            success: function (communicationTask) {
                currentCommunicationTask = communicationTask;
                if (Ext.isEmpty(me.categoriesStore)) {
                    me.categoriesStore = me.getStore('Mdc.store.CommunicationTasksCategories');
                    me.categoriesStore.load({
                        scope: me,
                        callback: onCategoriesLoaded
                    });
                } else {
                    onCategoriesLoaded();
                }
            }
        });
    },

    showCommunicationTaskCommandCategoriesAdd: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget,
            taskModel = me.getModel('Mdc.model.CommunicationTask'),
            comTaskId = router.arguments['id'],
            currentCommunicationTask = null,
            filter = function(categoryRecord) {
                var match = false;
                Ext.Array.each(currentCommunicationTask.get(me.COMMAND_CATEGORIES), function(commandCategory) {
                    match = match || (commandCategory.id === categoryRecord.get('id'));
                    if (match) return false;
                });
                return !match;
            },
            onCategoriesLoaded = function() {
                me.commandCategoriesStore.removeFilter(); // remove all previous filters
                me.commandCategoriesStore.filterBy(filter, me); // apply the one desired
                widget = Ext.widget('comtaskAddCommandCategories', {
                    router: router,
                    cancelRoute: 'administration/communicationtasks/view/commandcategories',
                    communicationTask: currentCommunicationTask,
                    store: me.commandCategoriesStore
                });
                me.getApplication().fireEvent('loadCommunicationTask', currentCommunicationTask);
                me.getApplication().fireEvent('changecontentevent', widget);
            };

        taskModel.load(comTaskId, {
            success: function (communicationTask) {
                currentCommunicationTask = communicationTask;
                if (Ext.isEmpty(me.commandCategoriesStore)) {
                    me.commandCategoriesStore = me.getStore('Mdc.store.MessageCategories');
                    me.commandCategoriesStore.load({
                        scope: me,
                        callback: onCategoriesLoaded
                    });
                } else {
                    onCategoriesLoaded();
                }
            }
        });
    },

    disableBtn: function () {
        var btn = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommand button[action=addCommand]')[0];
        btn.setDisabled(true);
    },

    removeEdit: function () {
        var categoryCombo = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommandCategoryCombo')[0],
            actionBtn = Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0],
            btns = Ext.ComponentQuery.query('comtaskCreateEdit tag-button');
        Ext.Array.each(btns, function (btn) {
            if (btn.categoryId === categoryCombo.value) {
                btn.fireEvent('closeclick', btn);
                btn.destroy();
            }
        });
        if (!Ext.isEmpty(this.commands)) {
            actionBtn.setDisabled(false);
        }
    },

    cancelEdit: function () {
        var commandView = Ext.ComponentQuery.query('comtaskCommand')[0],
            actionBtn = Ext.ComponentQuery.query('comtaskCreateEdit #createEditTask')[0];
        commandView.destroy();
        actionBtn.setDisabled(false);
    },

    createEdit: function (btn) {
        var me = this,
            editView = me.getTaskEdit(),
            form = editView.down('form').getForm(),
            formErrorsPanel = me.getTaskEdit().down('#errors'),
            model = Ext.create('Mdc.model.CommunicationTask'),
            nameField = editView.down('form').down('textfield[name=name]'),
            router = me.getController('Uni.controller.history.Router'),
            backUrl = router.getRoute('administration/communicationtasks').buildUrl();

        editView.setLoading(true);
        me.trimFields();
        if (form.isValid()) {
            var record = form.getRecord();

            if (!record) {
                record = model;
            }

            record.beginEdit();
            record.set('name', nameField.getValue());
            record.endEdit();
            formErrorsPanel.hide();
            record.save({
                backUrl: backUrl,
                success: function () {
                    window.location.href = backUrl;
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comtask.saved', 'MDC', 'Communication task saved'));
                    editView.setLoading(false);
                },
                failure: function (record, requestObject) {
                    if (requestObject.error.status === 400) {
                        formErrorsPanel.show();
                        var result = Ext.decode(requestObject.response.responseText, true);
                        if (result) {
                            form.markInvalid(result.errors);
                        }
                    }
                    editView.setLoading(false);
                }
            });
        } else {
            editView.setLoading(false);
            formErrorsPanel.show();
        }
    },

    trimFields: function () {
        var nameField = this.getTaskEdit().down('form').down('[name=name]'),
            nameValue;
        if (nameField.value) {
            nameValue = Ext.util.Format.trim(nameField.value);
            nameField.setValue(nameValue);
        }
    },

    setTooltip: function () {
        var iconIntervals = Ext.ComponentQuery.query('#radioIntervals')[0].getEl().down('img'),
            textIntervals = Uni.I18n.translate('comtask.tooltip.textIntervals', 'MDC', 'If the clock difference between the clock in the meter and the clock of the communication server is equal to or bigger than the minimum clock difference, the intervals will be marked as bad time'),
            iconEvents = Ext.ComponentQuery.query('#radioEvents')[0].getEl().down('img'),
            textEvents = Uni.I18n.translate('comtask.tooltip.textEvents', 'MDC', 'When data with a status flag comes in, meter events will be created'),
            iconFail = Ext.ComponentQuery.query('#radioFail')[0].getEl().down('img'),
            textFail = Uni.I18n.translate('comtask.tooltip.textFail', 'MDC', 'A profile configuration defines how a load profile of that configuration looks like. When the profile configuration doesn\'t match the load profile, a failure occurs');

        iconIntervals.tooltip = Ext.create('Ext.tip.ToolTip', { target: iconIntervals, html: Ext.String.htmlEncode(textIntervals) });
        iconEvents.tooltip = Ext.create('Ext.tip.ToolTip', { target: iconEvents, html: Ext.String.htmlEncode(textEvents) });
        iconFail.tooltip = Ext.create('Ext.tip.ToolTip', { target: iconFail, html: Ext.String.htmlEncode(textFail) });
    },

    loadModelToEditForm: function (taskId, widget) {
        var self = this,
            editView = self.getTaskEdit(),
            model = self.getModel('Mdc.model.CommunicationTask'),
            categoriesStore = this.getStore('Mdc.store.CommunicationTasksCategories'),
            form = editView.down('form').getForm();

        widget.setLoading(true);
        model.load(taskId, {
            success: function (record) {
                self.getTaskEdit().getCenterContainer().down().setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", record.get('name')));
                self.getApplication().fireEvent('loadCommunicationTask', record);
                form.loadRecord(record);
                widget.setLoading(false);
            }
        });
    },

    setValuesToForm: function (command, commandContainer) {
        var parametersContainer = commandContainer.down('comtaskCommandCategoryActionCombo').nextNode().nextNode();
        switch (command.categoryId) {
            case 'logbooks':
                var logValues = [];
                Ext.Array.each(command.parameters[0].value, function (item) {
                    logValues.push(item.value);
                });
                parametersContainer.setValue(logValues);
                break;
            case 'registers':
                var regValues = [];
                Ext.Array.each(command.parameters[0].value, function (item) {
                    regValues.push(item.value);
                });
                parametersContainer.setValue(regValues);
                break;
            case 'loadprofiles':
                var proValues = [];
                Ext.Array.each(command.parameters[0].value, function (item) {
                    proValues.push(item.value);
                });
                parametersContainer.down('#checkProfileTypes').setValue(proValues);
                parametersContainer.down('#radioIntervals').setValue({
                    intervals: command.parameters[2].value.toString()
                });
                parametersContainer.down('#radioEvents').setValue({
                    events: command.parameters[3].value.toString()
                });
                parametersContainer.down('#radioFail').setValue({
                    fail: command.parameters[1].value.toString()
                });
                parametersContainer.down('#disContTime').setValue(command.parameters[4].value.name);
                parametersContainer.down('#disContNum').setValue(command.parameters[4].value.value);
                break;
            case 'clock':
                switch (command.actionId) {
                    case 'set':
                        parametersContainer.down('#setMinTime').setValue(command.parameters[0].value.name);
                        parametersContainer.down('#setMinNum').setValue(command.parameters[0].value.value);
                        parametersContainer.down('#setMaxTime').setValue(command.parameters[1].value.name);
                        parametersContainer.down('#setMaxNum').setValue(command.parameters[1].value.value);
                        break;
                    case 'synchronize':
                        parametersContainer.down('#syncMinTime').setValue(command.parameters[0].value.name);
                        parametersContainer.down('#syncMinNum').setValue(command.parameters[0].value.value);
                        parametersContainer.down('#syncMaxTime').setValue(command.parameters[1].value.name);
                        parametersContainer.down('#syncMaxNum').setValue(command.parameters[1].value.value);
                        parametersContainer.down('#syncMaxTimeShift').setValue(command.parameters[2].value.name);
                        parametersContainer.down('#syncMaxNumShift').setValue(command.parameters[2].value.value);
                        break;
                }
                break;
        }
    },

    addActionComboBox: function (comboBox, newValue) {
        var me = this,
            view = me.getComTaskAddActionsView(),
            form = view.down('#mdc-comtask-addAction-form'),
            addActionBtn = form.down('#mdc-comtask-addAction-actionBtn'),
            actionsStore = me.getStore('Mdc.store.CommunicationTasksActions'),
            actionComboBox;

        actionComboBox = form.insert(2, {
            xtype: 'comtaskCommandCategoryActionCombo',
            itemId: 'mdc-comtask-addAction-action-combo',
            width: 500
        });

        actionsStore.getProxy().setExtraParam('category', newValue);
        actionsStore.load(function (records) {
            addActionBtn.disable();
            comboBox.on('change', function () {
                actionComboBox.destroy();
                form.down('#mdc-comtask-addAction-parameters-label').hide();
                addActionBtn.disable();
            }, comboBox, {single: true});
            if (records.length === 1) {
                actionComboBox.setValue(records[0]);
                addActionBtn.enable();
            }
        });
    },

    addActionCombo: function (combo, newValue) {
        var commandContainer = combo.up('comtaskCommand'),
            window = combo.up('#comTaskAddCommandWindow'),
            actionsStore = Ext.getStore('Mdc.store.CommunicationTasksActions'),
            actionCombo;

        window.down('#addCommandToTask').enable();

        actionCombo = commandContainer.add({
            xtype: 'comtaskCommandCategoryActionCombo'
        });

        actionsStore.getProxy().setExtraParam('category', newValue);
        actionsStore.load(function (records) {
            combo.on('change', function () {
                actionCombo.destroy();
            }, combo, {single: true});
            if (records.length === 1) {
                actionCombo.setValue(records[0]);
            }
        });
    },

    addActionParameters: function (actionCombo, newValue) {
        var me = this,
            view = me.getComTaskAddActionsView(),
            form = view.down('#mdc-comtask-addAction-form'),
            btnAction = form.down('#mdc-comtask-addAction-actionBtn').action,
            parametersLabelField = form.down('#mdc-comtask-addAction-parameters-label'),
            addActionBtn = form.down('#mdc-comtask-addAction-actionBtn'),
            category = form.down('#mdc-comtask-addAction-command-category-combo').getValue(),
            valuesArr = [],
            parametersContainer = me.chooseCommandParameters(category, newValue),
            parametersComponent;

        addActionBtn.enable();

        if (parametersContainer) {
            view.setLoading(true);
            parametersLabelField.show();
            parametersComponent = form.insert(4, parametersContainer);
            switch (parametersContainer.xtype) {
                case 'communication-tasks-logbookscombo':
                    if (btnAction === 'addComTaskAction') {
                        Ext.Array.each(me.logbookTypesStore.getRange(), function (record) {
                            valuesArr.push(record.data.id);
                        });
                        parametersComponent.setValue(valuesArr);
                    }
                    view.setLoading(false);
                    break;
                case 'communication-tasks-registerscombo':
                    if (btnAction === 'addComTaskAction') {
                        Ext.Array.each(me.registerGroupsStore.getRange(), function (record) {
                            valuesArr.push(record.data.id);
                        });
                        parametersComponent.setValue(valuesArr);
                    }
                    view.setLoading(false);
                    break;
                case 'communication-tasks-profilescombo':
                    if (btnAction === 'addComTaskAction') {
                        Ext.Array.each(me.loadProfileTypesStore.getRange(), function (record) {
                            valuesArr.push(record.data.id);
                        });
                        parametersComponent.down('#checkProfileTypes').setValue(valuesArr);
                    }
                    view.setLoading(false);
                    break;
                default:
                    view.setLoading(false);
                    break;
            }

            actionCombo.on('change', function () {
                parametersContainer && parametersContainer.destroy();
            }, actionCombo, {single: true});
            actionCombo.on('destroy', function () {
                parametersContainer && parametersContainer.destroy();
            }, actionCombo, {single: true});

        } else {
            parametersLabelField.hide();
        }
    },

    addCommandParameters: function (combo, newValue) {
        var commandContainer = combo.up('comtaskCommand'),
            category = commandContainer.down('comtaskCommandCategoryCombo').getValue(),
            valuesArr = [],
            parametersContainer = this.chooseCommandParameters(category, newValue),
            window = combo.up('#comTaskAddCommandWindow');


        if (parametersContainer) {
            window.setLoading(true);
            var parametersComponent = commandContainer.add(parametersContainer);
            switch (parametersContainer.xtype) {
                case 'communication-tasks-logbookscombo':
                    parametersComponent.getStore().load({
                        callback: function (records) {
                            if (window.btnAction === 'add') {
                                Ext.Array.each(records, function (rec) {
                                    valuesArr.push(rec.data.id);
                                });
                                parametersComponent.setValue(valuesArr);
                            }
                            window.setLoading(false);
                        }
                    });
                    break;
                case 'communication-tasks-registerscombo':
                    parametersComponent.getStore().load({
                        callback: function (records) {
                            if (window.btnAction === 'add') {
                                Ext.Array.each(records, function (rec) {
                                    valuesArr.push(rec.data.id);
                                });
                                parametersComponent.setValue(valuesArr);
                            }
                            window.setLoading(false);
                        }
                    });
                    break;
                case 'communication-tasks-profilescombo':
                    parametersComponent.down('#checkProfileTypes').getStore().load({
                        callback: function (records) {
                            if (window.btnAction === 'add') {
                                Ext.Array.each(records, function (rec) {
                                    valuesArr.push(rec.data.id);
                                });
                                parametersComponent.down('#checkProfileTypes').setValue(valuesArr);
                            }
                            window.setLoading(false);
                        }
                    });
                    break;
                default:
                    window.setLoading(false);
                    break;
            }
        }

        combo.on('change', function () {
            parametersContainer && parametersContainer.destroy();
        }, combo, {single: true});
        combo.on('destroy', function () {
            parametersContainer && parametersContainer.destroy();
        }, combo, {single: true});
    },

    chooseCommandParameters: function (category, action) {
        var me = this,
            xtype;
        switch (category) {
            case 'logbooks':
                if (Ext.isEmpty(me.logbookTypesStore)) {
                    me.logbookTypesStore = me.getStore('Mdc.store.LogbookTypes');
                    me.logbookTypesStore.getProxy().pageParam = false;
                    me.logbookTypesStore.getProxy().startParam = false;
                    me.logbookTypesStore.getProxy().limitParam = false;
                    me.logbookTypesStore.load();
                }
                xtype = 'communication-tasks-logbookscombo';
                break;
            case 'registers':
                if (Ext.isEmpty(me.registerGroupsStore)) {
                    me.registerGroupsStore = me.getStore('Mdc.store.RegisterGroups');
                    me.registerGroupsStore.getProxy().pageParam = false;
                    me.registerGroupsStore.getProxy().startParam = false;
                    me.registerGroupsStore.getProxy().limitParam = false;
                    me.registerGroupsStore.load();
                }
                xtype = 'communication-tasks-registerscombo';
                break;
            case 'loadprofiles':
                if (Ext.isEmpty(me.loadProfileTypesStore)) {
                    me.loadProfileTypesStore = me.getStore('Mdc.store.LoadProfileTypes');
                    me.loadProfileTypesStore.getProxy().pageParam = false;
                    me.loadProfileTypesStore.getProxy().startParam = false;
                    me.loadProfileTypesStore.getProxy().limitParam = false;
                    me.loadProfileTypesStore.load();
                }
                if (Ext.isEmpty(me.timeUnitsStore)) {
                    me.timeUnitsStore = me.getStore('Mdc.store.TimeUnits');
                    me.timeUnitsStore.load();
                }
                xtype = 'communication-tasks-profilescombo';
                break;
            case 'clock':
                if (Ext.isEmpty(me.timeUnitsStore)) {
                    me.timeUnitsStore = me.getStore('Mdc.store.TimeUnits');
                    me.timeUnitsStore.load();
                }
                switch (action) {
                    case 'set':
                        xtype = 'communication-tasks-parameters-clock-set';
                        break;
                    case 'synchronize':
                        xtype = 'communication-tasks-parameters-clock-synchronize';
                        break;
                }
                break;
            default:
                break;
        }
        if (xtype) {
            return Ext.widget(xtype, {labelWidth: 300, width: 500});
        }
        return null;
    },

    addOrEditAction: function(button) {
        this.doAddOrEditAction(button.action === 'addComTaskAction');
    },

    doAddOrEditAction: function(busyAdding /*true=Add - false=Edit*/) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = me.getComTaskAddActionsView(),
            form = view.down('#mdc-comtask-addAction-form'),
            errorMsgPnl = view.down('uni-form-error-message'),
            parameterErrorMsgPnl = form.down('#mdc-comtask-addAction-parameter-error-message'),
            categoryCombo = form.down('comtaskCommandCategoryCombo'),
            categoryId = categoryCombo.value,
            actionCombo = form.down('comtaskCommandCategoryActionCombo'),
            actionId = actionCombo.value,
            comTaskRecord = view.communicationTask,
            allValid = true,
            newAction = {},
            parametersContainer,
            backUrl = router.getRoute('administration/communicationtasks/view/actions').buildUrl();

        view.setLoading(true);
        errorMsgPnl.hide();

        // 1. Check if a categoryId was given
        if (Ext.isEmpty(categoryId)) {
            errorMsgPnl.show();
            categoryCombo.markInvalid(me.ERROR_MESSAGE_FIELD_REQUIRED);
            allValid = false;
        }
        // 2. Check for already assigned categories
        if (busyAdding) {
            Ext.Array.each(comTaskRecord.get(me.ACTIONS), function (action) {
                if (action.categoryId === categoryId) {
                    errorMsgPnl.show();
                    categoryCombo.markInvalid(
                        Uni.I18n.translate('comtask.already.has.category', 'MDC', 'The communication task already has this category')
                    );
                    allValid = false;
                    return false; // break out of the each() loop
                }
            });
        }
        // 3. Check if an actionId was given
        if (Ext.isEmpty(actionId)) {
            errorMsgPnl.show();
            actionCombo.markInvalid(me.ERROR_MESSAGE_FIELD_REQUIRED);
            allValid = false;
        }

        if (allValid) {
            newAction.categoryId = categoryId;
            newAction.category = categoryCombo.getRawValue();
            newAction.actionId = actionId;
            newAction.action = actionCombo.getRawValue();
            newAction.parameters = [];

            parametersContainer = actionCombo.nextNode().nextNode();
            if (!Ext.isEmpty(parametersContainer)) {
                switch (parametersContainer.xtype) {
                    case 'communication-tasks-logbookscombo':
                        if (parametersContainer.value.length < 1) {
                            errorMsgPnl.show();
                            parametersContainer.markInvalid(me.ERROR_MESSAGE_FIELD_REQUIRED);
                            allValid = false;
                        }
                        me.fillLogbooks(newAction, parametersContainer);
                        break;
                    case 'communication-tasks-registerscombo':
                        if (parametersContainer.value.length < 1) {
                            errorMsgPnl.show();
                            parametersContainer.markInvalid(me.ERROR_MESSAGE_FIELD_REQUIRED);
                            allValid = false;
                        }
                        me.fillRegisters(newAction, parametersContainer);
                        break;
                    case 'communication-tasks-profilescombo':
                        if (parametersContainer.down('#checkProfileTypes').value.length < 1) {
                            errorMsgPnl.show();
                            parametersContainer.down('#checkProfileTypes').markInvalid(me.ERROR_MESSAGE_FIELD_REQUIRED);
                            allValid = false;
                        }
                        me.fillProfiles(newAction, parametersContainer);
                        break;
                    case 'communication-tasks-parameters-clock-set':
                        me.fillClockSet(newAction, parametersContainer);
                        break;
                    case 'communication-tasks-parameters-clock-synchronize':
                        me.fillClockSync(newAction, parametersContainer);
                        break;
                    default:
                        break;
                }
            }

            if (allValid) {
                if (busyAdding) {
                    // Add the newAction to the comTask
                    comTaskRecord.get(me.ACTIONS).push(newAction);
                } else {
                    // Replace the old with the new action
                    var item2Remove;
                    Ext.Array.each(comTaskRecord.get(me.ACTIONS), function(action) {
                        if (action.categoryId === newAction.categoryId) {
                            item2Remove = action;
                            return false;
                        }
                    });
                    if (item2Remove) {
                        Ext.Array.remove(comTaskRecord.get(me.ACTIONS), item2Remove);
                    }
                    comTaskRecord.get(me.ACTIONS).push(newAction);
                }
                comTaskRecord.save({
                    backUrl: backUrl,
                    success: function () {
                        window.location.href = backUrl;
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comtask.saved', 'MDC', 'Communication task saved'));
                        view.setLoading(false);
                    },
                    failure: function (record, requestObject) {
                        if (requestObject.error.status === 400) {
                            errorMsgPnl.show();
                            var result = Ext.decode(requestObject.response.responseText, true);
                            if (result) {
                                Ext.Array.each(result.errors, function(error) {
                                    if (error.id === 'protocolTasks') {
                                        parameterErrorMsgPnl.add({
                                            xtype: 'container',
                                            html: '<span style="color: #eb5642">' + Ext.String.htmlEncode(error.msg) + '</span>'
                                        });
                                        parameterErrorMsgPnl.show();
                                    }
                                });
                                form.markInvalid(result.errors);
                            }
                        }
                        view.setLoading(false);
                    }
                });
            } else {
                view.setLoading(false);
            }
        }else {
            view.setLoading(false);
        }
    },

    onCommandCategoriesGridSelectionChange: function() {
        var me = this,
            infoField = me.getSelectedCommandCategoriesInfoField(),
            addCommandCategoriesGrid = me.getAddCommandCategoriesGrid(),
            selectedQuantity = addCommandCategoriesGrid.getView().getSelectionModel().getSelection().length;

        infoField.setValue(
            Uni.I18n.translatePlural('general.nrOfCommandCategories.selected', selectedQuantity, 'MDC',
                'No command categories selected', '{0} command category selected', '{0} command categories selected')
        );

        me.getUncheckAllBtn().setDisabled(selectedQuantity < 1);
        addCommandCategoriesGrid.getView().restoreScrollState();
    },

    addCommandToModel: function (button) {
        var self = this,
            window = button.up('#comTaskAddCommandWindow'),
            commandContainer = window.down('comtaskCommand'),
            actionContainer = commandContainer.down('comtaskCommandCategoryActionCombo'),
            categoryContainer = commandContainer.down('comtaskCommandCategoryCombo'),
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            categoryId = categoryContainer.value,
            category = categoryContainer.getRawValue(),
            newCommand = editView.down('#newCommand'),
            btns = Ext.ComponentQuery.query('comtaskCreateEdit tag-button'),
            parametersContainer,
            btnToRemoveAfterValidation,
            numItem,
            offset = 0,
            couldAdd = true,
            protocol = {};


        Ext.Array.each(btns, function (btn) {
            if (btn.categoryId === categoryId) {
                switch (button.action) {
                    case 'add':
                        categoryContainer.markInvalid(
                            Uni.I18n.translate('comtask.already.has.category', 'MDC', 'The communication task already has this category')
                        );
                        couldAdd = false;
                        break;
                    case 'edit':
                        btnToRemoveAfterValidation = btn;
                        offset = 1;
                        break;
                }
                return false; // break out of each()
            }
        });

        if (Ext.isEmpty(categoryId)) {
            categoryContainer.markInvalid(me.ERROR_MESSAGE_FIELD_REQUIRED);
            couldAdd = false;
        }

        if (actionContainer) {
            parametersContainer = actionContainer.nextNode();
            protocol.categoryId = categoryId;
            protocol.category = category;
            protocol.actionId = actionContainer.value;
            protocol.action = actionContainer.getRawValue();
            protocol.parameters = [];


            if (Ext.isEmpty(protocol.actionId)) {
                actionContainer.markInvalid(me.ERROR_MESSAGE_FIELD_REQUIRED);
                couldAdd = false;
            }

            Ext.Array.each(self.commands, function (item) {
                if (item.categoryId === protocol.categoryId) {
                    numItem = self.commands.indexOf(item);
                }
            });

            Ext.Array.each(self.recordCommands, function (item) {
                if (item.categoryId === protocol.categoryId && item.actionId === protocol.actionId) {
                    protocol.id = item.id;
                    return false; // break out of each()
                }
            });

            if (!Ext.isEmpty(parametersContainer)) {
                switch (parametersContainer.xtype) {
                    case 'communication-tasks-logbookscombo':
                        if (parametersContainer.value.length < 1) {
                            parametersContainer.markInvalid(self.ERROR_MESSAGE_FIELD_REQUIRED);
                            couldAdd = false;
                        }
                        self.fillLogbooks(protocol, parametersContainer);
                        break;
                    case 'communication-tasks-registerscombo':
                        if (parametersContainer.value.length < 1) {
                            parametersContainer.markInvalid(self.ERROR_MESSAGE_FIELD_REQUIRED);
                            couldAdd = false;
                        }
                        self.fillRegisters(protocol, parametersContainer);
                        break;
                    case 'communication-tasks-profilescombo':
                        if (parametersContainer.down('#checkProfileTypes').value.length < 1) {
                            parametersContainer.down('#checkProfileTypes').markInvalid(self.ERROR_MESSAGE_FIELD_REQUIRED);
                            couldAdd = false;
                        }
                        self.fillProfiles(protocol, parametersContainer);
                        break;
                    case 'communication-tasks-parameters-clock-set':
                        self.fillClockSet(protocol, parametersContainer);
                        break;
                    case 'communication-tasks-parameters-clock-synchronize':
                        self.fillClockSync(protocol, parametersContainer);
                        break;
                    default:
                        self.commands.push(protocol);
                        break;
                }
            } else {
                self.commands.push(protocol);
            }
        }
        if (couldAdd) {
            if (btnToRemoveAfterValidation) {
                btnToRemoveAfterValidation.destroy()
            }

            if (self.commands.length === 1) {
                editView.down('#noActionsAddedMsg').hide();
                editView.down('#addCommandsToTask').hide();
                editView.down('#addAnotherCommandsButton').show();
            } else if ( (self.commands.length - offset) === categoryContainer.getStore().totalCount ) {
                editView.down('#addAnotherCommandsButton').hide();
            }

            self.addTagButton(protocol);
            window.destroy();

            if (!Ext.isEmpty(numItem)) {
                self.commands.splice(numItem, 1);
            }

        } else {
            self.commands.pop()
        }
    },

    fillLogbooks: function (action, parametersContainer) {
        var self = this,
            logbooks = {};
        logbooks.name = "logbooktypeids";
        logbooks.value = [];
        Ext.Array.each(parametersContainer.value, function (item) {
            var logbook = {};
            logbook.name = "logbooktypeid";
            logbook.value = item;
            logbooks.value.push(logbook);
        });
        action.parameters.push(logbooks);
        self.commands.push(action);
    },

    fillRegisters: function (action, parametersContainer) {
        var self = this,
            registers = {};
        registers.name = "registergroupids";
        registers.value = [];
        Ext.Array.each(parametersContainer.value, function (item) {
            var register = {};
            register.name = "registergroupid";
            register.value = item;
            registers.value.push(register);
        });
        action.parameters.push(registers);
        self.commands.push(action);
    },

    fillProfiles: function (action, parametersContainer) {
        var self = this,
            profiles = {},
            intervals = {},
            events = {},
            fail = {},
            intervalsBoolean = false,
            eventsBoolean = false,
            failBoolean = false;
        profiles.name = "loadprofiletypeids";
        profiles.value = [];
        Ext.Array.each(parametersContainer.down('#checkProfileTypes').value, function (item) {
            var profile = {};
            profile.name = "loadprofiletypeid";
            profile.value = item;
            profiles.value.push(profile);
        });
        action.parameters.push(profiles);
        fail.name = "failifconfigurationmismatch";
        if (parametersContainer.down('#radioFail').getValue().fail === 'true') {
            failBoolean = true;
        }
        fail.value = failBoolean;
        action.parameters.push(fail);
        intervals.name = "markintervalsasbadtime";
        if (parametersContainer.down('#radioIntervals').getValue().intervals === 'true') {
            intervalsBoolean = true;
        }
        intervals.value = intervalsBoolean;
        action.parameters.push(intervals);
        events.name = "createmetereventsfromflags";
        if (parametersContainer.down('#radioEvents').getValue().events === 'true') {
            eventsBoolean = true;
        }
        events.value = eventsBoolean;
        action.parameters.push(events);
        var minTime = {},
            minTimeValue = {};
        minTime.name = "minclockdiffbeforebadtime";
        minTimeValue.name = parametersContainer.down('#disContTime').value;
        minTimeValue.value = parseInt(parametersContainer.down('#disContNum').getValue());
        minTime.value = minTimeValue;
        action.parameters.push(minTime);
        self.commands.push(action);
    },

    fillClockSet: function (action, parametersContainer) {
        var self = this,
            setMinTime = {},
            setMaxTime = {},
            setMinTimeValue = {},
            setMaxTimeValue = {};
        setMinTime.name = "minimumclockdifference";
        setMaxTime.name = "maximumclockdifference";
        setMinTimeValue.name = parametersContainer.down('#setMinTime').value;
        setMinTimeValue.value = parseInt(parametersContainer.down('#setMinNum').getValue());
        setMaxTimeValue.name = parametersContainer.down('#setMaxTime').value;
        setMaxTimeValue.value = parseInt(parametersContainer.down('#setMaxNum').getValue());
        setMinTime.value = setMinTimeValue;
        setMaxTime.value = setMaxTimeValue;
        action.parameters.push(setMinTime);
        action.parameters.push(setMaxTime);
        self.commands.push(action);
    },

    fillClockSync: function (action, parametersContainer) {
        var self = this,
            syncMinTime = {},
            syncMaxTime = {},
            syncMinTimeValue = {},
            syncMaxTimeValue = {},
            syncMaxTimeShift = {},
            syncMaxTimeShiftValue = {};
        syncMinTime.name = "minimumclockdifference";
        syncMaxTime.name = "maximumclockdifference";
        syncMaxTimeShift.name = "maximumclockshift";
        syncMinTimeValue.name = parametersContainer.down('#syncMinTime').value;
        syncMinTimeValue.value = parseInt(parametersContainer.down('#syncMinNum').getValue());
        syncMaxTimeValue.name = parametersContainer.down('#syncMaxTime').value;
        syncMaxTimeValue.value = parseInt(parametersContainer.down('#syncMaxNum').getValue());
        syncMaxTimeShiftValue.name = parametersContainer.down('#syncMaxTimeShift').value;
        syncMaxTimeShiftValue.value = parseInt(parametersContainer.down('#syncMaxNumShift').getValue());
        syncMinTime.value = syncMinTimeValue;
        syncMaxTime.value = syncMaxTimeValue;
        syncMaxTimeShift.value = syncMaxTimeShiftValue;
        action.parameters.push(syncMinTime);
        action.parameters.push(syncMaxTime);
        action.parameters.push(syncMaxTimeShift);
        self.commands.push(action);
    },

    loadCommandToWindow: function (window, command) {
        var me = this,
            actionsStore = Ext.getStore('Mdc.store.CommunicationTasksActions');

        window.down('comtaskCommandCategoryCombo').setValue(command.categoryId);
        window.down('comtaskCommandCategoryCombo').setRawValue(command.category);
        window.down('comtaskCommandCategoryCombo').disable();
        actionsStore.load({
            callback: function () {
                window.down('comtaskCommandCategoryActionCombo').setValue(command.actionId);
                window.down('comtaskCommandCategoryActionCombo').setRawValue(command.action);
                window.down('comtaskCommandCategoryActionCombo').disable();
                if (!Ext.isEmpty(command.parameters)) {
                    me.setValuesToForm(command, window);
                }
            }
        });
    },

    addTagButton: function (command) {
        var self = this;
        self.getCommandNames().add({
            xtype: 'tag-button',
            itemId: 'tagBtn' + command.categoryId,
            text: command.category + ' - ' + command.action,
            margin: '5 0 5 0',
            width: 200,
            categoryId: command.categoryId,
            handler: function () {
                var window = Ext.create('Mdc.view.setup.comtasks.ComTaskAddCommandWindow', {
                    title: Uni.I18n.translate('communicationtasks.task.editAction', 'MDC', 'Edit action'),
                    btnAction: 'edit',
                    btnText: Uni.I18n.translate('general.save', 'MDC', 'Save')
                });
                self.loadCommandToWindow(window, command);
            },
            listeners: {
                closeclick: function (me) {
                    var numItem;
                    Ext.Array.each(self.commands, function (item) {
                        if (item.categoryId === me.categoryId) {
                            numItem = self.commands.indexOf(item);
                        }
                    });
                    if (!Ext.isEmpty(numItem)) {
                        self.commands.splice(numItem, 1);
                    }
                    self.getTaskEdit().down('#addAnotherCommandsButton').show();
                    if (self.commands.length < 1) {
                        self.getTaskEdit().down('#noActionsAddedMsg').show();
                        self.getTaskEdit().down('#addCommandsToTask').show();
                        self.getTaskEdit().down('#addAnotherCommandsButton').hide();
                    }
                }
            }
        });
    },

    forwardToPreviousPage: function () {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();
        router.getRoute(splittedPath.join('/')).forward();
    }

});
