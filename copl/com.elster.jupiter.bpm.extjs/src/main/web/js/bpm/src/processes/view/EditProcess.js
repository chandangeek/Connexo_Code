/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.view.EditProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-edit-process',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Bpm.processes.view.PrivilegesGrid',
        'Uni.property.form.GroupedPropertyForm',
        'Uni.property.view.property.SelectionGrid'
    ],

    editProcessRecord: null,
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
                        name: 'type',
                        width: 600,
                        labelWidth: 250,
                        fieldLabel: Uni.I18n.translate('editProcess.associatedTo', 'BPM', 'Start on'),
                        enforceMaxLength: true,
                        required: true,
                        store: 'Bpm.processes.store.Associations',
                        editable: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'type',
                        forceSelection: true

                    },
                    {
                        xtype: 'form',
                        title: Uni.I18n.translate('editProcess.startWhenIn', 'BPM', 'Start when in'),
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },

                        itemId: 'custom-form',
                        items: [
                            {
                                xtype: 'property-form',
                                defaults: {
                                    labelWidth: 235,
                                    resetButtonHidden: true
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        title: Uni.I18n.translate('editProcess.startWhenAllowed', 'BPM', 'Start when allowed'),
                        ui: 'medium',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        itemId: 'processes-form',

                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('editProcess.privileges', 'BPM', 'Privileges'),
                                itemId: 'ctn-privileges',
                                required: true,
                                width: 600,
                                labelWidth: 234,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                name: 'processPrivileges',
                                items: [
                                    {
                                        margin: '-7 0 0 0',
                                        xtype: 'privileges-grid',
                                        itemId: 'bpm-privileges-grid',
                                        padding: '0 0 5 0'
                                    },
                                    {
                                        html: '<div id="error-message" class="x-form-invalid-under" style="display:none"></div>'
                                    }
                                ],
                                // add validation support
                                isFormField: true,
                                markInvalid: function (errors) {
                                    var errorMessage = this.getEl().query('#error-message')[0];
                                    errorMessage.style.display ='block';
                                    errorMessage.innerHTML=errors;
                                },
                                clearInvalid: function () {
                                    this.getEl().query('#error-message')[0].style.display ='none';
                                },
                                isValid: function () {
                                    return true;
                                },
                                getModelData: function () {
                                    return null;
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
                                text: Uni.I18n.translate('general.save', 'BPM', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-save'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'BPM', 'Cancel'),
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

