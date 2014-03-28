Ext.define('Isu.controller.AddLicense', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'administration.datacollection.licensing.addlicense.Overview',
        'administration.datacollection.licensing.Overview',
        'administration.datacollection.licensing.Details'
    ],

    refs: [
        {
            ref: 'addPanel',
            selector: 'add-license-overview'
        },
        {
            ref: 'listPanel',
            selector: 'licensing-list'
        },
        {
            ref: 'detailsPanel',
            selector: 'licensing-details'
        }
    ],

    init: function () {
        this.control({
            'add-license-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'add-license-overview filefield': {
                change: this.onChange
            },
            'add-license-overview button[name=add]': {
                click: this.onSubmit
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('add-license-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this;
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Licensing',
                href: 'licensing'
            }),
            breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Add license',
                href: 'addlicense'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onChange: function (fileField, value) {
        var addView = this.getAddPanel(),
            addButton = addView.down('button[name=add]');
        if (value !== "") {
            addButton.enable();
        } else {
            addButton.disable();
        }
    },

    onSubmit: function () {
        var self = this,
            form = self.getAddPanel().down('form').getForm(),
            listPanel = self.getListPanel(),
            detailsPanel = self.getDetailsPanel();
        if (form.isValid()) {
            form.submit({
                url: '/api/sam/license/upload',
                method: 'POST',
                waitMsg: 'Loading...',
                success: function (response) {
                    var result = Ext.decode(response.responseText).data;
                    var header = {
                        style: 'msgHeaderStyle'
                    };
                    if (Ext.isEmpty(result.failure)) {
                        window.location.href = '#/issue-administration/datacollection/licensing';
                        header.text = 'License successfully uploaded';
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'notify',
                            msgBody: [header],
                            y: 10,
                            showTime: 5000
                        });
                    } else {
                        var msges = [],
                            bodyItem = {};
                        header.text = 'Failed to add license';
                        msges.push(header);
                        bodyItem.text = result.failure[0].message;
                        bodyItem.style = 'msgItemStyle';
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
                                    hrefTarget: '',
                                    href: '#/issue-administration/datacollection/licensing'
                                }
                            ]
                        });
                    }
                }
            });
        }
    }
});

