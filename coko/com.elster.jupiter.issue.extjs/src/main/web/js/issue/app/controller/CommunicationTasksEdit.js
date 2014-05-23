Ext.define('Isu.controller.CommunicationTasksEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.CommunicationTasks',
        'Isu.store.CommunicationTasksCategories',
        'Isu.store.CommunicationTasksActions',
        'Isu.store.TimeTypes',
        'Isu.store.CommunicationTasksLogbooks',
        'Isu.store.CommunicationTasksProfiles',
        'Isu.store.CommunicationTasksRegisters'
    ],

    views: [
        'Isu.view.administration.communicationtasks.Edit',
        'Isu.view.administration.communicationtasks.Command',
        'Isu.view.administration.communicationtasks.parameters.Logbooks',
        'Isu.view.administration.communicationtasks.parameters.Profiles',
        'Isu.view.administration.communicationtasks.parameters.Registers'
    ],

    refs: [
        {
            ref: 'taskEdit',
            selector: 'communication-tasks-edit'
        },
        {
            ref: 'commandNames',
            selector: 'communication-tasks-edit [name=commandnames]'
        },
        {
            ref: 'commandFields',
            selector: 'communication-tasks-edit [name=commandfields]'
        }
    ],

    init: function () {
        this.control({
            'communication-tasks-edit breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'communication-tasks-edit communication-tasks-categorycombo': {
                change: this.addActionCombo
            },
            'communication-tasks-edit communication-tasks-actioncombo': {
                change: this.addCommandParameters,
                afterrender: this.disableBtn
            },
            'communication-tasks-edit communication-tasks-command button[action=addCommand]': {
                click: this.addCommandToModel
            },
            'communication-tasks-edit communication-tasks-command button[action=saveCommand]': {
                click: this.addCommandToModel
            },
            'communication-tasks-edit communication-tasks-command button[action=cancelEditCommand]': {
                click: this.cancelEdit
            },
            'communication-tasks-edit communication-tasks-command button[action=removeCommand]': {
                click: this.removeEdit
            },
            'communication-tasks-edit #createEditTask': {
                click: this.createEdit
            },
            'communication-tasks-profilescombo': {
                afterrender: this.setTooltip
            }
        });
        this.commands = [];
    },

    disableBtn: function () {
        var btn = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-command button[action=addCommand]')[0];
        btn.setDisabled(true);
    },

    removeEdit: function () {
        var categoryCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-categorycombo')[0],
            actionBtn = Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0],
            btns = Ext.ComponentQuery.query('communication-tasks-edit tag-button');
        Ext.Array.each(btns, function (btn) {
            if (btn.category === categoryCombo.value) {
                btn.fireEvent('closeclick', btn);
                btn.destroy();
            }
        });
        actionBtn.setDisabled(false);
    },

    cancelEdit: function () {
        var commandView = Ext.ComponentQuery.query('communication-tasks-command')[0],
            actionBtn = Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0];
        commandView.destroy();
        actionBtn.setDisabled(false);
    },

    createEdit: function (btn) {
        var self = this,
            header = {
                style: 'msgHeaderStyle'
            },
            msges = [],
            sendingData = {},
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            preloader,
            nameField = editView.down('form').down('textfield[name=name]');
        self.trimFields();
        sendingData.name = nameField.getValue();
        sendingData.commands = self.commands;
        if (btn.text === 'Create') {
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Creating communication task",
                target: editView
            });
            preloader.show();
            self.createTask(preloader, sendingData, header, msges);
        } else {
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Editing communication task",
                target: editView
            });
            preloader.show();
            self.editTask(preloader, sendingData, header, msges);
        }
    },

    createTask: function(preloader, sendingData, header, msges) {
        var self = this;
        Ext.Ajax.request({
            url: '/api/cts/comtasks',
            method: 'POST',
            jsonData: sendingData,
            success: function () {
                window.location.href = '#/issue-administration/communicationtasks';
                header.text = 'Successfully created';
                self.getApplication().fireEvent('isushowmsg', {
                    type: 'notify',
                    msgBody: [header],
                    y: 10,
                    showTime: 5000
                });
                self.commands = [];
            },
            failure: function (response) {
                header.text = 'Error during creation.';
                msges.push(header);
                var result = Ext.decode(response.responseText, true);
                if (result !== null) {
                    self.showNameNotUniqueError(result, msges);
                } else {
                    var bodyItem = {};
                    bodyItem.style = 'msgItemStyle';
                    bodyItem.text = 'The communication task could not be created because of an error in the database.';
                    msges.push(bodyItem);
                    self.showDatabaseError(msges);
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    editTask: function(preloader, sendingData, header, msges) {
        var self = this;
        Ext.Ajax.request({
            url: '/api/cts/comtasks/' + self.taskEditId,
            method: 'PUT',
            jsonData: sendingData,
            success: function () {
                window.location.href = '#/issue-administration/communicationtasks';
                header.text = 'Successfully edited';
                self.getApplication().fireEvent('isushowmsg', {
                    type: 'notify',
                    msgBody: [header],
                    y: 10,
                    showTime: 5000
                });
                self.commands = [];
            },
            failure: function (response) {
                var bodyItem = {};
                header.text = 'Error during edit.';
                msges.push(header);
                bodyItem.style = 'msgItemStyle';
                bodyItem.text = 'The communication task could not be edited because of an error in the database.';
                msges.push(bodyItem);
                self.showDatabaseError(msges);
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    showNameNotUniqueError: function (result, msges) {
        var self = this,
            editView = self.getTaskEdit(),
            nameField = editView.down('form').down('[name=name]'),
            bodyItem = {};
        bodyItem.style = 'msgItemStyle';
        bodyItem.text = 'The name is not unique';
        nameField.markInvalid(bodyItem.text);
        msges.push(bodyItem);
    },

    trimFields: function () {
        var nameField = this.getTaskEdit().down('form').down('[name=name]'),
            nameValue = Ext.util.Format.trim(nameField.value);
        nameField.setValue(nameValue);
    },

    showDatabaseError: function (msges) {
        var self = this,
            editView = self.getTaskEdit();
        self.getApplication().fireEvent('isushowmsg', {
            type: 'error',
            msgBody: msges,
            y: 10,
            closeBtn: true,
            btns: [
                {
                    text: 'Cancel',
                    cls: 'isu-btn-link',
                    hnd: function () {
                        window.location = '#/issue-administration/communicationtasks';
                    }
                }
            ],
            listeners: {
                close: {
                    fn: function () {
                        editView.enable();
                    }
                }
            }
        });
        editView.disable();
    },

    setTooltip: function () {
        var iconIntervals = Ext.ComponentQuery.query('#radioIntervals')[0].getEl().down('img'),
            textIntervals = 'If the clock difference between the clock in the meter and the clock of the communication server is equal to or bigger than the minimum clock difference, the intervals will be marked as bad time',
            iconEvents = Ext.ComponentQuery.query('#radioEvents')[0].getEl().down('img'),
            textEvents = 'When data with a status flag comes in, meter events will be created',
            iconFail = Ext.ComponentQuery.query('#radioFail')[0].getEl().down('img'),
            textFail = "A profile configuration defines how a load profile of that configuration looks like. When the profile configuration doesn't match the load profile, a failure occurs.";
        iconIntervals.tooltip = Ext.create('Ext.tip.ToolTip', {
            target: iconIntervals,
            html: textIntervals
        });
        iconEvents.tooltip = Ext.create('Ext.tip.ToolTip', {
            target: iconEvents,
            html: textEvents
        });
        iconFail.tooltip = Ext.create('Ext.tip.ToolTip', {
            target: iconFail,
            html: textFail
        });
    },

    showOverview: function (id) {
        var self = this,
            widget = Ext.widget('communication-tasks-edit');
        if (id) {
            self.taskEditId = id;
            this.operationType = 'Edit';
            this.getApplication().fireEvent('changecontentevent', widget);
            this.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' communication task');
            this.getTaskEdit().down('toolbar').getComponent('createEditTask').setText('Edit');
            self.commands = [];
            this.loadModelToEditForm(id);
        } else {
            this.operationType = 'Create';
            this.getApplication().fireEvent('changecontentevent', widget);
            this.getTaskEdit().getCenterContainer().down().setTitle(this.operationType + ' communication task');
            this.getTaskEdit().down('toolbar').getComponent('createEditTask').setText('Create');
            this.loadModelToCreateForm();
            self.commands = [];
        }
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this,
            breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Communication tasks',
                href: 'communicationtasks'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: me.operationType + ' communication task'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    loadModelToCreateForm: function () {
        var self = this,
            categoriesStore = this.getStore('Isu.store.CommunicationTasksCategories');
        categoriesStore.load({
            scope: this,
            callback: function () {
                self.addNewCommand();
            }
        });
    },

    loadModelToEditForm: function (id) {
        var self = this,
            nameField = self.getTaskEdit().down('textfield'),
            categoriesStore = self.getStore('Isu.store.CommunicationTasksCategories');
        categoriesStore.load({
            scope: this,
            callback: function () {
                Ext.Ajax.request({
                    url: '/api/cts/comtasks/' + id,
                    success: function (response) {
                        var rec = Ext.decode(response.responseText);
                        nameField.setValue(rec.name);
                        Ext.Array.each(rec.commands, function (command) {
                            self.addTagButton(command);
                        });
                        self.commands = rec.commands;
                        self.addAnotherButton();
                    }
                });
            }
        });
    },

    showCommandsAndActions: function (command) {
        var self = this,
            commandFields = self.getCommandFields(),
            commandView = Ext.ComponentQuery.query('communication-tasks-command')[0],
            commandContainer,
            categoryCombo,
            actionCombo;
        if (commandView) {
            commandView.destroy();
        }
        commandContainer = commandFields.add({
            xtype: 'communication-tasks-command'
        });
        commandContainer.add({
            xtype: 'communication-tasks-actioncombo'
        });
        categoryCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-categorycombo')[0];
        actionCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-actioncombo')[0];
        categoryCombo.setValue(command.category);
        actionCombo.setValue(command.action);
        self.setValuesToForm(command, commandContainer);
        categoryCombo.setDisabled(true);
        actionCombo.setDisabled(true);
        commandContainer.down('button[action=saveCommand]').show();
        commandContainer.down('button[action=removeCommand]').show();
        commandContainer.down('button[action=cancelEditCommand]').show();
    },

    setValuesToForm: function (command, commandContainer) {
        var parametersContainer = commandContainer.down('communication-tasks-actioncombo').nextNode();
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
                if (!parametersContainer.down('#disCont').isDisabled()) {
                    parametersContainer.down('#disContTime').setValue(command.parameters[4].value.name);
                    parametersContainer.down('#disContNum').setValue(command.parameters[4].value.value);
                }
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

    addNewCommand: function () {
        var commandNames = this.getCommandNames(),
            commandFields = this.getCommandFields(),
            newCommand = Ext.ComponentQuery.query('#newCommand')[0],
            commandContainer;
        if (newCommand && newCommand.isHidden()) {
            newCommand.show();
        } else {
            commandNames.add({
                html: 'New command',
                style: 'margin: 0 0 0 100px; padding: 8px 0 0 0;',
                itemId: 'newCommand',
                width: 151
            });
        }
        commandContainer = commandFields.add({
            xtype: 'communication-tasks-command'
        });
        commandContainer.down('button[action=addCommand]').show();
    },

    addActionCombo: function (combo, newValue) {
        var commandContainer = combo.up('communication-tasks-command'),
            actionsStore = Ext.getStore('Isu.store.CommunicationTasksActions'),
            actionCombo = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-actioncombo')[0];
        if (combo.lastActiveError !== '') {
            combo.clearInvalid();
        }
        if (!actionCombo) {
            actionsStore.getProxy().setExtraParam('category', newValue);
            actionsStore.load(function () {
                actionCombo = commandContainer.add({
                    xtype: 'communication-tasks-actioncombo'
                });
                combo.on('change', function () {
                    actionCombo.destroy();
                }, combo, {single: true});
            });
        }
    },

    addCommandParameters: function (combo, newValue) {
        var commandContainer = combo.up('communication-tasks-command'),
            category = commandContainer.down('communication-tasks-categorycombo').getValue(),
            parametersContainer = this.chooseCommandParameters(category, newValue);
        if (parametersContainer) {
            commandContainer.add(parametersContainer);
            combo.on('change', function () {
                parametersContainer.destroy();
            }, combo, {single: true});
            combo.on('destroy', function () {
                parametersContainer.destroy();
            }, combo, {single: true});
        }
        if (!commandContainer.down('button[action=addCommand]').isHidden()) {
            commandContainer.down('button[action=addCommand]').setDisabled(false);
        }
    },

    chooseCommandParameters: function (category, action) {
        var xtype,
            self = this,
            logbooksStore = self.getStore('Isu.store.CommunicationTasksLogbooks'),
            profilesStore = self.getStore('Isu.store.CommunicationTasksProfiles'),
            registersStore = self.getStore('Isu.store.CommunicationTasksRegisters');
        switch (category) {
            case 'logbooks':
                logbooksStore.load();
                xtype = 'communication-tasks-logbookscombo';
                break;
            case 'registers':
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
            commandContainer = button.up('communication-tasks-command'),
            commandFields = self.getCommandFields(),
            editView = self.getTaskEdit(),
            form = editView.down('form').getForm(),
            category = commandContainer.down('communication-tasks-categorycombo').value,
            action = commandContainer.down('communication-tasks-actioncombo').value,
            newCommand = Ext.ComponentQuery.query('#newCommand')[0],
            btns = Ext.ComponentQuery.query('communication-tasks-edit tag-button'),
            parametersContainer = commandContainer.down('communication-tasks-actioncombo').nextNode(),
            btnAction = button.action,
            numItem,
            protocol = {};
        Ext.Array.each(btns, function (btn) {
            if (button.action === 'addCommand' && btn.category === category) {
                commandContainer.down('communication-tasks-categorycombo').markInvalid('The communication task already has this category');
                button.disable();
            }
        });
        if (commandContainer.down('communication-tasks-categorycombo').lastActiveError === '') {
            protocol.category = category;
            protocol.action = action;
            protocol.parameters = [];
            Ext.Array.each(self.commands, function (item) {
                if (item.category === protocol.category) {
                    protocol.id = item.id;
                    numItem = self.commands.indexOf(item);
                }
            });
            self.commands.splice(numItem, 1);
            switch (parametersContainer.xtype) {
                case 'communication-tasks-logbookscombo':
                    self.fillLogbooks(protocol, parametersContainer);
                    break;
                case 'communication-tasks-registerscombo':
                    self.fillRegisters(protocol, parametersContainer);
                    break;
                case 'communication-tasks-profilescombo':
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
            if (btnAction === 'addCommand') {
                newCommand.destroy();
                self.addTagButton(protocol);
                self.addAnotherButton();
            } else {
                Ext.ComponentQuery.query('communication-tasks-edit #createEditTask')[0].setDisabled(false);
                self.updateTagButton(protocol);
            }
            commandFields.removeAll();
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
        fail.name = "failifconfigurationmismatch";
        if (parametersContainer.down('#radioFail').getValue().fail === 'true') {
            failBoolean = true;
        }
        fail.value = failBoolean;
        protocol.parameters.push(fail);
        if (!parametersContainer.down('#disCont').isDisabled()) {
            var minTime = {},
                minTimeValue = {};
            minTime.name = "minclockdiffbeforebadtime";
            minTimeValue.name = parametersContainer.down('#disContTime').value;
            minTimeValue.value = parseInt(parametersContainer.down('#disContNum').getValue());
            minTime.value = minTimeValue;
            protocol.parameters.push(minTime);
        }
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

    updateTagButton: function (command) {
        var self = this;
        self.getCommandNames().down('#tagBtn' + command.category).handler = function () {
            var newCommand = Ext.ComponentQuery.query('#newCommand')[0];
            if (newCommand) {
                newCommand.hide();
                self.addAnotherButton();
            }
            self.showCommandsAndActions(command);
        };
    },

    addTagButton: function (command) {
        var self = this;
        self.getCommandNames().add({
            xtype: 'tag-button',
            itemId: 'tagBtn' + command.category,
            text: command.action + ' ' + command.category,
            margin: '5 0 5 100',
            category: command.category,
            handler: function () {
                var newCommand = Ext.ComponentQuery.query('#newCommand')[0];
                if (newCommand) {
                    newCommand.hide();
                    self.addAnotherButton();
                }
                self.showCommandsAndActions(command);
            },
            listeners: {
                closeclick: function (me) {
                    var commandView = Ext.ComponentQuery.query('communication-tasks-command')[0],
                        numItem;
                    if (commandView) {
                        if (me.category === commandView.down('communication-tasks-categorycombo').value) {
                            commandView.destroy();
                        }
                    }
                    Ext.Array.each(self.commands, function (item) {
                        if (item.category === me.category) {
                            numItem = self.commands.indexOf(item);
                        }
                    });
                    self.commands.splice(numItem, 1);
                }
            }
        });
    },

    addAnotherButton: function () {
        var self = this;
        self.getCommandNames().add({
            xtype: 'button',
            itemId: 'addAnotherButton',
            text: '+ Add another',
            margin: '5 0 5 100',
            handler: function () {
                this.destroy();
                var categoriesStore = self.getStore('Isu.store.CommunicationTasksCategories'),
                    commandView = Ext.ComponentQuery.query('communication-tasks-command')[0];
                if (commandView) {
                    commandView.destroy();
                }
                categoriesStore.load({
                    scope: this,
                    callback: function () {
                        self.addNewCommand();
                    }
                });
            }
        });
    }
});