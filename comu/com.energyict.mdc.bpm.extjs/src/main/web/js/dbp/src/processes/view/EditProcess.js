Ext.define('Dbp.processes.view.EditProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-edit-process',
    requires: [
        'Uni.util.FormErrorMessage',
        'Dbp.processes.view.DeviceStatesGrid',
        'Dbp.processes.view.PrivilegesGrid'
    ],

    processModel: null,
    processId: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-edit-process',
                ui: 'large',
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-associated-to',
                        name: 'associatedTo',
                        width: 600,
                        labelWidth: 250,
                        fieldLabel: Uni.I18n.translate('editProcess.associatedTo', 'DBP', 'Start on'),
                        enforceMaxLength: true,
                        required: true,
                        store: 'Dbp.processes.store.Associations',
                        editable: false,
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'value'
                    },
                    {
                        xtype: 'form',
                        title: Uni.I18n.translate('editProcess.startWhenInDeviceState', 'DBP', 'Start when in device state'),
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        itemId: 'device-states-form',
                        items: [
                            {
                                xtype: 'preview-container',
                                margin: '0 0 0 250',
                                itemId: 'device-states-grid-preview-container',
                                grid: {
                                    xtype: 'device-states-grid',
                                    itemId: 'dbp-device-states-grid',
                                    processId: me.processId,
                                    store: me.processModel.deviceStates()
                                },
								emptyComponent: {
									xtype: 'no-items-found-panel',
									title: Uni.I18n.translate('editProcess.noDeviceStateFound','DBP','No device state found'),
									reasons: [
										Uni.I18n.translate('editProcess.noDeviceState.reason1','DBP','No device states have been defined yet.')
									],
									stepItems: [
										{
											text: Uni.I18n.translate('DBP.addDeviceStates','DBP','Add device states'),
                                            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
											itemId: 'device-state-grid-add-link',
											href: ''
										}
									]
								}//,
                                //hidden:true
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        title: Uni.I18n.translate('editProcess.startWhenAllowed', 'DBP', 'Start when allowed'),
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        itemId: 'processes-form',
                        items: [
                            {
                                xtype: 'preview-container',
                                margin: '0 0 0 250',
                                itemId: 'privileges-grid-preview-container',
                                grid: {
                                    xtype: 'privileges-grid',
                                    itemId: 'dbp-privileges-grid',
                                    processId: me.processId,
                                    store: me.processModel.privileges()
                                },
								emptyComponent: {
									xtype: 'no-items-found-panel',
									title: Uni.I18n.translate('editProcess.noPrivilegeFound','DBP','No privileges found'),
									reasons: [
										Uni.I18n.translate('editProcess.noPrivilege.reason1','DBP','No privileges have been defined yet.')
									],
									stepItems: [
										{
											text: Uni.I18n.translate('editProcess.addPrivileges','DBP','Add privileges'),
											privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
											itemId: 'privileges-grid-add-link',
                                            href: ''
                                        }
									]
								}
                            }

                        ]
                    },
                    {
                        xtype: 'container',
                        margin: '0 0 0 265',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('general.save', 'DBP', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-save'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'DBP', 'Cancel'),
                                href: '#/administration/managementprocesses',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
       // me.setEdit(me.edit, me.returnLink);
    }
});

