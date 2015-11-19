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
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        width: 600,
                        margin: '0 0 10 0',
                        hidden: true
                    },
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
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('editProcess.deviceStates', 'DBP', 'Device states'),
                                itemId: 'ctn-device-states',
                                required: true,
                                width: 600,
                                labelWidth: 234,
                                layout: 'vbox',
                                items: [
                                    {
                                        xtype: 'container',
                                        width: '100%',
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'component',
                                                html: Uni.I18n.translate('editProcess.noDeviceStates', 'DBP', 'No device states have been added'),
                                                itemId: 'empty-device-states-grid',
                                                style: {
                                                    'font': 'italic 13px/17px Lato',
                                                    'color': '#686868',
                                                    'margin-top': '6px',
                                                    'margin-right': '10px'
                                                },
                                                hidden: true
                                            },
                                            {
                                                xtype: 'component',
                                                itemId: 'dbp-add-device-states-to-right-component',
                                                flex: 1
                                            },
                                            {
                                                itemId: 'add-device-states-button',
                                                xtype: 'button',
                                                margin: '0 0 10 0',
                                                text: Uni.I18n.translate('editProcess.addDeviceStates', 'DBP', 'Add device states')
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'device-states-grid',
                                        itemId: 'dbp-device-states-grid',
                                        processId: me.processId,
                                        store: me.processModel.deviceStates()
                                    }
                                ]
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
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('editProcess.privileges', 'DBP', 'Privileges'),
                                itemId: 'ctn-privileges',
                                required: true,
                                width: 600,
                                labelWidth: 234,
                                layout: 'vbox',
                                items: [
                                    {
                                        xtype: 'container',
                                        width: '100%',
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'component',
                                                html: Uni.I18n.translate('editProcess.noDeviceStates', 'DBP', 'No privileges have been added'),
                                                itemId: 'empty-privileges-grid',
                                                style: {
                                                    'font': 'italic 13px/17px Lato',
                                                    'color': '#686868',
                                                    'margin-top': '6px',
                                                    'margin-right': '10px'
                                                },
                                                hidden: true
                                            },
                                            {
                                                xtype: 'component',
                                                itemId: 'dbp-add-privileges-to-right-component',
                                                flex: 1
                                            },
                                            {
                                                itemId: 'add-privileges-button',
                                                xtype: 'button',
                                                margin: '0 0 10 0',
                                                text: Uni.I18n.translate('editProcess.addPrivileges', 'DBP', 'Add privileges')
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'privileges-grid',
                                        itemId: 'dbp-privileges-grid',
                                        processId: me.processId,
                                        store: me.processModel.privileges()
                                    }
                                ]
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
    }
});

