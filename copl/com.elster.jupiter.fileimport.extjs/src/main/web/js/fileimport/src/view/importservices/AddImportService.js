/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.importservices.AddImportService', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fim-add-import-service',
    //overflowY: true,
    models: [
        'Ldr.model.LogLevel'
    ],
    requires: [
        'Uni.form.field.DateTime',
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm',
        'Uni.form.field.CheckboxWithExplanation',
        'Ldr.store.LogLevels'
    ],




    edit: false,
    importServiceRecord: null,
    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#btn-add').setText(Uni.I18n.translate('general.save', 'FIM', 'Save'));
            this.down('#btn-add').action = 'edit';
            this.down('#cbo-file-importer').setDisabled(true);
        } else {
            this.edit = edit;
            this.down('#btn-add').setText(Uni.I18n.translate('general.add', 'FIM', 'Add'));
            this.down('#btn-add').action = 'add';
        }
        this.down('#btn-cancel-link').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-add-import-service',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
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
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'txt-name',
                        required: true,
                        width: 600,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('general.name', 'FIM', 'Name'),
                        enforceMaxLength: true,
                        listeners: {
                            afterrender: function (field) {
                                field.focus(false, 200);
                            }
                        }
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.logLevel', 'FIM', 'Log level'),
                        emptyText: Uni.I18n.translate('general.logLevel.selectPrompt', 'FIM', 'Select a log level...'),
                        required: true,
                        name: 'logLevel',
                        width: 600,
                        itemId: 'fim-data-logLevels',
                        allowBlank: false,
                        store: 'LogLevelsStore',
                        queryMode: 'local',
                        displayField: 'displayValue',
                        valueField: 'id',
                        forceSelection: true
                    },
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-file-importer',
                                name: 'importerName',
                                width: 600,
                                fieldLabel: Uni.I18n.translate('importService.fileImporter', 'FIM', 'File importer'),
                                labelWidth: 250,
                                required: true,
                                store: 'Fim.store.FileImporters',
                                editable: true,
                                disabled: false,
                                emptyText: Uni.I18n.translate('importService.fileImporterPrompt', 'FIM', 'Select a file importer...'),
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'displayName',
                                valueField: 'name',
                                forceSelection: true
                            },
                            {
                                xtype: 'container',
                                itemId: 'no-file-importer',
                                hidden: true,
                                html: '<div style="color: #EB5642">' + Uni.I18n.translate('general.noFileImporter', 'FIM', 'No file importer defined yet.') + '</div>',
                                margin: '0 0 0 265'
                            }
                        ]
                    },
                    {
                        xtype: 'textfield',
                        name: 'importDirectory',
                        itemId: 'txt-import-folder',
                        maskRe: /\S/,
                        required: true,
                        allowBlank: false,
                        width: 600,
                        fieldLabel: Uni.I18n.translate('general.importFolder', 'FIM', 'Import folder'),
                        enforceMaxLength: true
                    },
                    {
                        xtype: 'checkbox-with-explanation',
                        name: 'activeInUI',
                        itemId: 'allow-uploads',
                        fieldLabel: Uni.I18n.translate('general.allowUploads', 'FIM', 'Allow uploads in application'),
                        explanation: Uni.I18n.translate('general.allowUploads.note', 'FIM', "Makes upload available on the 'Upload file for import' page")
                    },
                    {
                        xtype: 'container',
                        margin: '0 0 8 0',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'pathMatcher',
                                itemId: 'txt-file-pattern',
                                width: 600,
                                labelWidth: 250,
                                fieldLabel: Uni.I18n.translate('importService.filePattern', 'FIM', 'File pattern'),
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'button',
                                itemId: 'txt-file-pattern-info',
                                tooltip: Uni.I18n.translate('importService.filePattern.tooltip', 'FIM', 'Click for more information'),
                                text: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                                ui: 'blank',
                                shadow: false,
                                margin: '6 0 0 10',
                                width: 16,
                                tabIndex: -1,
                                listeners: {
                                    click: function () {
                                        var me = Ext.getCmp(this.id);
                                        me.up('contentcontainer').fireEvent('displayinfo', me);
                                    }
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('importService.folderScanFrequency', 'FIM', 'Folder scan frequency'),
                        //required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'numberfield',
                                fieldLabel: Uni.I18n.translate('importService.folderScanEvery', 'FIM', 'Every'),
                                labelWidth: 40,
                                width: 120,
                                maxValue: 10800,
                                minValue: 1,
                                defaultValue: 1,
                                name: 'scanFrequency',
                                itemId: 'num-folder-scan-frequency',
                                listeners: {
                                    blur: me.frequencyScanNumberFieldValidation
                                }
                            },
                            {
                                xtype: 'label',
                                margin: '10 0 0 20',
                                text: Uni.I18n.translate('importService.folderScanUnit', 'FIM', 'minute(s)'),
                                itemId: 'cbo-folder-scan-unit'
                            }
                        ]
                    },
                    {
                        xtype: 'textfield',
                        name: 'inProcessDirectory',
                        itemId: 'txt-in-progress-folder',
                        maskRe: /\S/,
                        required: true,
                        allowBlank: false,
                        width: 600,
                        fieldLabel: Uni.I18n.translate('importService.inProgressFolder', 'FIM', 'In progress folder'),
                        enforceMaxLength: true
                    },
                    {
                        xtype: 'textfield',
                        name: 'successDirectory',
                        itemId: 'txt-success-folder',
                        maskRe: /\S/,
                        required: true,
                        allowBlank: false,
                        width: 600,
                        fieldLabel: Uni.I18n.translate('importService.successFolder', 'FIM', 'Success folder'),
                        enforceMaxLength: true
                    },
                    {
                        xtype: 'textfield',
                        name: 'failureDirectory',
                        itemId: 'txt-failure-folder',
                        maskRe: /\S/,
                        required: true,
                        allowBlank: false,
                        width: 600,
                        fieldLabel: Uni.I18n.translate('importService.failureFolder', 'FIM', 'Failure folder'),
                        enforceMaxLength: true
                    },
                    {
                        xtype: 'grouped-property-form'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'FIM', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-add'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'FIM', 'Cancel'),
                                href: '#/administration/importservices',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit, me.returnLink);
    },
    frequencyScanNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue || value > field.maxValue) {
            field.setValue(field.minValue);
        }
    }
});

