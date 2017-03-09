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
    }
});
