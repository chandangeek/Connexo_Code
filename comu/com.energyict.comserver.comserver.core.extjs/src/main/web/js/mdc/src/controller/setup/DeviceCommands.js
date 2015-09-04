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
            comTaskId = record.get('preferredComTask').id,
            mRID = menu.mRID;
        me.showTriggerConfirmation(mRID, comTaskId)
    },

    showTriggerConfirmation: function (mRID, comTaskId) {
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
                        me.triggerCommand(mRID, comTaskId)
                    }
                },
                msg: Uni.I18n.translate('deviceCommand.overview.triggerMsg', 'MDC', 'Would you like to trigger command now?'),
                title: Uni.I18n.translate('deviceCommand.action.trigger', 'MDC', 'Trigger command')
            });
    },

    triggerCommand: function (mRID, comTaskId) {
        var me = this;
        Ext.Ajax.request({
            url: '/api/ddr/devices/' + encodeURIComponent(mRID) + '/comtasks/' + comTaskId + '/runnow',
            method: 'PUT',
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommand.overview.triggerSuccess', 'MDC', 'Command triggered'));
                me.getDeviceCommandsGrid().getStore().load()
            }
        })
    },

    revokeCommand: function (record, device) {
        var me = this,
            mRID = device.get('mRID'),
            title = Uni.I18n.translate('deviceCommand.overview.revokex', 'MDC', "Revoke '{0}'?",[record.get('command').name]);
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('deviceCommand.overview.revoke', 'MDC', 'Revoke')
        }).show({
                msg: Uni.I18n.translate('deviceCommand.overview.revokeMsg', 'MDC', 'This command will no longer be able to send'),
                title: title,
                fn: function (btnId) {
                    if (btnId == 'confirm') {
                        record.set('status', {value: 'CommandRevoked'});
                        record.save({
                            url: '/api/ddr/devices/' + encodeURIComponent(mRID) + '/devicemessages/',
                            success: function () {
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommand.overview.revokeSuccess', 'MDC', 'Command revoked'));
                                me.getDeviceCommandsGrid().getStore().load()
                            }
                        });
                    }
                }
            });
    },


    changeReleaseDate: function (record, device) {
        var me = this,
            title = Uni.I18n.translate('deviceCommand.overview.changeReleaseDateHeader', 'MDC', "Change release date of command '{0}'",[record.get('command').name]);
        Ext.widget('device-command-change-release-date', {
            title: title,
            record: record,
            listeners: {
                save: {
                    fn: function (newDate, record, oldDate) {
                        record.data.releaseDate = newDate;
                        record.save({
                            url: me.getStore('Mdc.store.DeviceCommands').getProxy().url,
                            callback: function (records, operation, success) {
                                if (success) {
                                    var store = me.getStore('Mdc.store.DeviceCommands');
                                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceCommand.changeReleaseDate.success', 'MDC', 'Release date changed'));
                                    me.getDeviceCommandsGrid().getStore().load()
                                } else {
                                    record.set('releaseDate', oldDate);
                                    me.getDeviceCommandsGrid().refresh();
                                }
                            }
                        });
                    }
                }
            }
        }).show();
    },

    showOverview: function (mrid) {
        var me = this,
            store = me.getStore('Mdc.store.DeviceCommands');

        store.getProxy().setUrl(mrid);

        Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
            success: function (device) {
                var widget = Ext.widget('deviceCommandsSetup', {
                    device: device
                });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    showAddOverview: function (mrid) {
        var me = this,
            catStore = me.getStore('Mdc.store.DeviceMessageCategories');

        Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
            success: function (device) {
                me.getStore('Mdc.store.DeviceCommands').getProxy().setUrl(device.get('mRID'));
                widget = Ext.widget('device-command-add', {
                    device: device
                });
                if (mrid) {
                    catStore.setMrid(mrid);
                    catStore.load()
                }
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    navigateAdd: function (btn) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/commands/add').forward({mRID: btn.mRID});
    },

    cancelClick: function (btn) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/commands').forward({mRID: btn.mRID});
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
            previewForm.loadRecord(record);
            previewPropertiesForm.loadRecord(record);
            if (status == 'CommandWaiting' || status == 'CommandPending') {
                actionsButton.show();
                actionsButton.menu.device = device;
                actionsButton.menu.record = record;
                if (!!actionClmn) {
                    actionClmn.menu.device = device;
                    actionClmn.menu.mRID = device.get('mRID');
                }
            } else {
                actionsButton.hide()
            }
            if (!Ext.isEmpty(record.get('properties'))) {
                previewPropertiesHeader.update('<h3>' + Uni.I18n.translate('deviceCommand.overview.attr', 'MDC', 'Attributes of {0}', [title]) + '</h3>');
                previewPropertiesHeader.show()
            } else {
                previewPropertiesHeader.hide()
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
        if (Ext.isDefined(cat)) {
            me.getCommandCombo().clearValue();
            me.getCommandCombo().bindStore(cat.deviceMessageSpecs());
        }
    },

    commandChange: function (combo, records) {
        var me = this,
            command = records[0],
            propertyHeader = me.getAddPropertyHeader();
        if (command) {
            me.getAddPropertyForm().loadRecord(command);
            if (command.properties() && (command.properties().getCount() > 0)) {
                propertyHeader.show();
                propertyHeader.update('<h3>' + Uni.I18n.translate('deviceCommand.overview.attr', 'MDC', 'Attributes of {0}', [command.get('name')]) + '</h3>');
            } else {
                propertyHeader.hide()
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
            commandForm = me.getAddCommandForm();
        if (commandForm.isValid() && (propertyForm && propertyForm.isValid())) {
            propertyForm.updateRecord();
            var record = propertyForm.getRecord(),
                releaseDate = new Date(commandForm.getValues().releaseDate).getTime(),
                messageSpecification;
            if (!Ext.isEmpty(record.get('id'))) {
                messageSpecification = {id: record.get('id')}
            }
            record.beginEdit();
            record.set('id', '');
            releaseDate && record.set('releaseDate', releaseDate);
            messageSpecification && record.set('messageSpecification', messageSpecification);
            record.set('status', null);
            record.endEdit();
            record.save({
                url: '/api/ddr/devices/' + encodeURIComponent(btn.mRID) + '/devicemessages',
                method: 'POST',
                success: function (record, operation) {
                    if (operation.success) {
                        var router = me.getController('Uni.controller.history.Router'),
                            response = Ext.JSON.decode(operation.response.responseText);
                        router.getRoute('devices/device/commands').forward();
                        response['preferredComTask'] && me.showTriggerConfirmation(btn.mRID, response['preferredComTask'].id);
                    }
                }
            });
        }
    }
})
;
