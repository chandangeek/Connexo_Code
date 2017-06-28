/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.commands.controller.Commands', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.commands.view.CommandsOverview',
        'Mdc.commands.view.AddCommand'
    ],

    requires: [
    ],

    stores: [
        'Mdc.commands.store.Commands',
        'Mdc.store.DeviceGroups',
        'Mdc.commands.store.CommandsForDeviceGroup'
    ],

    refs: [
        {
            ref: 'commandPreview',
            selector: 'command-preview'
        },
        {
            ref: 'commandPreviewForm',
            selector: 'command-preview command-preview-form'
        },
        {
            ref: 'previewActionsBtn',
            selector: '#mdc-command-preview-actions-button'
        },
        {
            ref: 'commandsGrid',
            selector: 'commands-grid'
        },
        {
            ref: 'addCommandWizard',
            selector: 'add-command add-command-wizard'
        },
        {
            ref: 'navigationMenu',
            selector: 'add-command add-command-side-navigation'
        },
        {
            ref: 'step1FormErrorMessage',
            selector: 'add-command add-command-wizard add-command-step1 #mdc-add-command-step1-error'
        },
        {
            ref: 'step1DeviceGroupCombo',
            selector: 'add-command add-command-wizard add-command-step1 #mdc-add-command-step1-deviceGroup-combo'
        },
        {
            ref: 'step2FormErrorMessage',
            selector: 'add-command add-command-wizard add-command-step2 #mdc-add-command-step2-error'
        },
        {
            ref: 'step2CategoryCombo',
            selector: 'add-command add-command-wizard add-command-step2 #mdc-add-command-step2-category-combo'
        },
        {
            ref: 'step2CommandCombo',
            selector: 'add-command add-command-wizard add-command-step2 #mdc-add-command-step2-command-combo'
        }
    ],

    wizardInformation: null,
    categoriesStore: undefined,
    commandsStore: undefined,
    commandsPerCategory: undefined,

    init: function () {
        this.control({
            '#mdc-add-command-cancel': {
                click: this.navigateToCommandsOverview
            },
            '#mdc-commands-grid': {
                selectionchange: this.loadCommandDetail
            },
            '#mdc-commands-grid-add-command-btn': {
                click: this.navigateToAddCommandWizard
            },
            '#mdc-empty-commands-grid-add-button': {
                click: this.navigateToAddCommandWizard
            },
            'add-command-wizard button[navigationBtn=true]': {
                click: this.moveTo
            },
            'add-command-side-navigation': {
                movetostep: this.moveTo
            },
            'add-command-step2 #mdc-add-command-step2-category-combo': {
                change: this.onCategoryChange
            }
        });
    },

    navigateToCommandsOverview: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/commands').forward();
    },

    navigateToAddCommandWizard: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/commands/add').forward();
    },

    showCommandsOverview: function() {
        var me = this,
            widget = Ext.widget('commands-overview');

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    loadCommandDetail: function (selectionModel, selectedRecords) {
        var me = this,
            record = selectedRecords[0],
            preview = me.getCommandPreview(),
            previewForm = me.getCommandPreviewForm(),
            trackingField = previewForm.down('#mdc-command-preview-tracking-field'),
            previewActionsButton = me.getPreviewActionsBtn(),
            previewActionsMenu = preview.down('menu');

        if (Ext.isEmpty(record)) return;
        Ext.suspendLayouts();
        if (record.get('trackingCategory').id === 'trackingCategory.serviceCall') {
            trackingField.setFieldLabel(Uni.I18n.translate('general.serviceCall', 'MDC', 'Service call'));
            trackingField.renderer = function(val) {
                if (record.get('trackingCategory').activeLink != undefined && record.get('trackingCategory').activeLink) {
                    return '<a style="text-decoration: underline" href="' +
                        me.getController('Uni.controller.history.Router').getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: val.id})
                        + '">' + val.name + '</a>';
                } else {
                    return Ext.isEmpty(val.id) ? '-'  : Ext.String.htmlEncode(val.id);
                }
            }
        } else {
            trackingField.setFieldLabel(Uni.I18n.translate('general.trackingSource', 'MDC', 'Tracking source'));
            trackingField.renderer = function (val) {
                return !Ext.isEmpty(val) && !Ext.isEmpty(val.name) ? Ext.String.htmlEncode(val.name) : '-';
            }
        }
        previewForm.loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('messageSpecification').name));
        if (previewActionsMenu) {
            previewActionsMenu.record = record;
        }

        var status = record.get('status').value;
        previewActionsButton.setVisible( (status === 'WAITING' || status === 'PENDING') );
        Ext.resumeLayouts(true);
    },


    showAddCommandWizard: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            widget = Ext.widget('add-command', {
                itemId: 'mdc-add-command',
                router: router,
                returnLink: router.getRoute('workspace/commands').buildUrl()
            }),
            deviceGroupStore = Ext.getStore('Mdc.store.DeviceGroups');

        mainView.setLoading();
        me.wizardInformation = {};
        deviceGroupStore.load(function () {
            var numberOfDdeviceGroups = this.getCount();
            if (numberOfDdeviceGroups === 0) {
                var deviceGroupCombo = widget.down('#mdc-add-command-step1-deviceGroup-combo');
                deviceGroupCombo.hide();
                deviceGroupCombo.setDisabled(true);
                widget.down('#mdc-add-command-step1-noDeviceGroup-msg').show();
            } else if (numberOfDdeviceGroups === 1) {
                var deviceGroupCombo = widget.down('#mdc-add-command-step1-deviceGroup-combo');
                deviceGroupCombo.setValue(this.getAt(0));
            }
            mainView.setLoading(false);
            me.getApplication().fireEvent('changecontentevent', widget);
        });
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getAddCommandWizard().getLayout(),
            currentStep = wizardLayout.getActiveItem().navigationIndex,
            direction,
            nextStep,
            changeStep = function () {
                Ext.suspendLayouts();
                me.prepareNextStep(nextStep);
                wizardLayout.setActiveItem(nextStep - 1);
                me.getNavigationMenu().moveToStep(nextStep);
                Ext.resumeLayouts(true);
            };

        if (button.action === 'step-next' || button.action === 'confirm-action') {
            direction = 1;
            nextStep = currentStep + direction;
        } else {
            direction = -1;
            if (button.action === 'step-back') {
                nextStep = currentStep + direction;
            } else {
                nextStep = button;
            }
        }

        if (direction > 0) {
            me.validateCurrentStep(currentStep, changeStep);
        } else {
            changeStep();
        }
    },

    prepareNextStep: function (stepNumber) {
        var me = this,
            wizard = me.getAddCommandWizard(),
            buttons = wizard.getDockedComponent('mdc-add-command-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm-action]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]');

        switch (stepNumber) {
            case 1:
                nextBtn.show();
                backBtn.show();
                backBtn.disable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 2:
                me.getStep2FormErrorMessage().hide();
                nextBtn.show();
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 3:
                nextBtn.hide();
                backBtn.show();
                confirmBtn.show();
                finishBtn.hide();
                cancelBtn.show();
                me.prepareStep3(wizard);
                break;
            case 4:
                nextBtn.hide();
                backBtn.hide();
                confirmBtn.hide();
                finishBtn.show();
                cancelBtn.hide();
                me.prepareStep4(wizard);
                break;
        }
    },

    validateCurrentStep: function (stepNumber, callback) {
        var me = this,
            doCallback = function () {
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        switch (stepNumber) {
            case 1:
                me.validateStep1(function() {
                    doCallback();
                    me.prepareStep2();
                });
                break;
            case 2:
                if (me.validateStep2()) {
                    doCallback();
                }
                break;
            default:
                doCallback();
        }
    },

    validateStep1: function (callback) {
        var me = this,
            wizard = me.getAddCommandWizard(),
            step1 = wizard.down('add-command-step1'),
            noDeviceGroupsMsg = step1.down('#mdc-add-command-step1-noDeviceGroup-msg'),
            step1ErrorMsg = me.getStep1FormErrorMessage(),
            deviceGroupCombo = me.getStep1DeviceGroupCombo();

        step1ErrorMsg.hide();
        if (noDeviceGroupsMsg.isVisible()) { // because of no device groups defined yet
            step1ErrorMsg.show();
            noDeviceGroupsMsg.markInvalid(Uni.I18n.translate('general.required.field', 'MDC', 'This field is required'));
        } else if (!deviceGroupCombo.validate()) {
            step1ErrorMsg.show();
        } else {
            me.wizardInformation.deviceGroupId = deviceGroupCombo.getValue();
            me.wizardInformation.deviceGroupName = deviceGroupCombo.getRawValue();
            callback();
        }
    },

    prepareStep2: function () {
        var me = this,
            wizard = me.getAddCommandWizard(),
            step2ErrorMsg = me.getStep2FormErrorMessage(),
            step2 = wizard.down('add-command-step2'),
            step2Form = step2.getForm(),
            commandsForDeviceGroupStore = me.getStore('Mdc.commands.store.CommandsForDeviceGroup'),
            categorySet = new Set(),
            categoryModels = [],
            category = undefined;

        me.commandsPerCategory = {};
        step2.setLoading();
        step2ErrorMsg.hide();
        step2Form.clearInvalid();
        if (Ext.isEmpty(me.categoriesStore)) {
            me.categoriesStore = Ext.create('Ext.data.ArrayStore', {
                fields: ['name']
            });
        } else {
            me.categoriesStore.removeAll(false);
        }
        commandsForDeviceGroupStore.getProxy().setUrl(me.wizardInformation.deviceGroupId);
        commandsForDeviceGroupStore.load(function(records, operation, success) {
            Ext.Array.forEach(records, function(record, index){
                category = record.get('category');
                if (!categorySet.has(category)) {
                    categorySet.add(category);
                    categoryModels.push({name: category});
                    me.commandsPerCategory[category] = [];
                    me.commandsPerCategory[category].push(record);
                } else {
                    me.commandsPerCategory[category].push(record);
                }
            }, me);
            categoryModels.sort(function (model1, model2) {
                return model1.name.localeCompare(model2.name); // Sort the categories alphabetically
            });
            if (categoryModels.length != 0) {
                me.categoriesStore.add(categoryModels);
                me.getStep2CategoryCombo().reset();
                me.getStep2CategoryCombo().bindStore(me.categoriesStore, true);
            } else {
                me.getStep2CategoryCombo().reset();
                me.getStep2CommandCombo().reset();
            }
            step2.setLoading(false);
        });
    },

    validateStep2: function() {
        var me = this,
            wizard = me.getAddCommandWizard(),
            step2ErrorMsg = me.getStep2FormErrorMessage(),
            step2 = wizard.down('add-command-step2'),
            commandCombo = me.getStep2CommandCombo(),
            valid = true;

        if (!step2.isValid()) {
            valid = false;
            step2ErrorMsg.show();
        } else {
            me.wizardInformation.command = commandCombo.getRawValue();
            me.wizardInformation.commandName = commandCombo.getValue();
        }
        return valid;
    },

    prepareStep3: function (wizard) {
        var me = this,
            step3 = wizard.down('add-command-step3'),
            confirmationTitle = Uni.I18n.translate('add.command.step3.title', 'MDC',
                "Add '{0}' to devices in '{1}'?", [me.wizardInformation.command, me.wizardInformation.deviceGroupName]),
            confirmationMessage = Uni.I18n.translate('add.command.step3.msg', 'MDC', 'The selected command will be added to all devices in the device group.');

        step3.update('<h3>' + confirmationTitle + '</h3><br>' + confirmationMessage);
    },

    prepareStep4: function (wizard) {
        var me = this,
            step4 = wizard.down('add-command-step4'),
            statusMessage = Uni.I18n.translate('add.command.step4.msg', 'MDC',
                "The command '{0}' will be added to all devices in the device group '{1}'.", [me.wizardInformation.command, me.wizardInformation.deviceGroupName]);

        step4.update(statusMessage);
    },

    onCategoryChange: function(combo, newValue, oldValue) {
        var me = this,
            commandCombo = me.getStep2CommandCombo(),
            commandsStore = Ext.create('Ext.data.ArrayStore', {
                fields: ['category', 'command', 'commandName']
            });

        if (Ext.isEmpty(me.commandsPerCategory) || Ext.isEmpty(me.commandsPerCategory[newValue])) {
            commandCombo.setDisabled(true);
        } else {
            Ext.suspendLayouts();
            var commandsArray = me.commandsPerCategory[newValue];
            commandsStore.add(commandsArray);
            if (commandsArray.length === 1) {
                commandCombo.setValue(me.commandsPerCategory[newValue][0].get('command'));
            } else {
                commandCombo.reset();
            }
            commandCombo.bindStore(commandsStore, true);
            commandCombo.enable();
            Ext.resumeLayouts(true);
        }
    }

});
