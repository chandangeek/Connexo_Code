Ext.define('Fim.view.importservices.AddImportService', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fim-add-import-service',
    //overflowY: true,
    requires: [
        'Uni.form.field.DateTime',
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm'
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
                        width: 400,
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
                        enforceMaxLength: true
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
                                editable: false,
                                disabled: false,
                                emptyText: Uni.I18n.translate('importService.fileImporterPrompt', 'FIM', 'Select a file importer...'),
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'displayName',
                                valueField: 'name'
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
                                xtype: 'box',
                                itemId: 'txt-file-pattern-info',
                                cls: 'uni-info-icon',
                                qtip: Uni.I18n.translate('importService.filePatternInfo', 'FIM', 'File pattern info'),

                                autoEl: {
                                    tag: 'img',
                                    src: "../sky/build/resources/images/shared/icon-info-small.png",
                                    width: 16,
                                    height: 16
                                },
                                margin: '6 0 0 10',
                                style: {
                                    cursor: 'pointer'
                                },
                                listeners: {
                                    el: {
                                        click: function () {
                                            var me = Ext.getCmp(this.id);
                                            me.up('contentcontainer').fireEvent('displayinfo', me);
                                        }
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
                        margin: '20 0 0 0',
                        fieldLabel: '&nbsp',
                        labelAlign: 'right',
                        labelWidth: 260,
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

