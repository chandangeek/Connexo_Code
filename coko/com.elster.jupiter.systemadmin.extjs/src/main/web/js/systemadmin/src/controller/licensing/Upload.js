Ext.define('Sam.controller.licensing.Upload', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'licensing.Upload'
    ],

    refs: [
        {
            ref: 'uploadPanel',
            selector: 'upload-licenses-overview'
        }
    ],

    init: function () {
        this.control({
            'upload-licenses-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'upload-licenses-overview filefield': {
                change: this.onChange
            },
            'upload-licenses-overview button[name=upload]': {
                click: this.onSubmit
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('upload-licenses-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this;
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'System administration',
                href: me.getController('Sam.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Licensing',
                href: 'licensing/licenses'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Upload licenses',
                href: 'upload'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onChange: function (fileField, value) {
        var uploadView = this.getUploadPanel(),
            uploadButton = uploadView.down('button[name=upload]'),
            form = uploadView.down('form').getForm();
        if (value !== "" && form.isValid()) {
            uploadButton.enable();
        } else {
            uploadButton.disable();
        }
    },


    onSubmit: function () {
        var self = this,
            uploadPanel = self.getUploadPanel(),
            router = self.getController('Uni.controller.history.Router'),
            form = uploadPanel.down('form').getEl().dom,
            message = 'Licenses successfully uploaded for applications: ';

        if (uploadPanel.down('form').getForm().isValid()) {
            uploadPanel.setLoading();
            Ext.Ajax.request({
                url: '/api/lic/license/upload',
                method: 'POST',
                form: form,
                headers: {'Content-type': 'multipart/form-data'},
                isFormUpload: true,
                callback: function (config, success, response) {
                    var responseObject = JSON.parse(response.responseText);
                    uploadPanel.setLoading(false);
                    if (Ext.isEmpty(responseObject.errors)) {
                        router.getRoute('administration/licensing/licenses').forward();
                        Ext.Array.each(responseObject.data.success, function (item, index) {
                            if (index) {
                                message += ', '
                            }
                            message += item
                        });
                        self.getApplication().fireEvent('acknowledge', message);
                        self.getApplication().fireEvent('upload', responseObject.data.success[0]);
                        Ext.getStore('apps').load();
                    } else {
                        uploadPanel.down('#upload').disable();
                        self.getApplication().getController('Uni.controller.Error').showError('Failed to upload licenses', responseObject.errors[0].msg);
                    }
                }
            });
        }
    }
});


