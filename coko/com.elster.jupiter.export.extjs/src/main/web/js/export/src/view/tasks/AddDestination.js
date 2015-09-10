Ext.define('Dxp.view.tasks.AddDestination', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-add-destination',
    edit: false,
    returnLink: null,
    router: null,
    requires: [
        'Dxp.view.common.ExportTagsInfoButton'
    ],
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
                                {displayValue: Uni.I18n.translate('destination.file','DES','Save file'), value: 'FILE'},
                                {displayValue: Uni.I18n.translate('destination.email','DES','Mail'), value: 'EMAIL'},
                                {displayValue: Uni.I18n.translate('destination.ftp','DES','Ftp'), value: 'FTP'}
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
                        name: 'server',
                        itemId: 'ftp-server',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.ftpServer', 'DES', 'FTP server'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },
                    {
                        xtype: 'textfield',
                        name: 'user',
                        itemId: 'user-field',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.user', 'DES', 'User'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },
                    {
                        xtype: 'password-field',
                        name: 'password',
                        itemId: 'password-field',
                        width: 500,
                        fieldLabel: Uni.I18n.translate('general.password', 'DES', 'Password'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.fileName', 'DES', 'File name'),
                        layout: 'hbox',
                        required: true,
                        itemId: 'dxp-file-name-container',
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'fileName',
                                itemId: 'destination-file-name',
                                width: 236,
                                allowBlank: false,
                                enforceMaxLength: true,
                                maxLength: 80,
                                listeners: {
                                    blur: {
                                        fn: me.fileNameValidation,
                                        scope: me
                                    }
                                }
                            },
                            {
                                xtype: 'dxp-export-tags-info-button'
                            }
                        ]
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
                        maxLength: 80,
                        msgTarget: 'under',
                        listeners: {
                            blur: {
                                fn: me.fileExtensionValidation,
                                scope: me
                            }
                        }
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.fileLocation', 'DES', 'File location'),
                        layout: 'hbox',
                        required: true,
                        itemId: 'dxp-file-location-container',
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'fileLocation',
                                itemId: 'destination-file-location',
                                width: 236,
                                allowBlank: false,
                                enforceMaxLength: true,
                                maxLength: 80,
                                listeners: {
                                    blur: {
                                        fn: me.fileLocationValidation,
                                        scope: me
                                    }
                                }
                            },
                            {
                                xtype: 'dxp-export-tags-info-button'
                            }
                        ]
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

    fileNameValidation: function(field) {
        this.fieldValidation(
            field,
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=\/:\\@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileName','DES',"This field contains a space or one of the following invalid characters: #<>$+%!`&*'|?\{@\}\"=/:\\"),
            '#dxp-file-name-container'
        );
    },

    fileLocationValidation: function(field) {
        this.fieldValidation(
            field,
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=:@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileLocation','DES',"This field contains a space or one of the following invalid characters: #<>$+%!`&*'|?\{@\}\"=:"),
            '#dxp-file-location-container'
        );
    },

    fileExtensionValidation: function(field) {
        this.fieldValidation(
            field,
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=\/:\\@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileName','DES',"This field contains a space or one of the following invalid characters: #<>$+%!`&*'|?\{@\}\"=/:\\"),
            '#destination-file-extension'
        );
    },

    fieldValidation: function(field, regexOfInvalidChars, errorMsg, errorMsgComponentId) {
        var me = this,
            component,
            value = field.getValue(),
            allowedTags = [];

        // a. First remove the allowed tags
        allowedTags.push('<date>');
        allowedTags.push('<time>');
        allowedTags.push('<sec>');
        allowedTags.push('<millisec>');
        allowedTags.push('<dateyear>');
        allowedTags.push('<datemonth>');
        allowedTags.push('<dateday>');
        allowedTags.push('<datadate>');
        allowedTags.push('<datatime>');
        allowedTags.push('<dataenddate>');
        allowedTags.push('<dataendtime>');
        allowedTags.push('<seqnrwithinday>');
        allowedTags.push('<datayearandmonth>');
        allowedTags.push(/\<dateformat:[a-zA-Z]+\>/);
        allowedTags.push('<identifier>');
        for (var i=0, max=allowedTags.length; i < max; i++) {
            value = value.replace(allowedTags[i], '');
        }

        // b. Then check for invalid characters
        component = me.down(errorMsgComponentId);
        if (value.search(regexOfInvalidChars) !== -1) {
            component.setActiveError(errorMsg);
        } else {
            component.unsetActiveError();
        }
        component.doComponentLayout();
    }
});