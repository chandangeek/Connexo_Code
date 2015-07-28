Ext.define('Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions', {
    extend: 'Ext.app.Controller',

    views: [
        'Dlc.devicelifecycletransitions.view.Setup',
        'Dlc.devicelifecycletransitions.view.Add'
    ],

    stores: [
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitions',
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionEventTypes',
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionPrivileges',
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionFromState',
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionToState',
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionAutoActions',
        'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionChecks',
        'Dlc.main.store.Clipboard'
    ],

    models: [
        'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition',
        'Dlc.devicelifecycles.model.DeviceLifeCycle',
        'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransitionState'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-life-cycle-transitions-setup'
        },
        {
            ref: 'addPage',
            selector: 'device-life-cycle-transitions-add'
        }
    ],

    transition: null,

    init: function () {
        this.control({
            'device-life-cycle-transitions-setup device-life-cycle-transitions-grid': {
                select: this.showDeviceLifeCycleTransitionPreview
            },
            'device-life-cycle-transitions-add #add-button': {
                click: this.createDeviceLifeCycleTransition
            },
            'transitions-action-menu': {
                click: this.chooseAction
            },
            'device-life-cycle-transitions-add #no-states button': {
                click: this.redirectToAddStatePage
            },
            'device-life-cycle-transitions-add #transition-triggered-by-combo': {
                change: this.proposeTransitionName
            }
        });
    },

    showDeviceLifeCycleTransitions: function (deviceLifeCycleId) {
        var me = this,
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitions'),
            view;

        store.getProxy().setUrl(router.arguments);
        view = Ext.widget('device-life-cycle-transitions-setup', {
            router: router
        });
        me.getApplication().fireEvent('changecontentevent', view);

        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
                view.down('#device-life-cycle-link').setText(deviceLifeCycleRecord.get('name'));
            }
        });
    },

    showDeviceLifeCycleTransitionPreview: function (selectionModel, record, index) {
        var me = this,
            page = me.getPage(),
            preview = page.down('device-life-cycle-transitions-preview'),
            previewForm = page.down('device-life-cycle-transitions-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('transitions-action-menu').record = record;
        Ext.resumeLayouts(true);
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        switch (item.action) {
            case 'editTransition':
                route = 'administration/devicelifecycles/devicelifecycle/transitions/edit';
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
            router = me.getController('Uni.controller.history.Router');

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceLifeCycleTransitions.remove.msg', 'DLC', 'This transition will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'DLC', 'Remove') + ' \'' + record.get('name') + '\'?',
            fn: function (state) {
                if (state === 'confirm') {
                    record.getProxy().setUrl(router.arguments);
                    me.getPage().setLoading(Uni.I18n.translate('general.removing', 'DLC', 'Removing...'));
                    record.destroy({
                        success: function () {
                            me.getPage().setLoading(false);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycleTransitions.remove.success.msg', 'DLC', 'Transition removed'));
                            router.getRoute().forward();
                        }
                    });
                }
            }
        });
    },

    showAddDeviceLifeCycleTransition: function (deviceLifeCycleId, transitionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            isEdit = transitionId ? true : false,
            view = Ext.widget('device-life-cycle-transitions-add', {
                router: router,
                edit: isEdit
            }),
            form = view.down('#device-life-cycle-transitions-add-form'),
            fromCombo = view.down('#transition-from-combo'),
            toCombo = view.down('#transition-to-combo'),
            triggeredByCombo = view.down('#transition-triggered-by-combo'),
            privilegesCheckboxgroup = view.down('#privileges-checkboxgroup'),
            transitionModel = me.getModel('Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition'),
            statesStore = me.getStore('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionFromState'),
            privilegesStore = me.getStore('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionPrivileges'),
            deviceLifeCycleModel = me.getModel('Dlc.devicelifecycles.model.DeviceLifeCycle'),
            autoActionsContainer = view.down('#autoActionsContainer'),
            pretranstionChecksContainer = view.down('#pretansitionsContainer');

        Ext.util.History.on('change', me.checkRoute, me);
        me.transition = null;
        me.fromEditTransitionPage = false;
        deviceLifeCycleModel.load(deviceLifeCycleId, {
            success: function (deviceLifeCycleRecord) {
                me.getApplication().fireEvent('devicelifecycleload', deviceLifeCycleRecord);
            }
        });
        statesStore.getProxy().setUrl(router.arguments);
        statesStore.load(function () {
            if (this.getCount() == 0) {
                fromCombo.hide();
                toCombo.hide();
                Ext.Array.each(Ext.ComponentQuery.query('#no-states'), function (item) {
                    item.show();
                });
            }
            privilegesStore.load(function (privileges) {
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
                triggeredByCombo.getStore().load(function () {
                    if (isEdit) {
                        me.fromEditTransitionPage = true;
                        transitionModel.getProxy().setUrl(router.arguments);
                        transitionModel.load(transitionId, {
                            success: function (transition) {
                                var fromValue = statesStore.getById(transition.get('fromState').id),
                                    toValue = statesStore.getById(transition.get('toState').id),
                                    privilegesArray = [];

                                me.transition = transition;
                                me.getApplication().fireEvent('deviceLifeCycleTransitionEdit', transition);
                                form.setTitle(Uni.I18n.translatePlural('deviceLifeCycleTransitions.edit.title', transition.get('name'), 'DLC', 'Edit \'{0}\''));
                                if (me.getStore('Dlc.main.store.Clipboard').get('addTransitionValues')) {
                                    me.setFormValues();
                                } else {
                                    view.down('#transition-name').setValue(transition.get('name'));
                                    triggeredByCombo.setValue(transition.get('triggeredBy').symbol);
                                    fromCombo.setValue(fromValue);
                                    toCombo.setValue(toValue);
                                    fromCombo.fireEvent('select', fromCombo, fromValue);
                                    toCombo.fireEvent('select', toCombo, toValue);
                                    autoActionsContainer.on('rendered',function (container) {
                                        container.down('#actions-property-form').setValue(transition.get('microActions'))
                                    }, me, {single: true});
                                    pretranstionChecksContainer.on('rendered',function (container){
                                        container.down('#checks-property-form').setValue(transition.get('microChecks'))
                                    }, me, {single: true});
                                    Ext.Array.each(transition.get('privileges'), function (transitionPrivilege) {
                                        privilegesArray.push(transitionPrivilege.privilege);
                                    });
                                    privilegesCheckboxgroup.setValue({
                                        privilege: privilegesArray
                                    });
                                }
                                view.setLoading(false);
                            }
                        });
                    } else if (me.getStore('Dlc.main.store.Clipboard').get('addTransitionValues')) {
                        me.setFormValues();
                    }
                    me.getApplication().fireEvent('changecontentevent', view);
                    isEdit && view.setLoading();
                });
            });
        });
    },

    createDeviceLifeCycleTransition: function (button) {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#device-life-cycle-transitions-add-form'),
            formErrorsPanel = form.down('#form-errors'),
            router = me.getController('Uni.controller.history.Router'),
            privilegesCheckboxgroupValue = page.down('#privileges-checkboxgroup').getValue(),
            autoActionComponent = page.down('#actions-property-form'),
            pretransitionCheckComponent = page.down('#checks-property-form'),
            record = me.transition || Ext.create('Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransition'),
            privilegesArray = [],
            microActions = [],
            microChecks = [];

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.setText(formErrorsPanel.defaultText);
            form.getForm().clearInvalid();
            formErrorsPanel.hide();
        }

        Ext.Array.each(privilegesCheckboxgroupValue.privilege, function (transitionPrivilege) {
            privilegesArray.push({ privilege: transitionPrivilege});
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
        record.set('fromState', { id: form.down('#transition-from-combo').getValue() });
        record.set('toState', { id: form.down('#transition-to-combo').getValue() });
        record.set('triggeredBy', { symbol: form.down('#transition-triggered-by-combo').getValue() });
        record.set('privileges', privilegesArray);
        record.set('microActions', microActions);
        record.set('microChecks', microChecks);
        record.endEdit();
        page.setLoading();
        record.save({
            success: function () {
                router.getRoute('administration/devicelifecycles/devicelifecycle/transitions').forward();
                if (button.action === 'editTransition') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycleTransitions.edit.successMsg', 'DLC', 'Device life cycle transition saved'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceLifeCycleTransitions.add.successMsg', 'DLC', 'Device life cycle transition added'));
                }
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                page.setLoading(false);
                if (json && json.errors) {
                    form.getForm().markInvalid(json.errors);
                    formErrorsPanel.show();
                    Ext.Array.each(json.errors, function (item) {
                        if (item.id.indexOf('duplicate') !== -1) {
                            formErrorsPanel.setText(item.msg);
                            formErrorsPanel.doLayout();
                        }
                    });
                }
            }
        })
    },

    redirectToAddStatePage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            additionalParams = {};

        me.saveFormValues();
        additionalParams.fromEditTransitionPage = me.fromEditTransitionPage;
        router.getRoute('administration/devicelifecycles/devicelifecycle/states/add').forward(null, additionalParams);
    },

    saveFormValues: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#device-life-cycle-transitions-add-form');

        me.getStore('Dlc.main.store.Clipboard').set('addTransitionValues', form.getValues());
    },

    setFormValues: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#device-life-cycle-transitions-add-form'),
            fromCombo = page.down('#transition-from-combo'),
            toCombo = page.down('#transition-to-combo'),
            statesStore = me.getStore('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionFromState'),
            obj = me.getStore('Dlc.main.store.Clipboard').get('addTransitionValues'),
            fromValue,
            toValue;

        page.down('#privileges-checkboxgroup').setValue({
            privilege: obj.privilege
        });
        page.down('#transition-triggered-by-combo').setValue(obj.triggeredBy);
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
            addStateRegexp = /administration\/devicelifecycles\/(.*)\/states\/add/;

        Ext.util.History.un('change', me.checkRoute, me);

        if (token.search(addStateRegexp) == -1) {
            me.getStore('Dlc.main.store.Clipboard').clear('addTransitionValues');
        }
    },

    proposeTransitionName: function (combo) {
        var me = this,
            page = me.getAddPage(),
            numberOfEventTypes = combo.getStore().getCount(),
            found = false,
            nameField = page.down('#transition-name'),
            oldNameValue = nameField.getValue();

        if (!page.edit) {
            if (oldNameValue == '') {
                nameField.setValue(combo.getRawValue());
            } else {
                var i = 0;
                while ((i < numberOfEventTypes) && (!found)) {
                    if (oldNameValue == combo.getStore().data.items[i].data.name) {
                        found = true;
                    }
                    i++;
                }
                if (found) {
                    nameField.setValue(combo.getRawValue());
                }
            }
        }
    }
});