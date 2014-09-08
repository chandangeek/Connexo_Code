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
            form = uploadPanel.down('form').getForm(),
            header = {
                style: 'msgHeaderStyle'
            },
            msges = [];
        if (form.isValid()) {
            form.submit({
                url: '/api/lic/license/upload',
                method: 'POST',
                waitMsg: 'Loading...',
                failure: function (form, action) {
                    if (Ext.isEmpty(action.result.data.failure)) {
                        window.location.href = '#/administration/licensing/licenses';
                        header.text = 'Licenses successfully uploaded for applications:';
                        msges.push(header);
                        Ext.Array.each(action.result.data.success, function(item) {
                            var bodyItem = {};
                            bodyItem.style = 'msgItemStyle';
                            bodyItem.text = item;
                            msges.push(bodyItem);
                        });
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'notify',
                            msgBody: msges,
                            y: 10,
                            showTime: 5000
                        });
                        self.getApplication().fireEvent('upload', action.result.data.success[0]);
                    } else {
                        var bodyItem = {};
                        bodyItem.style = 'msgItemStyle';
                        header.text = 'Failed to upload licenses';
                        msges.push(header);
                        bodyItem.text = action.result.data.failure;
                        msges.push(bodyItem);
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'error',
                            msgBody: msges,
                            y: 10,
                            closeBtn: true,
                            btns: [
                                {
                                    text: 'Cancel',
                                    cls: 'isu-btn-link',
                                    hnd: function () {
                                        window.location = '#/administration/licensing/licenses';
                                    }
                                }
                            ],
                            listeners: {
                                close: {
                                    fn: function () {
                                        uploadPanel.enable();
                                    }
                                }
                            }
                        })
                        uploadPanel.disable();
                    }
                }
            });
        }
    }
});


