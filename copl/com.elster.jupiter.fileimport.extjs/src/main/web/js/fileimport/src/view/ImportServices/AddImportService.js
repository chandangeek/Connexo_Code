Ext.define('Fim.view.importServices.AddImportService', {
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
								name: 'file-importer',
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
								valueField: 'id'
							},
							{
								xtype: 'displayfield',
								itemId: 'no-file-importer',
								hidden: true,
								value: '<div style="color: #FF0000">' + Uni.I18n.translate('general.noFileImporter', 'FIM', 'No file importer defined yet.') + '</div>',
								width: 250,
								margin: '0 0 0 265'
							}
						]
					},
					{
						xtype: 'textfield',
						name: 'importFolder',
						itemId: 'txt-import-folder',
						required: true,
						width: 600,                            							
						fieldLabel: Uni.I18n.translate('importService.importFolder', 'FIM', 'Import folder'),
						enforceMaxLength: true
					},
					{
						xtype: 'textfield',
						name: 'filePattern',
						itemId: 'txt-file-pattern',
						width: 600,                            							
						fieldLabel: Uni.I18n.translate('importService.filePattern', 'FIM', 'File pattern'),
						enforceMaxLength: true
					},	
					{
						xtype: 'fieldcontainer',	
						fieldLabel: Uni.I18n.translate('importService.folderScanFrequency', 'FIM', 'Folder scan frequency'),										
						required: true,
						layout: 'hbox',								
						items: [
							{
								xtype: 'numberfield',
								fieldLabel: Uni.I18n.translate('importService.folderScanEvery', 'FIM', 'Every'),
								labelWidth: 40,
								width: 120,
								maxValue: 60,
								minValue: 1,
								defaultValue: 1, 
								name: 'folderScanFrequency',
								itemId: 'num-folder-scan-frequency'
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
						name: 'inProgressFolder',
						itemId: 'txt-in-progress-folder',
						width: 600,                            							
						fieldLabel: Uni.I18n.translate('importService.inProgressFolder', 'FIM', 'In progress folder'),
						enforceMaxLength: true
					},	
					{
						xtype: 'textfield',
						name: 'successFolder',
						itemId: 'txt-success-folder',
						width: 600,                            							
						fieldLabel: Uni.I18n.translate('importService.successFolder', 'FIM', 'Success folder'),
						enforceMaxLength: true
					},	
					{
						xtype: 'textfield',
						name: 'failureFolder',
						itemId: 'txt-failure-folder',
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
    }
});

