Ext.define('Dxp.view.tasks.AddDestination', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-add-destination',
    edit: false,
    returnLink: null,
    router: null,
    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#save-destination-button').setText(Uni.I18n.translate('general.save', 'DES', 'Save'));
            this.down('#save-destination-button').action = 'editDestination';
        } else {
            this.edit = edit;
            this.down('#save-destination-button').setText(Uni.I18n.translate('general.add', 'DES', 'Add'));
            this.down('#save-destination-button').action = 'addDestination';
        }
        if (this.returnLink) {
            this.down('#cancel-link').href = this.returnLink;
        }
    },
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('dataExport.addDestination', 'DES', 'Add destination'),
                itemId: 'add-destination-form',
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
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 500
                    },
                    {
                        xtype: 'combo',
                        store: Ext.create('Ext.data.Store', {
                            fields: [
                                {name: 'displayValue'},
                                {name: 'value'}
                            ],

                            data: [
                                //{label: Uni.I18n.translate('general.saveFile', 'DES', 'Save file'), value: 'FILE'},
                                //{label: Uni.I18n.translate('dataExport.mail', 'DES', 'Mail'), value: 'MAIL'}
                                //translation does not work?
                                {displayValue: 'Save file', value: 'FILE'},
                                {displayValue: 'Mail', value: 'EMAIL'}
                            ]
                        }),
                        name: 'method',
                        itemId: 'destination-methods-combo',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.method', 'DES', 'Method'),
                        queryMode: 'local',
                        displayField: 'displayValue',
                        valueField: 'value',
                    },

                    {
                        xtype: 'textfield',
                        name: 'fileName',
                        itemId: 'destination-file-name',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.fileName', 'DES', 'File name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'fileExtension',
                        itemId: 'destination-file-extension',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.fileExtension', 'DES', 'File extension'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'fileLocation',
                        itemId: 'destination-file-location',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.fileLocation', 'DES', 'File location'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textarea',
                        name: 'recipients',
                        itemId: 'destination-recipients',
                        width: 500,
                        height: 80,
                        required: true,
                        fieldLabel: Uni.I18n.translate('dataExport.recipients', 'DES', 'To'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'subject',
                        itemId: 'destination-subject',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.subject', 'DES', 'Subject'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'attachmentName',
                        itemId: 'destination-attachment-name',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('dataExport.attachmentName', 'DES', 'Attachment name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'textfield',
                        name: 'attachmentExtension',
                        itemId: 'destination-attachment-extension',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('dataExport.attachmentExtension', 'DES', 'Attachment extension'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'save-destination-button',
                                text: Uni.I18n.translate('general.add', 'DES', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-add-destination-link',
                                text: Uni.I18n.translate('general.cancel', 'DES', 'Cancel'),
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit);
    },
});





