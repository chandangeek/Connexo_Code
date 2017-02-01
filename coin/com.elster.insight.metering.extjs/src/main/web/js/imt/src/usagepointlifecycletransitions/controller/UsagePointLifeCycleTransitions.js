/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycletransitions.controller.UsagePointLifeCycleTransitions', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.usagepointlifecycletransitions.view.Setup',
        'Imt.usagepointlifecycletransitions.view.Add'
    ],

    stores: [
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitions',
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionPrivileges',
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionFromState',
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionToState',
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionAutoActions',
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionChecks',
        'Imt.store.Clipboard'
    ],

    models: [
        'Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransition',
        'Imt.usagepointlifecycle.model.UsagePointLifeCycle',
        'Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransitionState'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'usagepoint-life-cycle-transitions-setup'
        },
        {
            ref: 'addPage',
            selector: 'usagepoint-life-cycle-transitions-add'
        }
    ],

    transition: null,

    init: function () {
        this.control({
            'usagepoint-life-cycle-transitions-setup usagepoint-life-cycle-transitions-grid': {
                select: this.showUsagePointLifeCycleTransitionPreview
            },
            'usagepoint-life-cycle-transitions-add #add-button': {
                click: this.createUsagePointLifeCycleTransition
            },
            'transitions-action-menu': {
                click: this.chooseAction
            },
            'usagepoint-life-cycle-transitions-add #no-states button': {
                click: this.redirectToAddStatePage
            }
        });
    },

    showUsagePointLifeCycleTransitions: function (usagePointLifeCycleId) {
        var me = this,
            usagePointLifeCycleModel = me.getModel('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitions'),
            view;

        store.getProxy().setUrl(router.arguments);
        view = Ext.widget('usagepoint-life-cycle-transitions-setup', {
            router: router
        });
        me.getApplication().fireEvent('changecontentevent', view);

        usagePointLifeCycleModel.load(usagePointLifeCycleId, {
            success: function (usagePointLifeCycleRecord) {
                me.getApplication().fireEvent('usagepointlifecycleload', usagePointLifeCycleRecord);
                view.down('#usagepoint-life-cycle-link').setText(usagePointLifeCycleRecord.get('name'));
            }
        });
    },

    showUsagePointLifeCycleTransitionPreview: function (selectionModel, record, index) {
        var me = this,
            page = me.getPage(),
            preview = page.down('usagepoint-life-cycle-transitions-preview'),
            previewForm = page.down('usagepoint-life-cycle-transitions-preview-form'),
            actionMenu = preview.down('transitions-action-menu');

        Ext.suspendLayouts();
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.loadRecord(record);
        if (actionMenu) {
            actionMenu.record = record;
        }
        Ext.resumeLayouts(true);
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        switch (item.action) {
            case 'editTransition':
                route = 'administration/usagepointlifecycles/usagepointlifecycle/transitions/edit';
                break;
            case 'removeTransition':
                me.removeTransition(menu.record);
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward({transitionId: menu.record.getId()});
    },

    removeTransition: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            page = me.getPage();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('usagePointLifeCycleTransitions.remove.msg', 'IMT', 'This transition will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'?", [record.get('name')]),
            fn: function (state) {
                if (state === 'confirm') {
                    record.getProxy().setUrl(router.arguments);
                    page.setLoading(Uni.I18n.translate('general.removing', 'IMT', 'Removing...'));
                    record.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycleTransitions.remove.success.msg', 'IMT', 'Transition removed'));
                            router.getRoute().forward();
                        },
                        callback: function () {
                            page.setLoading(false);
                        }
                    });
                }
            }
        });
    },

    showAddUsagePointLifeCycleTransition: function (usagePointLifeCycleId, transitionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            isEdit = transitionId ? true : false,
            view = Ext.widget('usagepoint-life-cycle-transitions-add', {
                router: router,
                edit: isEdit
            }),
            form = view.down('#usagepoint-life-cycle-transitions-add-form'),
            fromCombo = view.down('#transition-from-combo'),
            toCombo = view.down('#transition-to-combo'),
            privilegesCheckboxgroup = view.down('#privileges-checkboxgroup'),
            transitionModel = me.getModel('Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransition'),
            statesStore = me.getStore('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionFromState'),
            privilegesStore = me.getStore('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionPrivileges'),
            usagePointLifeCycleModel = me.getModel('Imt.usagepointlifecycle.model.UsagePointLifeCycle'),
            autoActionsContainer = view.down('#autoActionsContainer'),
            pretransitionChecksContainer = view.down('#pretransitionsContainer');

        Ext.util.History.on('change', me.checkRoute, me);
        me.transition = null;
        me.fromEditTransitionPage = false;
        usagePointLifeCycleModel.load(usagePointLifeCycleId, {
            success: function (usagePointLifeCycleRecord) {
                me.getApplication().fireEvent('usagepointlifecycleload', usagePointLifeCycleRecord);
            }
        });
        statesStore.getProxy().setUrl(router.arguments);
        statesStore.load(function () {
            if (this.getCount() == 0) {
                Ext.suspendLayouts();
                fromCombo.hide();
                toCombo.hide();
                Ext.Array.each(Ext.ComponentQuery.query('#no-states'), function (item) {
                    item.show();
                });
                Ext.resumeLayouts(true);
            }
            privilegesStore.load(function (privileges) {
                Ext.suspendLayouts();
                Ext.Array.each(privileges, function (privilege) {
                    var checkbox = Ext.widget('checkbox', {
                        name: 'privilege',
                        boxLabel: privilege.data.name,
                        inputValue: privilege.data.privilege,
                        checked: true
                    });
                    privilegesCheckboxgroup.add(checkbox);
                });
                !isEdit && privilegesCheckboxgroup.down('[inputValue=FOUR]').setValue(false);
                Ext.resumeLayouts(true);
                if (isEdit) {
                    me.fromEditTransitionPage = true;
                    transitionModel.getProxy().setUrl(router.arguments);
                    transitionModel.load(transitionId, {
                        success: function (transition) {
                            var fromValue = statesStore.getById(transition.get('fromState').id),
                                toValue = statesStore.getById(transition.get('toState').id),
                                privilegesArray = [];

                            me.transition = transition;
                            Ext.suspendLayouts();
                            me.getApplication().fireEvent('usagePointLifeCycleTransitionEdit', transition);
                            form.setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [transition.get('name')]));
                            if (me.getStore('Imt.store.Clipboard').get('addTransitionValues')) {
                                me.setFormValues();
                            } else {
                                view.down('#transition-name').setValue(transition.get('name'));
                                fromCombo.setValue(fromValue);
                                toCombo.setValue(toValue);
                                autoActionsContainer.on('rendered', function (container) {
                                    container.down('#actions-property-form').setValue(transition.get('microActions'))
                                }, me, {single: true});
                                pretransitionChecksContainer.on('rendered', function (container) {
                                    container.down('#checks-property-form').setValue(transition.get('microChecks'))
                                }, me, {single: true});
                                view.fillActionsAndChecks();
                                Ext.Array.each(transition.get('privileges'), function (transitionPrivilege) {
                                    privilegesArray.push(transitionPrivilege.privilege);
                                });
                                privilegesCheckboxgroup.setValue({
                                    privilege: privilegesArray
                                });
                            }
                            Ext.resumeLayouts(true);
                            view.setLoading(false);
                        }
                    });
                } else if (me.getStore('Imt.store.Clipboard').get('addTransitionValues')) {
                    me.setFormValues();
                }
                me.getApplication().fireEvent('changecontentevent', view);
                isEdit && view.setLoading();
            });
        });
    },

    createUsagePointLifeCycleTransition: function (button) {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#usagepoint-life-cycle-transitions-add-form'),
            formErrorsPanel = form.down('#form-errors'),
            router = me.getController('Uni.controller.history.Router'),
            privilegesCheckboxgroupValue = page.down('#privileges-checkboxgroup').getValue(),
            autoActionComponent = page.down('#actions-property-form'),
            pretransitionCheckComponent = page.down('#checks-property-form'),
            record = me.transition || Ext.create('Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransition'),
            privilegesArray = [],
            microActions = [],
            microChecks = [],
            backUrl = router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions').buildUrl();

        if (!formErrorsPanel.isHidden()) {
            Ext.suspendLayouts();
            formErrorsPanel.setText(formErrorsPanel.defaultText);
            form.getForm().clearInvalid();
            formErrorsPanel.hide();
            Ext.resumeLayouts(true);
        }

        Ext.Array.each(privilegesCheckboxgroupValue.privilege, function (transitionPrivilege) {
            privilegesArray.push({privilege: transitionPrivilege});
        });

        if (!!autoActionComponent) {
            microActions = autoActionComponent.getValue();
        }

        if (!!pretransitionCheckComponent) {
            microChecks = pretransitionCheckComponent.getValue();
        }

        !me.transition && record.getProxy().setUrl(router.arguments);
        record.beginEdit();
        record.set('name', form.down('#transition-name').getValue());
        record.set('fromState', {id: form.down('#transition-from-combo').getValue()});
        record.set('toState', {id: form.down('#transition-to-combo').getValue()});
        record.set('privileges', privilegesArray);
        record.set('microActions', microActions);
        record.set('microChecks', microChecks);
        record.endEdit();
        page.setLoading();
        record.save({
            backUrl: backUrl,
            success: function () {
                window.location.href = backUrl;
                if (button.action === 'editTransition') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycleTransitions.edit.successMessage', 'IMT', 'Transition saved'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePointLifeCycleTransitions.add.successMessage', 'IMT', 'Transition added'));
                }
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);

                Ext.suspendLayouts();
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                    Ext.Array.each(json.errors, function (item) {
                        if (item.id.indexOf('duplicate') !== -1) {
                            formErrorsPanel.setText(item.msg);
                            formErrorsPanel.doLayout();
                            formErrorsPanel.getEl().scrollIntoView(page.getEl()); // make sure the formErrorsPanel is visible even when you scrolled down far away
                        }
                    });
                }
                Ext.resumeLayouts(true);
            },
            callback: function () {
                page.setLoading(false);
            }
        })
    },

    redirectToAddStatePage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            additionalParams = {};

        me.saveFormValues();
        additionalParams.fromEditTransitionPage = me.fromEditTransitionPage;
        router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/states/add').forward(null, additionalParams);
    },

    saveFormValues: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#usagepoint-life-cycle-transitions-add-form');

        me.getStore('Imt.store.Clipboard').set('addTransitionValues', form.getValues());
    },

    setFormValues: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#usagepoint-life-cycle-transitions-add-form'),
            fromCombo = page.down('#transition-from-combo'),
            toCombo = page.down('#transition-to-combo'),
            statesStore = me.getStore('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionFromState'),
            obj = me.getStore('Imt.store.Clipboard').get('addTransitionValues'),
            fromValue,
            toValue;

        page.down('#privileges-checkboxgroup').setValue({
            privilege: obj.privilege
        });
        page.down('#transition-name').setValue(obj.name);
        if (obj.fromState) {
            fromValue = statesStore.getById(obj.fromState);
            fromCombo.setValue(fromValue);
            fromCombo.fireEvent('select', fromCombo, fromValue);
        }
        if (obj.toState) {
            toValue = statesStore.getById(obj.toState);
            toCombo.setValue(toValue);
            toCombo.fireEvent('select', toCombo, toValue);
        }
    },

    checkRoute: function (token) {
        var me = this,
            addStateRegexp = /administration\/usagepointlifecycles\/(.*)\/states\/add/;

        Ext.util.History.un('change', me.checkRoute, me);

        if (token.search(addStateRegexp) == -1) {
            me.getStore('Imt.store.Clipboard').clear('addTransitionValues');
        }
    }
});