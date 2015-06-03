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
        'Mdc.view.setup.comtasks.ComTaskAddCommandWindow'
    ],
    refs: [
        { ref: 'tasksView', selector: 'comtaskSetup' },
        { ref: 'itemPanel', selector: 'comtaskPreview' },
        { ref: 'tasksGrid', selector: 'comtaskGrid' },
        { ref: 'nameField', selector: '#comtaskName' },
        { ref: 'commandsField', selector: '#comtaskCommands' },
        { ref: 'gridPagingToolbarTop', selector: 'comtaskGrid pagingtoolbartop' },
        { ref: 'taskEdit', selector: 'comtaskCreateEdit' },
        { ref: 'commandNames', selector: 'comtaskCreateEdit [name=commandnames]' },
        { ref: 'commandFields', selector: 'comtaskCreateEdit [name=commandfields]' }
    ],
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
            'comtaskCreateEdit #addCommandsToTask': {
                click: this.showAddCommandPopUp
            },
            'comtaskCreateEdit #addAnotherCommandsButton': {
                click: this.showAddCommandPopUp
            },
            '#comTaskAddCommandWindow comtaskCommandCategoryActionCombo': {
                change: this.addCommandParameters
            },
            '#comTaskAddCommandWindow #addCommandToTask': {
                click: this.addCommandToModel
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
            }
        });
        this.store = this.getStore('Mdc.store.CommunicationTasks');
        this.commands = [];
    },

    showCommunicationTasksView: function () {
        var widget = Ext.widget('comtaskSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    chooseCommunicationTasksAction: function (menu, item) {
        var tasksGrid = this.getTasksGrid(),
            record = tasksGrid.getView().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'edit':
                var router = this.getController('Uni.controller.history.Router');
                router.getRoute('administration/communicationtasks/edit').forward({id: record.get('id')});
                break;
            case 'delete':
                this.showConfirmationPanel();
                //this.deleteTask(record.get('id'));
                break;
        }
    },

    showAddCommandPopUp: function (btn) {
        var self = this,
            categoriesStore = this.getStore('Mdc.store.CommunicationTasksCategories'),
            widget = btn.up('comtaskCreateEdit'),
            window;
        widget.setLoading(true);
        categoriesStore.load({
            scope: this,
            callback: function () {
                widget.setLoading(false);
                window = Ext.create('Mdc.view.setup.comtasks.ComTaskAddCommandWindow', {
                    title: Uni.I18n.translate('communicationtasks.task.addCommand', 'MDC', 'Add action'),
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
                    itemPanel.down('#comtaskPreviewFieldsPanel').loadRecord(record);
                    itemPanel.down('#comtaskPreviewFieldsPanel').setLoading(false);
                }
            }
        });
    },

    showConfirmationPanel: function () {
        var me = this,
            tasksView = me.getTasksView(),
            grid = tasksView.down('grid'),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + lastSelected.get('name') + "'?",
            msg: Uni.I18n.translate('comtask.remove.confirmation.msg', 'MDC', 'This communication task will no longer be available'),
            config: {
                me: me
            },
            fn: me.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            tasksView = me.getTasksView(),
            grid = tasksView.down('grid'),
            model = grid.getView().getSelectionModel().getLastSelected(),
            widget = me.getTasksView();

        if (state === 'confirm') {
            widget.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
            model.destroy({
                success: function () {
                    widget.setLoading(false);
                    me.getApplication().fireEvent('acknowledge',Uni.I18n.translate('comtasks.removeSuccessMsg', 'MDC', 'Communciation task removed'));
                },
                failure: function(response){
                    var json;
                    json = Ext.decode(response.responseText, true);
                    if (json && json.message) {
                        me.getApplication().getController(
                            'Uni.controller.Error').showError(Uni.I18n.translate('comtasks.removeErrorMsg', 'MDC', 'Error during removal of communication task'),
                            json.message
                        );
                    }
                }
            });
        }
    },

    deleteTask: function (id) {
        var self = this,
            tasksView = self.getTasksView(),
            grid = tasksView.down('grid'),
            record = grid.getSelectionModel().getLastSelected(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: self.getTasksView()
            });

        Ext.create('Uni.view.window.Confirmation').show({
            title: Ext.String.format(Uni.I18n.translate('comtask.remove.confirmation.title', 'MDC', 'Remove \'{0}\'?'), record.data.name),
            msg: Uni.I18n.translate('comtask.remove.confirmation.msg', 'MDC', 'This communication task will no longer be available'),
            fn: function (state) {
                if (state === 'confirm') {
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/cts/comtasks/' + id,
                        method: 'DELETE',
                        success: function () {
                            self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comtasks.removeSuccessMsg', 'MDC', 'Communciation task removed'));
                            self.store.loadPage(1, {
                                callback: function () {
                                    grid.getSelectionModel().select(0);
                                }
                            });
                        },
                        failure: function (response) {
                            if (response.status == 400) {
                                var result = Ext.decode(response.responseText, true),
                                    title = Ext.String.format(Uni.I18n.translate('comtask.remove.failed', 'MDC', 'Failed to remove {0}'), record.data.name),
                                    message = Uni.I18n.translate('general.server.error', 'MDC', 'Server error');
                                if (!Ext.isEmpty(response.statusText)) {
                                    message = response.statusText;
                                }
                                if (result && result.message) {
                                    message = result.message;
                                } else if (result && result.error) {
                                    message = result.error;
                                }
                                self.getApplication().getController('Uni.controller.Error').showError(title, message);
                            }
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                }
            }
        });
    },

    showCommunicationTasksCreateEdit: function () {
        var router = this.getController('Uni.controller.history.Router'),
            widget = Ext.widget('comtaskCreateEdit'),
            taskId = router.arguments['id'],
            connectedGrid = widget.down('#messagesConnectedGrid'),
            allMessagesStore = connectedGrid.getAllItemsStore(),
            selectedMessagesStore = connectedGrid.getSelectedItemsStore();

        allMessagesStore.removeAll();
        selectedMessagesStore.removeAll();
        this.getApplication().fireEvent('changecontentevent', widget);
        this.getTaskEdit().down('toolbar').getComponent('createEditTask').setText(
            Ext.isEmpty(taskId) ? Uni.I18n.translate('general.add', 'MDC', 'Add') : Uni.I18n.translate('general.save', 'MDC', 'Save')
        );
        this.getTaskEdit().down('toolbar').getComponent('createEditTask').action = Ext.isEmpty(taskId) ? 'add' : 'save';

        if (taskId) {
            this.getTaskEdit().getCenterContainer().down().setTitle(Uni.I18n.translate('comtask.edit', 'MDC', 'Edit communication task'));
            this.commands = [];
            this.loadModelToEditForm(taskId, widget, selectedMessagesStore, allMessagesStore);
        } else {
            this.getTaskEdit().getCenterContainer().down().setTitle(Uni.I18n.translate('comtask.create', 'MDC', 'Add communication task'));
            this.commands = [];
            allMessagesStore.load();
        }
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
            if (btn.category === categoryCombo.value) {
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
        var self = this,
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            formErrorsPanel = self.getTaskEdit().down('#errors'),
            model = Ext.create('Mdc.model.CommunicationTask'),
            nameField = editView.down('form').down('textfield[name=name]'),
            selectedMessagesStore = editView.down('#messagesConnectedGrid').getSelectedItemsStore(),
            protocolTasksErrorMessage = editView.down('#protocolTasksErrorMessage'),
            messages = [];

        editView.setLoading(true);
        self.trimFields();
        if (form.isValid()) {
            var record = form.getRecord();

            if (!record) {
                record = model;
            }

            selectedMessagesStore.each(function (record) {
                messages.push({id: record.get('id')});
            });
            record.beginEdit();
            record.set('name', nameField.getValue());
            record.set('commands', self.commands);
            record.set('messages', messages);
            record.endEdit();
            formErrorsPanel.hide();
            protocolTasksErrorMessage.removeAll();
            protocolTasksErrorMessage.hide();
            record.save({
                success: function () {
                    window.location.href = '#/administration/communicationtasks';
                    self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comtask.saved', 'MDC', 'Communication task saved'));
                    self.commands = [];
                    editView.setLoading(false);
                },
                failure: function (record, requestObject) {
                    if (requestObject.error.status === 400) {
                        formErrorsPanel.show();

                        var result = Ext.decode(requestObject.response.responseText, true);
                        if (result) {
                            Ext.Array.each(result.errors, function(error) {
                                if (error.id === 'protocolTasks') {
                                    protocolTasksErrorMessage.add({
                                        xtype: 'container',
                                        html: '<span style="color: #eb5642">' + Ext.String.htmlEncode(error.msg) + '</span>'
                                    });
                                    protocolTasksErrorMessage.show();
                                }
                            });
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

    loadModelToEditForm: function (id, widget, selectedMessagesStore, allMessagesStore) {
        var self = this,
            editView = self.getTaskEdit(),
            model = self.getModel('Mdc.model.CommunicationTask'),
            categoriesStore = this.getStore('Mdc.store.CommunicationTasksCategories'),
            form = editView.down('form').getForm();

        widget.setLoading(true);
        allMessagesStore.load({
            params: {availableFor: id},
            callback: function () {
                model.load(id, {
                    success: function (record) {
                        self.getTaskEdit().getCenterContainer().down().setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + record.get('name') + "'"  );
                        self.getApplication().fireEvent('loadCommunicationTask', record);
                        form.loadRecord(record);
                        Ext.Array.each(record.get('commands'), function (command) {
                            self.addTagButton(command);
                            widget.down('#addCommandsToTask').hide();
                            widget.down('#addAnotherCommandsButton').show();
                        });
                        Ext.Array.each(record.get('messages'), function (message) {
                            selectedMessagesStore.add(message);
                        });
                        self.commands = record.get('commands');
                        categoriesStore.load({
                            scope: this,
                            callback: function () {
                                if (self.commands.length === categoriesStore.totalCount) {
                                    widget.down('#addAnotherCommandsButton').hide();
                                }
                                widget.setLoading(false);
                            }
                        });
                    }
                })
            }
        });

    },

    setValuesToForm: function (command, commandContainer) {
        var parametersContainer = commandContainer.down('comtaskCommandCategoryActionCombo').nextNode();
        switch (command.category) {
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
                switch (command.action) {
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
        var xtype,
            self = this,
            logbooksStore = self.getStore('Mdc.store.LogbookTypes'),
            profilesStore = self.getStore('Mdc.store.LoadProfileTypes'),
            registersStore = self.getStore('Mdc.store.RegisterGroups');
        switch (category) {
            case 'logbooks':
                logbooksStore.getProxy().pageParam = false;
                logbooksStore.getProxy().startParam = false;
                logbooksStore.getProxy().limitParam = false;
                logbooksStore.load();
                xtype = 'communication-tasks-logbookscombo';
                break;
            case 'registers':
                registersStore.getProxy().pageParam = false;
                registersStore.getProxy().startParam = false;
                registersStore.getProxy().limitParam = false;
                registersStore.load();
                xtype = 'communication-tasks-registerscombo';
                break;
            case 'topology':
                break;
            case 'loadprofiles':
                profilesStore.load();
                xtype = 'communication-tasks-profilescombo';
                break;
            case 'clock':
                switch (action) {
                    case 'set':
                        xtype = 'communication-tasks-parameters-clock-set';
                        break;
                    case 'synchronize':
                        xtype = 'communication-tasks-parameters-clock-synchronize';
                        break;
                }
                break;

        }
        if (xtype) {
            return Ext.widget(xtype);
        }
        return null;
    },

    addCommandToModel: function (button) {
        var self = this,
            window = button.up('#comTaskAddCommandWindow'),
            commandContainer = window.down('comtaskCommand'),
            actionContainer = commandContainer.down('comtaskCommandCategoryActionCombo'),
            categoryContainer = commandContainer.down('comtaskCommandCategoryCombo'),
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            category = categoryContainer.value,
            newCommand = editView.down('#newCommand'),
            btns = Ext.ComponentQuery.query('comtaskCreateEdit tag-button'),
            parametersContainer,
            btnToRemoveAfterValidation,
            action,
            numItem,
            couldAdd = true,
            protocol = {};


        Ext.Array.each(btns, function (btn) {
            if (btn.category === category) {
                switch (button.action) {
                    case 'add':
                        categoryContainer.markInvalid(
                            Uni.I18n.translate('comtask.already.has.category', 'MDC', 'The communication task already has this category')
                        );
                        couldAdd = false;
                        break;
                    case 'edit':
                        btnToRemoveAfterValidation = btn;
                        break;
                }
            }
        });

        if (Ext.isEmpty(category)) {
            categoryContainer.markInvalid(
                Uni.I18n.translate('general.required.field', 'MDC', 'This is a required field')
            );
            couldAdd = false;
        }

        if (actionContainer) {
            action = actionContainer.value;
            parametersContainer = actionContainer.nextNode();
            protocol.category = category;
            protocol.action = action;
            protocol.parameters = [];

            if (Ext.isEmpty(protocol.action)) {
                actionContainer.markInvalid(
                    Uni.I18n.translate('general.required.field', 'MDC', 'This is a required field')
                )
                couldAdd = false;
            }

            Ext.Array.each(self.commands, function (item) {
                if (item.category === protocol.category) {
                    protocol.id = item.id;
                    numItem = self.commands.indexOf(item);
                }
            });

            if (!Ext.isEmpty(numItem)) {
                self.commands.splice(numItem, 1);
            }

            if (!Ext.isEmpty(parametersContainer)) {
                switch (parametersContainer.xtype) {
                    case 'communication-tasks-logbookscombo':
                        if (parametersContainer.value.length < 1) {
                            parametersContainer.markInvalid(
                                Uni.I18n.translate('general.required.field', 'MDC', 'This is a required field')
                            );
                            couldAdd = false;
                        }
                        self.fillLogbooks(protocol, parametersContainer);
                        break;
                    case 'communication-tasks-registerscombo':
                        if (parametersContainer.value.length < 1) {
                            parametersContainer.markInvalid(
                                Uni.I18n.translate('general.required.field', 'MDC', 'This is a required field')
                            );
                            couldAdd = false;
                        }
                        self.fillRegisters(protocol, parametersContainer);
                        break;
                    case 'communication-tasks-profilescombo':
                        if (parametersContainer.down('#checkProfileTypes').value.length < 1) {
                            parametersContainer.down('#checkProfileTypes').markInvalid('This is a required field');
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
            btnToRemoveAfterValidation && btnToRemoveAfterValidation.destroy();

            if (self.commands.length === 1) {
                editView.down('#addCommandsToTask').hide();
                editView.down('#addAnotherCommandsButton').show();
            } else if (self.commands.length === categoryContainer.getStore().totalCount) {
                editView.down('#addAnotherCommandsButton').hide();
            }

            self.addTagButton(protocol);
            window.destroy();
        }
    },

    fillLogbooks: function (protocol, parametersContainer) {
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
        protocol.parameters.push(logbooks);
        self.commands.push(protocol);
    },

    fillRegisters: function (protocol, parametersContainer) {
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
        protocol.parameters.push(registers);
        self.commands.push(protocol);
    },

    fillProfiles: function (protocol, parametersContainer) {
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
        protocol.parameters.push(profiles);
        fail.name = "failifconfigurationmismatch";
        if (parametersContainer.down('#radioFail').getValue().fail === 'true') {
            failBoolean = true;
        }
        fail.value = failBoolean;
        protocol.parameters.push(fail);
        intervals.name = "markintervalsasbadtime";
        if (parametersContainer.down('#radioIntervals').getValue().intervals === 'true') {
            intervalsBoolean = true;
        }
        intervals.value = intervalsBoolean;
        protocol.parameters.push(intervals);
        events.name = "createmetereventsfromflags";
        if (parametersContainer.down('#radioEvents').getValue().events === 'true') {
            eventsBoolean = true;
        }
        events.value = eventsBoolean;
        protocol.parameters.push(events);
        var minTime = {},
            minTimeValue = {};
        minTime.name = "minclockdiffbeforebadtime";
        minTimeValue.name = parametersContainer.down('#disContTime').value;
        minTimeValue.value = parseInt(parametersContainer.down('#disContNum').getValue());
        minTime.value = minTimeValue;
        protocol.parameters.push(minTime);
        self.commands.push(protocol);
    },

    fillClockSet: function (protocol, parametersContainer) {
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
        protocol.parameters.push(setMinTime);
        protocol.parameters.push(setMaxTime);
        self.commands.push(protocol);
    },

    fillClockSync: function (protocol, parametersContainer) {
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
        protocol.parameters.push(syncMinTime);
        protocol.parameters.push(syncMaxTime);
        protocol.parameters.push(syncMaxTimeShift);
        self.commands.push(protocol);
    },

    loadCommandToWindow: function (window, command) {
        var me = this,
            actionsStore = Ext.getStore('Mdc.store.CommunicationTasksActions');
        window.down('comtaskCommandCategoryCombo').setValue(command.category);
        window.down('comtaskCommandCategoryCombo').disable();
        actionsStore.load({
            callback: function () {
                window.down('comtaskCommandCategoryActionCombo').setValue(command.action);
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
            itemId: 'tagBtn' + command.category,
            text: Uni.I18n.translate('comtask.action.' + command.category + '.' + command.action, 'MDC', command.category + ' - ' + command.action),
            margin: '5 0 5 0',
            width: 180,
            category: command.category,
            handler: function () {
                var window = Ext.create('Mdc.view.setup.comtasks.ComTaskAddCommandWindow', {
                    title: Uni.I18n.translate('communicationtasks.task.editCommand', 'MDC', 'Edit command'),
                    btnAction: 'edit',
                    btnText: Uni.I18n.translate('general.save', 'MDC', 'Save')
                });
                self.loadCommandToWindow(window, command);
            },
            listeners: {
                closeclick: function (me) {
                    var numItem;
                    Ext.Array.each(self.commands, function (item) {
                        if (item.category === me.category) {
                            numItem = self.commands.indexOf(item);
                        }
                    });
                    if (!Ext.isEmpty(numItem)) {
                        self.commands.splice(numItem, 1);
                    }
                    self.getTaskEdit().down('#addAnotherCommandsButton').show();
                    if (self.commands.length < 1) {
                        self.getTaskEdit().down('#addCommandsToTask').show();
                        self.getTaskEdit().down('#addAnotherCommandsButton').hide();
                    }
                }
            }
        });
    }
});
