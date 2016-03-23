Ext.define('Scs.controller.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Scs.view.Setup',
        'Scs.view.Landing',
        'Scs.view.SetupOverview',
        'Scs.view.ServiceCallPreviewContainer',
        'Scs.view.PreviewForm',
        'Uni.view.window.Confirmation'
    ],
    stores: [
        'Scs.store.ServiceCalls',
        'Scs.store.Logs',
        'Scs.store.ServiceCallTypes',
        'Scs.store.States'
    ],
    models: [
        'Scs.model.ServiceCall'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'service-call-preview-container'
        },
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
        },
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
        },
        {
            ref: 'overviewTabPanel',
            selector: '#service-call-overview-tab'
        },
        {
            ref: 'landingPage',
            selector: 'scs-landing-page'
        },
        {
            ref: 'serviceCallGrid',
            selector: 'servicecalls-grid'
        },
        {
            ref: 'previewActionButton',
            selector: '#previewMenuButton'
        }
    ],

    init: function () {
        this.control({
            'service-call-preview-container servicecalls-grid': {
                select: this.showPreview
            },
            'scs-action-menu': {
                click: this.chooseAction
            },
            'servicecalls-grid': {
                serviceCallLinkClicked: this.clickedColumn
            },
            'servicecalls-setup-overview #service-call-overview-tab': {
                tabchange: this.onTabChange
            }
        });
    },

    showServiceCalls: function () {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls'),
            view;

        store.setProxy({
            type: 'rest',
            url: '/api/scs/servicecalls',
            timeout: 120000,
            reader: {
                type: 'json',
                root: 'serviceCalls'
            }
        });
        view = Ext.widget('servicecalls-setup', {
            router: me.getController('Uni.controller.history.Router'),
            store: store
        });

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showServiceCallSpecifications: function () {
        if(!Uni.util.History.isSuspended()) {
            this.showServiceCallOverview(arguments, 'specs');
        } else {
            this.setBreadcrumb(this.getOverviewTabPanel().breadcrumbs, true);
        }
    },

    showServiceCallGrid: function() {
        if(!Uni.util.History.isSuspended()) {
            this.showServiceCallOverview(arguments, 'grid');
        } else {
            this.setBreadcrumb(this.getOverviewTabPanel().breadcrumbs, false);
        }
    },

    showServiceCallOverview: function (arguments, tab) {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls'),
            logStore = Ext.getStore('Scs.store.Logs'),
            view,
            servicecallId = arguments[arguments.length - 1];
        if (servicecallId) {
            store.setProxy({
                type: 'rest',
                url: '/api/scs/servicecalls/' + servicecallId + '/children',
                timeout: 120000,
                reader: {
                    type: 'json',
                    root: 'serviceCalls'
                }
            });
            me.getModel('Scs.model.ServiceCall').load(servicecallId, {
                success: function (record) {
                    logStore.getProxy().setUrl(servicecallId);
                    //logStore.load({
                    //
                    //})
                    var parents = record.get('parents');
                    parents.push({id:record.get('id'),name:record.get('name')});

                    if (record.get('numberOfChildren') > 0) {
                        view = Ext.widget('servicecalls-setup-overview', {
                            router: me.getController('Uni.controller.history.Router'),
                            serviceCallId: record.get('name'),
                            serviceCallParam: servicecallId,
                            store: store,
                            tab: tab,
                            breadcrumbs: parents,
                            record: record
                        });
                        me.setBreadcrumb(parents, tab === 'specs');
                        view.down('scs-landing-page').updateLandingPage(record);
                            Uni.util.History.setSuspended(false);
                    } else {
                        view = Ext.widget('scs-landing-page', {
                            router: me.getController('Uni.controller.history.Router'),
                            serviceCallId: record.get('name'),
                            record: record
                        });
                        view.updateLandingPage(record);
                        me.setBreadcrumb(parents, false);
                    }

                    me.getApplication().fireEvent('changecontentevent', view);

                },
                failure: function (record, operation) {
                    view = Ext.widget('errorNotFound');
                    me.getBreadcrumbs().hide();
                    me.getApplication().fireEvent('changecontentevent', view);
                }
            });

        }
    },

    onTabChange: function (tabPanel, newTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            addDestinationRoute,
            route,
            showSpecs = newTab.itemId === 'specifications-tab';

        route = !showSpecs ? router.getRoute('workspace/servicecalls/overview') : router.getRoute('workspace/servicecalls/overview/specifications');
        Uni.util.History.suspendEventsForNextCall(true);
        route.forward(tabPanel.servicecallParam);
    },

    setBreadcrumb: function (breadcrumbs, showSpecificationBreadcrumb) {
        var me = this,
            trail = this.getBreadcrumbs(),
            root = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Workspace',
                href: me.getController('Scs.controller.history.ServiceCall').tokenizeShowOverview()
            }),
            parent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Service calls',
                href: 'servicecalls'
            }),
            bc;
        root.setChild(parent);
            Ext.each(breadcrumbs, function (item) {
                bc = Ext.create('Uni.model.BreadcrumbItem', {
                    key: item.id,
                    text: item.name,
                    href: '/' + item.id,
                    relative: false
                });
                parent.setChild(bc);
                parent = bc;
            });

        if(showSpecificationBreadcrumb) {
            bc = Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('general.specifications', 'SCS', 'Specifications'),
                href: '/specifications',
                relative: true
            });
            parent.setChild(bc);
        }
        trail.setBreadcrumbItem(root);
        trail.doLayout();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('servicecalls-preview'),
            serviceCallName = record.get('name'),
            previewForm = preview.down('#servicecall-grid-preview-form');


        me.getPreviewActionButton().setDisabled(!record.get('canCancel'));
        me.getPreviewActionButton().down('scs-action-menu').record = record;
        me.getModel('Scs.model.ServiceCall').load(record.get('id'), {
            success: function (record) {
                previewForm.updatePreview(record);
            },
            failure: function() {
            }
        });
        preview.setTitle(serviceCallName);
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'cancel':
                me.cancelServiceCall(menu.record);
        }
    },

    cancelServiceCall: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.yes', 'SCS', 'Yes'),
                cancelText: Uni.I18n.translate('general.no', 'SCS', 'No')
            }),
            store = Ext.getStore('Scs.store.ServiceCalls'),
            serviceCallState = record.get('state');
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('servicecall.remove.msg', 'SCS', 'This service call will be canceled and no longer be running. Do you wish to continue?'),
                title: Uni.I18n.translate('general.cancelX', 'SCS', "Cancel '{0}'?", [record.data.name]),
                fn: function (state) {
                    if (state === 'confirm') {
                        serviceCallState.id = "sclc.default.cancelled";
                        record.set('state', serviceCallState);
                        if(record.get('parents') === '') {
                            record.set('parents', [])
                        }
                        if(record.get('children') === '') {
                            record.set('children', [])
                        }
                        if(record.get('targetObject') === '') {
                            record.set('targetObject', null)
                        }
                        record.save({
                            success: function(newRecord){
                                if(me.getLandingPage()) {
                                    me.getModel('Scs.model.ServiceCall').load(newRecord.get('id'), {
                                        success: function (record) {
                                            me.getLandingPage().updateLandingPage(record);
                                            me.getLandingPage().down('#scAtionButton').disable();
                                        }
                                    });
                                }
                                if(me.getServiceCallGrid()) {
                                    store.load();
                                }
                            }
                        });
                    }
                }
            });
    }
});