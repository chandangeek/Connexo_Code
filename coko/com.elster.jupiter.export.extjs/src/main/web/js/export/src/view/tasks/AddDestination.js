/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.AddDestination', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-add-destination',
    edit: false,
    returnLink: null,
    router: null,
    fieldIdsWithErrors:[],
    labelWidth: 250,
    fieldWidth: 583,
    fieldWithTooltipWidth: 319,

    requires: [
        'Dxp.view.common.ExportTagsInfoButton',
        'Uni.util.FormErrorMessage'
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
                    labelWidth: me.labelWidth
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        itemId: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: me.fieldWidth
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
                                {displayValue: Uni.I18n.translate('destination.ftp','DES','FTP'), value: 'FTP'},
                                {displayValue: Uni.I18n.translate('destination.ftps','DES','FTPS'), value: 'FTPS'}
                            ]
                        }),
                        name: 'method',
                        itemId: 'destination-methods-combo',
                        width: me.fieldWidth,
                        emptyText: Uni.I18n.translate('addDataExportTask.destinationMethodPrompt', 'DES', 'Select a method...'),
                        submitEmptyText: false,
                        required: true,
                        forceSelection: true,
                        allowBlank: false,
                        editable: false,
                        fieldLabel: Uni.I18n.translate('general.method', 'DES', 'Method'),
                        queryMode: 'local',
                        displayField: 'displayValue',
                        valueField: 'value',
                        listeners: {
                            select: {
                                fn: me.onMethodSelect,
                                scope: me
                            }
                        }
                    },

                    {
                        xtype: 'textfield',
                        name: 'server',
                        itemId: 'hostname',
                        width: me.fieldWidth,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.hostName', 'DES', 'Hostname'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        msgTarget: 'under'
                    },
                    {
                        xtype: 'numberfield',
                        name: 'port',
                        itemId: 'dxp-port-field',
                        width: me.fieldWidth,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.port', 'DES', 'Port'),
                        value: 21,
                        minValue: 1,
                        maxValue: 65535,
                        allowDecimals: false,
                        allowExponential: false,
                        allowBlank: false,
                        hideTrigger: true,
                        msgTarget: 'under'
                    },
                    {
                        xtype: 'textfield',
                        name: 'user',
                        itemId: 'user-field',
                        width: me.fieldWidth,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.user', 'DES', 'User'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        msgTarget: 'under'
                    },
                    {
                        xtype: 'password-field',
                        name: 'password',
                        itemId: 'password-field',
                        width: me.fieldWidth,
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
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'fileName',
                                itemId: 'destination-file-name',
                                width: me.fieldWithTooltipWidth,
                                allowBlank: false,
                                enforceMaxLength: true,
                                maxLength: 80,
                                msgTarget: 'under'
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
                        width: me.fieldWidth,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.fileExtension', 'DES', 'File extension'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        msgTarget: 'under'
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.fileLocation', 'DES', 'File location'),
                        layout: 'hbox',
                        required: true,
                        itemId: 'dxp-file-location-container',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'fileLocation',
                                itemId: 'destination-file-location',
                                width: me.fieldWithTooltipWidth,
                                allowBlank: false,
                                enforceMaxLength: true,
                                maxLength: 80,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'dxp-export-tags-info-button'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'dxp-destination-recipients-container',
                        fieldLabel: Uni.I18n.translate('dataExport.recipients', 'DES', 'To'),
                        layout: 'hbox',
                        required: true,
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'recipients',
                                itemId: 'destination-recipients',
                                width: me.fieldWithTooltipWidth,
                                required: true,
                                allowBlank: false,
                                enforceMaxLength: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'button',
                                itemId: 'txt-user-name-info',
                                tooltip: Uni.I18n.translate('dataExport.recipients.tooltip', 'DES', 'Separate multiple e-mailaddresses by semicolons (;)'),
                                text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                                disabled: true, // to avoid a hand cursor
                                ui: 'blank',
                                shadow: false,
                                margin: '6 0 0 10',
                                width: 16,
                                tabIndex: -1
                            }
                        ]
                    },

                    {
                        xtype: 'textfield',
                        name: 'subject',
                        itemId: 'destination-subject',
                        width: me.fieldWidth,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.subject', 'DES', 'Subject'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        msgTarget: 'under'
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('dataExport.attachmentName', 'DES', 'Attachment name'),
                        layout: 'hbox',
                        required: true,
                        itemId: 'dxp-attachment-name-container',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'attachmentName',
                                itemId: 'destination-attachment-name',
                                width: me.fieldWithTooltipWidth,
                                allowBlank: false,
                                enforceMaxLength: true,
                                maxLength: 80,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'dxp-export-tags-info-button'
                            }
                        ]
                    },

                    {
                        xtype: 'textfield',
                        name: 'attachmentExtension',
                        itemId: 'destination-attachment-extension',
                        width: me.fieldWidth,
                        required: true,
                        fieldLabel: Uni.I18n.translate('dataExport.attachmentExtension', 'DES', 'Attachment extension'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        msgTarget: 'under'
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
                                disabled: true,
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

    onMethodSelect: function(combo, selectedItems) {
        var me = this,
            addBtn = me.down('#save-destination-button');

        addBtn.setDisabled(selectedItems.length===0);
        // Reset all error indications
        me.fieldIdsWithErrors = [];
        switch(selectedItems[0].get('value')) {
            case 'FILE':
                me.isFieldValid('#destination-file-name', true, "");
                me.isFieldValid('#destination-file-extension', true, "");
                me.isFieldValid('#destination-file-location', true, "");
                break;
            case 'EMAIL':
                me.isFieldValid('#destination-recipients', true, "");
                me.isFieldValid('#destination-subject', true, "");
                me.isFieldValid('#destination-attachment-name', true, "");
                me.isFieldValid('#destination-attachment-extension', true, "");
                break;
            case 'FTP':
                me.isFieldValid('#hostname', true, "");
                me.isFieldValid('#user-field', true, "");
                me.isFieldValid('#destination-file-name', true, "");
                me.isFieldValid('#destination-file-extension', true, "");
                me.isFieldValid('#destination-file-location', true, "");
                break;
            case 'FTPS':
                me.isFieldValid('#hostname', true, "");
                me.isFieldValid('#user-field', true, "");
                me.isFieldValid('#destination-file-name', true, "");
                me.isFieldValid('#destination-file-extension', true, "");
                me.isFieldValid('#destination-file-location', true, "");
                break;
            default:
                break;
        }
    },

    isFileNameValid: function() {
        return this.isRegexFieldValid(
            '#destination-file-name',
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=\/:\\@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileName', 'DES',
                "This field contains a space or one of the following invalid characters: {0}", "#<>$+%!`&*'|?\{@\}\"=/:\\", false)
        );
    },

    isFileLocationValid: function() {
        return this.isRegexFieldValid(
            '#destination-file-location',
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=:@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileLocation', 'DES',
                "This field contains a space or one of the following invalid characters: {0}", "#<>$+%!`&*'|?{@}\"=:", false)
        );
    },

    isFileExtensionValid: function() {
        return this.isRegexFieldValid(
            '#destination-file-extension',
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=\/:\\@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileName', 'DES',
                "This field contains a space or one of the following invalid characters: {0}", "#<>$+%!`&*'|?{@}\"=/:\\", false)
        );
    },

    isAttachmentNameValid: function() {
        return this.isRegexFieldValid(
            '#destination-attachment-name',
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=\/:\\@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileName', 'DES',
                "This field contains a space or one of the following invalid characters: {0}", "#<>$+%!`&*'|?{@}\"=/:\\", false)
        );
    },

    isAttachmentExtensionValid: function() {
        return this.isRegexFieldValid(
            '#destination-attachment-extension',
            /[#\<\>$\+%\!`\&\*'\|\{\}\?"\=\/:\\@\s]/,
            Uni.I18n.translate('dataExport.invalidCharacters.fileName', 'DES',
                "This field contains a space or one of the following invalid characters: {0}", "#<>$+%!`&*'|?{@}\"=/:\\", false)
        );
    },

    isRecipientsValid: function() {
        return this.isFieldNonEmpty('#destination-recipients');
    },

    isSubjectValid: function() {
        return this.isFieldNonEmpty('#destination-subject');
    },

    isServerNameValid: function() {
        return this.isFieldNonEmpty('#hostname');
    },

    isPortValid: function() {
        var me = this,
            fieldId = '#dxp-port-field',
            field = me.down(fieldId),
            valid =
                me.isFieldValid(
                    fieldId,
                    field.getValue() !== null,
                    Uni.I18n.translate('dataExport.requiredField', 'DES', 'This field is required')
                )
                &&
                me.isFieldValid(
                    fieldId,
                    field.getValue() > 0 && field.getValue() < 65536,
                    Uni.I18n.translate('dataExport.portInvalid', 'DES', 'Port should be between 1 and 65535')
                );
        return valid;
    },

    isUserNameValid: function() {
        return this.isFieldNonEmpty('#user-field');
    },

    isFieldNonEmpty: function(fieldId) {
        var me= this,
            field = me.down(fieldId);
        return me.isFieldValid(
            fieldId,
            field.getValue() !== null && field.getValue().length > 0,
            Uni.I18n.translate('dataExport.requiredField', 'DES', 'This field is required')
        );
    },

    isRegexFieldValid: function(fieldId, regexOfInvalidChars, errorMsg) {
        var me = this,
            component,
            value = me.down(fieldId).getValue(),
            allowedTags = [];

        // a. First, check for emptyness
        if (value === null || value.length === 0) {
            return me.isFieldValid(fieldId, false, Uni.I18n.translate('dataExport.requiredField','DES','This field is required'));
        }

        // b. Then remove the allowed tags
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
        allowedTags.push(/\<dateformat:[^#\<\>$\+%\!`\&\*'\|\{\}\?"\=\/:\\@\s]+\>/);
        allowedTags.push('<identifier>');
        for (var i=0, max=allowedTags.length; i < max; i++) {
            value = value.replace(allowedTags[i], '');
        }

        // c. Then check for invalid characters
        return me.isFieldValid(fieldId, value.search(regexOfInvalidChars) === -1, errorMsg);
    },

    isFieldValid: function(fieldId, conditionToBeValid, errorMsg) {
        var me = this,
            component = me.down(fieldId);

        if (conditionToBeValid) {
            component.unsetActiveError();
            if (me.fieldIdsWithErrors.indexOf(fieldId) !== -1) {
                me.fieldIdsWithErrors.splice(me.fieldIdsWithErrors.indexOf(fieldId), 1);
            }
            if (me.fieldIdsWithErrors.length === 0) {
                me.down('#form-errors').hide();
            }
        } else {
            component.setActiveError(errorMsg);
            me.down('#form-errors').show();
            if (me.fieldIdsWithErrors.indexOf(fieldId) === -1) {
                me.fieldIdsWithErrors.push(fieldId);
            }
        }
        component.doComponentLayout();
        return conditionToBeValid;
    },

    isFormValid: function() {
        var me = this,
            methodBox = me.down('#destination-methods-combo');

        switch (methodBox.getValue()) {
            case 'FILE':
                // Intentionally done in multiple lines:
                var valid1 = me.isFileNameValid();
                var valid2 = me.isFileExtensionValid();
                var valid3 = me.isFileLocationValid();
                return valid1 && valid2 && valid3;
            case 'EMAIL':
                // Intentionally done in multiple lines:
                var valid1 = me.isRecipientsValid();
                var valid2 = me.isSubjectValid();
                var valid3 = me.isAttachmentNameValid();
                var valid4 = me.isAttachmentExtensionValid();
                return valid1 && valid2 && valid3 && valid4;
            case 'FTP':
            case 'FTPS':
                var valid1 = me.isServerNameValid();
                var valid2 = me.isUserNameValid();
                var valid3 = me.isFileNameValid();
                var valid4 = me.isFileExtensionValid();
                var valid5 = me.isFileLocationValid();
                var valid6 = me.isPortValid();
                return valid1 && valid2 && valid3 && valid4 && valid5 && valid6;
            default:
                return true;
        }
    }

});