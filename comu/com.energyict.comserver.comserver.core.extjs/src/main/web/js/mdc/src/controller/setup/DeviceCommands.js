Ext.define("Mdc.controller.setup.DeviceCommands", {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.view.window.Acknowledgement',
        'Uni.view.window.Confirmation'
    ],

    views: [
        'Mdc.view.setup.devicecommand.DeviceCommandsSetup',
        'Mdc.view.setup.devicecommand.DeviceCommandPreview',
        'Mdc.view.setup.devicecommand.DeviceCommandPreviewForm',
        'Mdc.view.setup.devicecommand.DeviceCommandAdd',
        'Mdc.view.setup.devicecommand.widget.ChangeReleaseDateWindow'
    ],

    stores: [
        'Mdc.store.DeviceCommands',
        'Mdc.store.DeviceMessageCategories'
    ],

    models: [
        'Mdc.model.DeviceMessageSpec',
        'Mdc.model.DeviceCommand',
        'Mdc.model.DeviceMessageCategory'
    ],

    refs: [
        {
            ref: 'commandCombo',
            selector: '#device-command-add-form combobox[name=command]'
        },
        {
            ref: 'previewPanel',
            selector: '#deviceCommandPreview'
        },
        {
            ref: 'previewCommandForm',
            selector: '#deviceCommandPreview deviceCommandPreviewForm'
        },
        {
            ref: 'previewPropertiesPanel',
            selector: '#deviceCommandPreview #previewPropertiesPanel'
        },
        {
            ref: 'previewPropertiesHeader',
            selector: '#deviceCommandPreview #previewPropertiesHeader'
        },
        {
            ref: 'previewPropertiesForm',
            selector: '#deviceCommandPreview #previewPropertiesPanel property-form'
        },
        {
            ref: 'addPropertyForm',
            selector: '#device-command-add-panel #device-command-add-property-form'
        },
        {
            ref: 'addPropertyHeader',
            selector: '#device-command-add-property-header'
        },
        {
            ref: 'addCommandForm',
            selector: '#device-command-add-form'
        },
        {
            ref: 'addCommandPanel',
            selector: '#device-command-add-panel'
        },
        {
            ref: 'previewActionBtn',
            selector: '#commandsPreviewActionButton'
        },
        {
            ref: 'actionMenu',
            selector: '#deviceCommandPreview #device-command-action-menu'
        },
        {
            ref: 'deviceCommandsGrid',
            selector: '#deviceCommandsGrid'
        }
    ],

    init: function () {
        this.control({
            '#deviceCommandsGrid': {
                selectionchange: this.selectCommand
            },
            'device-command-action-menu': {
                beforeshow: this.configureMenu
            },
            '#device-command-add-form combobox[name=commandCategory]': {
                select: this.msgCategoryChange
            },
            '#device-command-add-form combobox[name=command]': {
                select: this.commandChange
            },
            '#device-command-add-panel button[action=add]': {
                click: this.addCommand
            },
            '#device-command-add-panel button[action=cancel]': {
                click: this.cancelClick
            },
            '#empty_grid_deviceAddCommandButton': {
                click: this.navigateAdd
            },
            '#deviceAddCommandButton': {
                click: this.navigateAdd
            },
            '#device-command-action-menu': {
                click: this.selectAction
            }
        })
    },

    selectAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'trigger':
                me.actionTriggerCommand(menu);
                break;
            case 'changeReleaseDate':
                me.changeReleaseDate(menu.record, menu.device);
                break;
            case 'revoke':
                me.revokeCommand(menu.record, menu.device);
                break;
        }
    },

    actionTriggerCommand: function (menu) {
        var me = this,
            record = menu.record,
            comTaskId = record.get('preferredComTask').id;

        me.showTriggerConfirmation(menu.deviceId, comTaskId, menu.device);
    },

    showTriggerConfirmation: function (deviceId, comTaskId, device) {
        var me = this;
        Ext.widget('confirmation-window', {
            confirmText: Uni.I18n.translate('deviceCommand.overview.trigger', 'MDC', 'Trigger'),
            confirmBtnUi: 'action'
        }).show({
                closable: false,
                icon: '',
                fn: function (btnId) {
                    if (btnId == 'confirm') {
                        var store = me.getStore('Mdc.store.DeviceCommands');
                        me.triggerCommand(deviceId, comTaskId, device);
                    }
                },
                msg: Uni.I18n.translate('deviceCommand.overview.triggerMsg', 'MDC', 'Would you like to trigger a communication task to execute this command?'),
                title: Uni.I18n.translate('deviceCommand.action.triggerComTask', 'MDC', 'Trigger communication task')
            });
    },

    triggerCommand: function (deviceId, comTaskId, device) {
        var me = this,
            infoData = {'device': {
                'version': device.data.version,
                'name': device.data.name,
                'parent' : {
                    'id': device.data.parent.id,
                    'version': device.data.parent.version
                }
            }};
        var info = Ext.encode(infoData);
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(deviceId) + '/comtasks/' + comTaskId + '/runnow',
            jsonData: info,
            method: 'PUT',
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommand.overview.triggerSuccess', 'MDC', 'Command triggered'));
                me.getDeviceCommandsGrid().getStore().load()
            }
        })
    },

    revokeCommand: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceId = router.arguments.name,
            title = Uni.I18n.translate('deviceCommand.overview.revokex', 'MDC', "Revoke '{0}'?",[record.get('command').name]);
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('deviceCommand.overview.revoke', 'MDC', 'Revoke')
        }).show({
                msg: Uni.I18n.translate('deviceCommand.overview.revokeMsg', 'MDC', 'This command will no longer be able to send'),
                title: title,
                fn: function (btnId) {
                    if (btnId == 'confirm') {
                        record.set('status', {value: 'REVOKED'});
                        record.save({
                            isNotEdit: true,
                            url: '/api/ddr/devices/' + deviceId + '/devicemessages/',
                            success: function () {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommand.overview.revokeSuccess', 'MDC', 'Command revoked'));
                                router.getRoute().forward();
                            },
                            failure: function (record, operation) {
                                record.reject();
                                if (operation.response.status === 409) {
                                    return
                                }
                                var title = Uni.I18n.translate('devicemessages.revoke.failurex', 'MDC', "Failed to revoke '{0}'", [record.get('command').name]),
                                    json = Ext.decode(operation.response.responseText),
                                    message = '';

                                if (json && json.errors) {
                                    message = json.errors[0].msg;
                                }
                                me.getApplication().getController('Uni.controller.Error').showError(title, message);
                            }
                        });
                    }
                }
            });
    },

    changeReleaseDate: function (record, device) {
        var me = this,
            title = Uni.I18n.translate('deviceCommand.overview.changeReleaseDateHeader', 'MDC', "Change release date of command '{0}'",[record.get('command').name]),
            router = me.getController('Uni.controller.history.Router'),
            responseText,
            store = me.getStore('Mdc.store.DeviceCommands');

        store.getProxy().setExtraParam('deviceId', device.get('name'));
        Ext.widget('device-command-change-release-date', {
            title: title,
            record: record,
            listeners: {
                save: {
                    fn: function (newDate, record, oldDate) {
                        record.setProxy(store.getProxy());
                        record.set('releaseDate', newDate);
                        record.save({
                            isNotEdit: true,
                            success: function () {
                                router.getRoute().forward();
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommand.changeReleaseDate.success', 'MDC', 'Release date changed'));
                            },
                            failure: function (record, operation) {
                                responseText = Ext.decode(operation.response.responseText, true);
                                me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('deviceCommand.changeReleaseDateFailed', 'MDC', "'Change release date' failed"), responseText.errors[0].msg);
                                record.reject();
                            }
                        });
                    }
                }
            }
        }).show();
    },

    showOverview: function (deviceId) {
        var me = this,
            store = me.getStore('Mdc.store.DeviceCommands');


        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {

                me.getStore('Mdc.store.DeviceCommands').getProxy().setExtraParam('deviceId', device.get('name'));
                var widget = Ext.widget('deviceCommandsSetup', {
                    device: device
                });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    showAddOverview: function (deviceId) {
        var me = this,
            catStore = me.getStore('Mdc.store.DeviceMessageCategories');

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getStore('Mdc.store.DeviceCommands').getProxy().setExtraParam('deviceId', device.get('name'));
                widget = Ext.widget('device-command-add', {
                    device: device
                });
                if (deviceId) {
                    catStore.setName(deviceId);
                    catStore.load();
                }
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    navigateAdd: function (btn) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/commands/add').forward({deviceId: encodeURIComponent(btn.deviceId)});
    },

    cancelClick: function (btn) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/commands').forward({deviceId: encodeURIComponent(btn.deviceId)});
    },

    selectCommand: function (grid, selected) {
        var me = this,
            record = selected[0],
            previewForm = me.getPreviewCommandForm(),
            previewPanel = me.getPreviewPanel(),
            actionsButton = me.getPreviewActionBtn(),
            previewPropertiesForm = me.getPreviewPropertiesForm(),
            previewPropertiesHeader = me.getPreviewPropertiesHeader(),
            device = me.getDeviceCommandsGrid().device;
        if (record) {
            var status = record.get('status').value,
                title = record.get('command').name,
                actionClmn = me.getDeviceCommandsGrid().down('uni-actioncolumn');

            previewPanel.setTitle(title);
            if(record.get('trackingCategory').id ==='trackingCategory.serviceCall'){
                previewForm.down('#tracking').setFieldLabel(Uni.I18n.translate('deviceCommands.view.serviceCall', 'MDC', 'Service call'));
                previewForm.down('#tracking').renderer = function (val) {
                    if (record.get('trackingCategory').activeLink != undefined && record.get('trackingCategory').activeLink) {
                        return '<a style="text-decoration: underline" href="' +
                            me.getController('Uni.controller.history.Router').getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: val.id})
                            + '">' + val.name + '</a>';
                    } else {
                        return val.id ? Ext.String.htmlEncode(val.id) : '-';
                    }
                }
            } else {
                previewForm.down('#tracking').setFieldLabel(Uni.I18n.translate('deviceCommands.view.trackingSource', 'MDC', 'Tracking source'));
                previewForm.down('#tracking').renderer = function (val) {
                    return !Ext.isEmpty(val) && !Ext.isEmpty(val.name) ? Ext.String.htmlEncode(val.name) : '-';
                }
            }
            previewForm.loadRecord(record);
            previewPropertiesForm.loadRecord(record);
            if (status == 'CommandWaiting' || status == 'CommandPending') {
                actionsButton.show();
                actionsButton.menu.device = device;
                actionsButton.menu.record = record;
                if (!!actionClmn) {
                    actionClmn.menu.device = device;
                    actionClmn.menu.deviceId = device.get('name');
                }
            } else {
                actionsButton.hide();
            }
            if (!Ext.isEmpty(record.get('properties'))) {
                previewPropertiesHeader.update('<h3>' + Uni.I18n.translate('deviceCommand.overview.attr', 'MDC', 'Attributes of {0}', [title]) + '</h3>');
                previewPropertiesHeader.show();
            } else {
                previewPropertiesHeader.hide();
            }
        }
    },

    configureMenu: function (menu) {
        menu.down('#triggerNow').show();
        if (!menu.record.get('willBePickedUpByComTask')) {
            menu.down('#triggerNow').hide();
        }
    },

    msgCategoryChange: function (combo, records) {
        var me = this,
            cat = records[0];
        me.getAddPropertyForm().hide();
        me.getAddPropertyHeader().hide();
        if (Ext.isDefined(cat)) {
            me.getCommandCombo().enable();
            me.getCommandCombo().reset();
            me.getCommandCombo().bindStore(cat.deviceMessageSpecs(), true);
        }
    },

    commandChange: function (combo, records) {
        var me = this,
            command = records[0].copy(),
            propertyHeader = me.getAddPropertyHeader();
        records[0].properties().each(function(record) {
            command.properties().add(record)
        });
        if (command) {
            me.getAddPropertyForm().loadRecord(command);
            if (command.properties() && (command.properties().getCount() > 0)) {
                propertyHeader.show();
                propertyHeader.update('<h3>' + Uni.I18n.translate('deviceCommand.overview.attr', 'MDC', 'Attributes of {0}', [command.get('name')]) + '</h3>');
                me.getAddPropertyForm().show();
            } else {
                me.getAddPropertyForm().hide();
                propertyHeader.hide();
            }
            if (!command.get('willBePickedUpByComTask')) {
                combo.markInvalid(Uni.I18n.translate('deviceCommand.add.willBePickedUpByComTask', 'MDC', 'This command is not part of a communication task on this device.'))
            }
            else if (!command.get('willBePickedUpByPlannedComTask')) {
                combo.markInvalid(Uni.I18n.translate('deviceCommand.add.willBePickedUpByPlannedComTask', 'MDC', 'This command is part of a communication task that is not planned to execute.'))
            }
        }
    },

    addCommand: function (btn) {
        var me = this,
            propertyForm = me.getAddPropertyForm(),
            addCommandPnl = me.getAddCommandPanel(),
            commandForm = me.getAddCommandForm();
        if (commandForm.isValid() && (propertyForm && propertyForm.isValid())) {
            addCommandPnl.down('#form-errors').hide();
            propertyForm.updateRecord();
            var newRecord = propertyForm.getRecord(),
                releaseDate = new Date(commandForm.getValues().releaseDate).getTime(),
                messageSpecification;
            if (!Ext.isEmpty(newRecord.get('id'))) {
                messageSpecification = {id: newRecord.get('id')}
            }
            newRecord.beginEdit();
            newRecord.set('id', '');
            releaseDate && newRecord.set('releaseDate', releaseDate);
            messageSpecification && newRecord.set('messageSpecification', messageSpecification);
            newRecord.set('status', null);
            if (Ext.isEmpty(commandForm.getValues().trackingCategory)) {
                newRecord.set('trackingCategory', null);
            }
            newRecord.endEdit();
            newRecord.save({
                url: '/api/ddr/devices/' + encodeURIComponent(btn.deviceId) + '/devicemessages',
                method: 'POST',
                success: function (record, operation) {
                    if (operation.success) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommand.overview.addSuccess', 'MDC', 'Command added'));
                        var router = me.getController('Uni.controller.history.Router'),
                            response = Ext.JSON.decode(operation.response.responseText);
                        router.getRoute('devices/device/commands').forward();
                        Ext.ModelManager.getModel('Mdc.model.Device').load(btn.deviceId, {
                            success: function (device) {
                                response['preferredComTask'] && me.showTriggerConfirmation(btn.deviceId, response['preferredComTask'].id, device);
                            }
                        });
                    }
                },
                failure: function (record, operation) {
                    if (operation && operation.response && operation.response.status === 400) {
                        me.formMarkInvalid(Ext.decode(operation.response.responseText));
                        addCommandPnl.down('#form-errors').show();
                        newRecord.set('id', messageSpecification.id);
                    }
                }
            });
        } else {
            addCommandPnl.down('#form-errors').show();
        }
    },

    formMarkInvalid: function (response) {
        var me = this;

        Ext.each(response.errors, function (error) {
            var failedField = me.getEditField(error.id);

            if (failedField) {
                failedField.markInvalid(error.msg);
            }
        });
    },

    getEditField: function (key) {
        var editPropertyForm = this.getAddPropertyForm(),
            editForm = this.getAddCommandForm();

        if (editPropertyForm) {
            if (key.indexOf('deviceMessageAttributes.')==0)
            {
                key = key.replace('deviceMessageAttributes.', '');
                return editPropertyForm.down('component[itemId='+key+']');
            }
        }
        if(editForm) {
            return editForm.down('component[name='+key+']');
        }
        return null;
    }


})
;
