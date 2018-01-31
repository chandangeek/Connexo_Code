/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.commands.controller.Commands', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.commands.view.CommandsOverview',
        'Mdc.commands.view.AddCommand'
    ],

    models: [
        'Mdc.model.DeviceCommand',
        'Mdc.commands.model.Command'
    ],

    stores: [
        'Mdc.commands.store.Commands',
        'Mdc.store.DeviceGroupsNoPaging',
        'Mdc.store.DeviceGroups',
        'Mdc.commands.store.CommandCategoriesForDeviceGroup'
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
        },
        {
            ref: 'addPropertyForm',
            selector: 'add-command add-command-wizard add-command-step2 #mdc-add-command-step2-property-form'
        },
        {
            ref: 'addPropertyHeader',
            selector: 'add-command add-command-wizard add-command-step2 #mdc-add-command-step2-property-header'
        }
    ],

    wizardInformation: null,
    categoriesStore: undefined,
    commandsStore: undefined,

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
            },
            'add-command-step2 #mdc-add-command-step2-command-combo': {
                select: this.onCommandSelect
            }
        });
    },

    navigateToCommandsOverview: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/commands').forward();
    },

    navigateToAddCommandWizard: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/commands/add').forward();
    },

    showCommandsOverview: function () {
        var me = this,
            widget = Ext.widget('commands-overview');

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    loadCommandDetail: function (selectionModel, selectedRecords) {
        var me = this,
            simpleRecord = selectedRecords[0],
            preview = me.getCommandPreview(),
            previewForm = me.getCommandPreviewForm(),
            trackingField = previewForm.down('#mdc-command-preview-tracking-field'),
            previewActionsButton = me.getPreviewActionsBtn(),
            previewActionsMenu = preview.down('menu'),
            model = Ext.ModelManager.getModel('Mdc.commands.model.Command');
        if (Ext.isEmpty(simpleRecord)) return;
        previewForm.setLoading(true);
        model.load(simpleRecord.get('id'), {
            success: function (record) {
                if (Ext.isEmpty(me.getCommandPreviewForm())) {
                    // The preview panel is already gone (because eg. the "Add command" wizard is already on screen)
                    return;
                }

                Ext.suspendLayouts();
                if (record.get('trackingCategory').id === 'trackingCategory.serviceCall') {
                    trackingField.setFieldLabel(Uni.I18n.translate('general.serviceCall', 'MDC', 'Service call'));
                    trackingField.renderer = function (val) {
                        if (record.get('trackingCategory').activeLink != undefined && record.get('trackingCategory').activeLink) {
                            return '<a style="text-decoration: underline" href="' +
                                me.getController('Uni.controller.history.Router').getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: val.id})
                                + '">' + val.name + '</a>';
                        } else {
                            return Ext.isEmpty(val.id) ? '-' : Ext.String.htmlEncode(val.id);
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
                previewActionsButton.setVisible((status === 'WAITING' || status === 'PENDING'));
                Ext.resumeLayouts(true);
            },
            callback: function() {
                if (me.getCommandPreviewForm()) {
                    me.getCommandPreviewForm().setLoading(false);
                }
            }
        });
    },


    showAddCommandWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            widget = Ext.widget('add-command', {
                itemId: 'mdc-add-command',
                router: router,
                returnLink: router.getRoute('workspace/commands').buildUrl()
            }),
            deviceGroupStore = Ext.getStore('Mdc.store.DeviceGroupsNoPaging');

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
                me.addCommandToDeviceGroup();
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
                me.validateStep1(function () {
                    doCallback();
                    me.prepareStep2();
                });
                break;
            case 2:
                me.validateStep2(function () {
                    doCallback();
                });
                break;
            case 3:
                if (me.validateStep3()) {
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
            step2Form = step2.getForm();

        me.categoriesStore = me.getStore('Mdc.commands.store.CommandCategoriesForDeviceGroup');
        step2.setLoading();
        step2ErrorMsg.hide();
        step2Form.clearInvalid();
        me.categoriesStore.getProxy().setUrl(me.wizardInformation.deviceGroupId);
        me.categoriesStore.load(function (records, operation, success) {
            me.getStep2CategoryCombo().reset();
            me.getStep2CommandCombo().reset();
            if (records.length === 1) {
                me.getStep2CategoryCombo().setValue(records[0].get('id'));
            }
            step2.setLoading(false);
        });
    },

    validateStep2: function (callback) {
        var me = this,
            wizard = me.getAddCommandWizard(),
            addCommandForm = wizard.down('#mdc-add-command-step2'),
            step2ErrorMsg = me.getStep2FormErrorMessage(),
            step2 = wizard.down('add-command-step2');

        if (!step2.isValid()) {
            step2ErrorMsg.show();
        } else {
            me.wizardInformation.releaseDate = new Date(addCommandForm.getValues().releaseDate).getTime();
            me.validateAddCommandExceedsLimit(callback, addCommandForm.getValues().command, me.wizardInformation.releaseDate);
        }
    },

    prepareStep3: function (wizard) {
        var me = this,
            step3 = wizard.down('add-command-step3').down('#msg-container'),
            confirmationTitle = Uni.I18n.translate('add.command.step3.title.specific', 'MDC',
                "Add '{0}' to devices in '{1}'?", [me.wizardInformation.command.get('name'), me.wizardInformation.deviceGroupName]),
            confirmationMessage = Uni.I18n.translate('add.command.step3.msg', 'MDC', 'The selected command will be added to all devices in the device group.'),
            triggerMessage = Uni.I18n.translate('add.command.step3.msg.trigger', 'MDC', 'Would you like to trigger a communication task to execute this command?');

        step3.update('<h3>' + confirmationTitle + '</h3><br>' + confirmationMessage + '<br><br>' + triggerMessage);
    },

    validateStep3: function () {
        var me = this,
            wizard = me.getAddCommandWizard(),
            addCommandForm = wizard.down('#mdc-add-command-step3'),
            valid = true;
            me.wizardInformation.trigger = addCommandForm.down('radiogroup').getValue().trigger;
        return valid;
    },

    prepareStep4: function (wizard) {
        var me = this,
            step4 = wizard.down('add-command-step4'),
            statusMessage = Uni.I18n.translate('add.command.step4.msg', 'MDC',
                "The command '{0}' will be added to all devices in the device group '{1}'.", [me.wizardInformation.command.get('name'), me.wizardInformation.deviceGroupName]);

        step4.update(statusMessage);
    },

    onCategoryChange: function (combo, categoryId) {
        var me = this,
            step2ErrorMsg = me.getStep2FormErrorMessage(),
            commandCombo = me.getStep2CommandCombo(),
            storeIndex = me.categoriesStore.findExact('id', categoryId),
            category = storeIndex >= 0 ? me.categoriesStore.getAt(storeIndex) : null;

        step2ErrorMsg.hide();
        me.getAddPropertyForm().hide();
        me.getAddPropertyHeader().hide();
        commandCombo.reset();
        if (!Ext.isEmpty(category)) {
            commandCombo.enable();
            commandCombo.bindStore(category.deviceMessageSpecs(), true);
            if (category.deviceMessageSpecs().getCount() === 1) {
                commandCombo.setValue(category.deviceMessageSpecs().getAt(0));
                var records = [];
                records.push(category.deviceMessageSpecs().getAt(0));
                me.onCommandSelect(commandCombo, records);
            }
        } else {
            commandCombo.disable();
        }
    },

    onCommandSelect: function (combo, selectedRecords) {
        var me = this,
            command = selectedRecords[0].copy(),
            propertyHeader = me.getAddPropertyHeader();

        selectedRecords[0].properties().each(function (record) {
            command.properties().add(record)
        });
        if (command) {
            me.getAddPropertyForm().loadRecord(command);
            if (command.properties() && (command.properties().getCount() > 0)) {
                propertyHeader.show();
                propertyHeader.update('<h3>' + Uni.I18n.translate('deviceCommand.overview.attr', 'MDC', 'Attributes of {0}', command.get('name')) + '</h3>');
                me.getAddPropertyForm().show();
            } else {
                me.getAddPropertyForm().hide();
                propertyHeader.hide();
            }
            me.wizardInformation.command = command;
        }
    },

    addCommandToDeviceGroup: function () {
        var me = this,
            wizard = me.getAddCommandWizard(),
            propertyForm = me.getAddPropertyForm();

        wizard.setLoading(true);
        propertyForm.updateRecord();
        var newRecord = propertyForm.getRecord(),
            messageSpecification;
        if (!Ext.isEmpty(newRecord.get('id'))) {
            messageSpecification = {id: newRecord.get('id')}
        }
        newRecord.beginEdit();
        newRecord.set('id', '');
        me.wizardInformation.releaseDate && newRecord.set('releaseDate', me.wizardInformation.releaseDate);
        messageSpecification && newRecord.set('messageSpecification', messageSpecification);
        newRecord.set('status', null);
        newRecord.set('trackingCategory', null);
        newRecord.set('trigger',me.wizardInformation.trigger);
        newRecord.endEdit();
        newRecord.save({
            url: '/api/ddr/devicegroups/' + me.wizardInformation.deviceGroupId + '/commands',
            method: 'POST',
            callback: function (record, operation, success) {
                if (operation.success) {
                    if (wizard.rendered) {
                        wizard.setLoading(false);
                        //wizard.down('#cmbw-step4').setResultMessage(action, success);
                    }
                }
            }
        });
    },

    validateAddCommandExceedsLimit: function (callback, command, releaseDate) {
        var me = this;
        Ext.Ajax.suspendEvent('requestexception');
        Ext.Ajax.request({
            url: '/api/ddr/devicegroups/commands/checkExceeds/' + me.wizardInformation.deviceGroupId,
            method: 'GET',
            params: {
                messageSpecificationId: command,
                releaseDate: releaseDate
            },
            success: function () {
                callback();
            },
            failure: function (response) {
                var message = response.responseText || response.statusText,
                    decoded = Ext.decode(message, true);
                if (decoded && decoded.message){
                    var title = Uni.I18n.translate('general.failedToAddBulkCommandTitle', 'MDC', 'Couldn\'t perform your action');
                    me.getApplication().getController('Uni.controller.Error').showError(title, decoded.message, decoded.errorCode);
                }
            },
            callback: function () {
                Ext.Ajax.resumeEvent('requestexception');
            }
        });
    }
});
