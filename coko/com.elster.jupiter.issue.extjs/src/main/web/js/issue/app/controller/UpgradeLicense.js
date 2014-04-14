Ext.define('Isu.controller.UpgradeLicense', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.Licensing'
    ],

    views: [
        'administration.datacollection.licensing.upgradelicense.Overview'
    ],

    refs: [
        {
            ref: 'upgradePanel',
            selector: 'upgrade-license-overview'
        }
    ],

    init: function () {
        this.control({
            'upgrade-license-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'upgrade-license-overview filefield': {
                change: this.onChange
            },
            'upgrade-license-overview button[name=upgrade]': {
                click: this.onSubmit
            },
            'upgrade-license-overview displayfield[name=appType]': {
                afterrender: this.setApplication
            }
        });
    },

    showOverview: function (id) {
        var widget = Ext.widget('upgrade-license-overview'),
            self = this;
        self.issueId = id;
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setApplication: function(field) {
        var self = this,
            record = this.getStore('Isu.store.Licensing').getById(self.issueId);
        field.setValue(record.data.application);
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
                text: 'Upgrade license',
                href: 'upgradelicense'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onChange: function (fileField, value) {
        var upgradeView = this.getUpgradePanel(),
            upgradeButton = upgradeView.down('button[name=upgrade]'),
            form = upgradeView.down('form').getForm();
        if (value !== "" && form.isValid()) {
            upgradeButton.enable();
        } else {
            upgradeButton.disable();
        }
    },

    onSubmit: function () {
        var self = this,
            form = self.getUpgradePanel().down('form').getForm(),
            header = {
                style: 'msgHeaderStyle'
            };
        if (form.isValid()) {
            form.submit({
                url: '/api/sam/license/upload',
                params: {
                    application: self.issueId
                },
                method: 'POST',
                waitMsg: 'Loading...',
                failure: function (form, action) {
                    if (Ext.isEmpty(action.result.data.failure)) {
                        window.location.href = '#/issue-administration/datacollection/licensing';
                        header.text = 'License successfully upgraded';
                        self.getApplication().fireEvent('isushowmsg', {
                            type: 'notify',
                            msgBody: [header],
                            y: 10,
                            showTime: 5000
                        });
                        self.getApplication().fireEvent('addlicense', action.result.data.success[0]);
                    } else {
                        var msges = [],
                            bodyItem = {};
                        header.text = 'Failed to upgrade license';
                        msges.push(header);
                        bodyItem.text = action.result.data.failure;
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
                                    hnd: function () {
                                        window.location = '#/issue-administration/datacollection/licensing';
                                    }
                                }
                            ]
                        })
                    }
                }
            });
        }
    }
});


