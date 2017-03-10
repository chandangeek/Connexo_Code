/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.controller.UploadFile', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],
    views: [
        'Fim.view.uploadfile.UploadFile'
    ],
    stores: [
        'Fim.store.AvailableImportServices'
    ],
    models: [
        'Fim.model.UploadFile'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'upload-file'
        }
    ],

    init: function () {
        this.control({
            'upload-file #upload-file-button': {
                click: this.uploadFile
            },
            'upload-file #import-service-form': {
                displayinfo: this.displayInfo
            }
        });
    },

    showUploadFileForImportInAdmin: function () {
        this.showUploadFileForImport(true);
    },

    showUploadFileForImportInMultisenseOrInsight: function () {
        this.showUploadFileForImport();
    },

    showUploadFileForImport: function (admin) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('upload-file', {
                itemId: 'upload-file',
                router: router,
                returnLink: admin ? router.getRoute('administration').buildUrl() : router.getRoute('workspace').buildUrl()
            }),
            importServicesCombo = widget.down('#import-service-combobox'),
            importServicesStore = me.getStore('Fim.store.AvailableImportServices');

        me.getApplication().fireEvent('changecontentevent', widget);

        importServicesStore.load(function () {
            if (importServicesStore.getCount() === 0) {
                Ext.suspendLayouts();
                importServicesCombo.hide();
                widget.down('#no-import-services').show();
                Ext.resumeLayouts(true);
            } else {
                widget.down('#upload-file-info-message').show();
                widget.down('#upload-file-form').loadRecord(Ext.create('Fim.model.UploadFile'));
            }
        });
    },

    uploadFile: function () {
        var me = this,
            page = me.getPage(),
            form = page.down('#upload-file-form');

        Ext.suspendLayouts();
        form.down('#upload-file-form-errors').hide();
        form.getForm().clearInvalid();
        Ext.resumeLayouts(true);
        form.updateRecord();
        page.setLoading();

        // setting of hidden fields, needs to request
        form.down('#scheduleid').setValue(form.down('#import-service-combobox').getValue());

        form.getRecord().doSave(me.callbackFn.bind(me), form);
    },

    callbackFn: function (options, success, response) {
        var me = this,
            responseText = Ext.decode(response.responseText, true),
            page = me.getPage(),
            form = page.down('#upload-file-form');

        page.setLoading(false);
        if (responseText.errors) {
            if (page.rendered) {
                if (responseText.errors.length) {
                    Ext.suspendLayouts();
                    form.down('#upload-file-form-errors').show();
                    form.getForm().markInvalid(responseText.errors);
                    Ext.resumeLayouts(true);
                } else {
                    var errorMessage = Ext.widget('messagebox', {
                            closeAction: 'destroy',
                            buttons: [
                                {
                                    text: Uni.I18n.translate('general.close', 'FIM', 'Close'),
                                    ui: 'remove',
                                    handler: function () {
                                        this.up('window').close();
                                    }
                                }
                            ]
                        }),
                        path = form.down('#upload-file-field').getValue();

                    errorMessage.show({
                        ui: 'notification-error',
                        modal: true,
                        icon: Ext.MessageBox.ERROR,
                        title: Uni.I18n.translate('uploadFile.failed', 'FIM', 'Failed to upload {0}', path.substring(path.lastIndexOf('\\') + 1)),
                        msg: responseText.message
                    });
                }
            }
        } else {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('uploadFile.success', 'FIM', 'File uploaded'));
            if (page.rendered) {
                window.location.href = page.returnLink;
            }
        }
    },

    displayInfo: function () {
        var infoDialog = Ext.create('widget.window', {
            title: Uni.I18n.translate('importService.filePatternInfo', 'FIM', 'File pattern info'),
            closable: true,
            overflowY: 'auto',
            modal: true,
            width: 600,
            minWidth: 350,
            height: 365,
            layout: {
                type: 'border',
                padding: 5
            },
            items: [
                {
                    xtype: 'container',
                    html: Uni.I18n.translate('importService.filePatternInfo.title', 'FIM', 'Pattern to filter which files will be imported, based on file name and/or extension.') + '<br><br>' +
                    Uni.I18n.translate('importService.filePatternInfo.example', 'FIM', 'Here are some examples of pattern syntax:') + '<br><ul>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex1', 'FIM', '*.csv - Matches all strings that end in .csv') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex2', 'FIM', '??? - Matches all strings with exactly three letters or digits') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex3', 'FIM', '*[0-9]* - Matches all strings containing a numeric value') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex4', 'FIM', '*.{txt,csv,xlsx} - Matches any string ending with .txt, .csv or.xlsx') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex5', 'FIM', 'a?*.csv - Matches any string beginning with a, followed by at least one letter or digit, and ending with .csv') + '</li>' + '<br>' +
                    '<li>' + '&nbsp' + Uni.I18n.translate('importService.filePatternInfo.ex6', 'FIM', '{foo*,*[0-9]*} - Matches any string beginning with foo or any string containing a numeric value') + '</li>' + '<br></ul>'
                }
            ]
        });
        infoDialog.show();
    }
});
