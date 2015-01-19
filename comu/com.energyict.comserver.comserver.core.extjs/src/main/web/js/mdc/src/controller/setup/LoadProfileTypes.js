Ext.define('Mdc.controller.setup.LoadProfileTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.loadprofiletype.LoadProfileTypeSetup',
        'setup.loadprofiletype.LoadProfileTypeSideFilter',
        'setup.loadprofiletype.LoadProfileTypeSorting',
        'setup.loadprofiletype.LoadProfileTypeFiltering',
        'setup.loadprofiletype.LoadProfileTypeGrid',
        'setup.loadprofiletype.LoadProfileTypePreview',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeEdit'
    ],

    stores: [
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.RegisterTypesToAdd',
        'Mdc.store.SelectedRegisterTypesForLoadProfileType',
        'Mdc.store.Intervals'
    ],

    models: [
        'Mdc.model.LoadProfileType'
    ],

    refs: [
        {ref: 'loadTypeGrid', selector: 'loadProfileTypeSetup #loadProfileTypeGrid'},
        {ref: 'loadTypePreview', selector: 'loadProfileTypeSetup #loadProfileTypePreview'},
        {ref: 'loadTypeCountContainer', selector: 'loadProfileTypeSetup #loadProfileTypesCountContainer'},
        {ref: 'loadTypeEmptyListContainer', selector: 'loadProfileTypeSetup #loadProfileTypeEmptyListContainer'},
        {ref: 'loadProfileSorting', selector: '#LoadProfileTypeSorting'},
        {ref: 'loadProfileFiltering', selector: '#LoadProfileTypeFiltering'},
        {ref: 'loadProfileDockedItems', selector: '#LoadProfileTypeDockedItems'},
        {ref: 'editPage', selector: 'load-profile-type-edit #load-profile-type-edit'},
        {ref: 'editForm', selector: 'load-profile-type-edit #load-profile-type-edit #load-profile-type-edit-form'},
        {ref: 'setupPage', selector: 'loadProfileTypeSetup'}
    ],

    init: function () {
        this.control({
            'loadProfileTypeSetup loadProfileTypeGrid': {
                select: this.loadGridItemDetail
            },
            'load-profile-type-edit #load-profile-type-add-register-types-grid': {
                allitemsadd: this.onAllRegisterTypesAdd,
                selecteditemsadd: this.onSelectedRegisterTypesAdd
            },
            'load-profile-type-edit #load-profile-type-edit-form #save-load-profile-type-button': {
                click: this.saveLoadProfileType
            }
        });

        this.intervalStore = this.getStore('Intervals');
        this.store = this.getStore('LoadProfileTypes');
        this.registerTypesStore = this.getStore('RegisterTypesToAdd');
        this.selectedRegisterTypesStore = this.getStore('SelectedRegisterTypesForLoadProfileType');
    },

    editRecord: function () {
        var grid = this.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        window.location.href = '#/administration/loadprofiletypes/' + lastSelected.getData().id + '/edit';
    },

    showConfirmationPanel: function () {
        var me = this,
            grid = me.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileTypes.confirmWindow.removeMsg', 'MDC', 'This load profile type will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + lastSelected.get('name') + "'?",
            config: {
                me: me
            },
            fn: me.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            grid = me.getLoadTypeGrid(),
            model = grid.getView().getSelectionModel().getLastSelected(),
            widget = me.getSetupPage();

        if (state === 'confirm') {
            widget.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
            model.destroy({
                callback: function (model, operation, success) {
                    var json;

                    widget.setLoading(false);

                    if (success) {
                        me.handleSuccessRequest(Uni.I18n.translate('loadProfileTypes.removeSuccessMsg', 'MDC', 'Load profile type was removed successfully'));
                        me.store.loadPage(1);
                    } else {
                        json = Ext.decode(operation.response.responseText, true);
                        if (json && json.message) {
                            me.getApplication().getController(
                                'Uni.controller.Error').showError(Uni.I18n.translate('loadProfileTypes.removeErrorMsg', 'MDC', 'Error during removing of load profile'),
                                json.message
                            );
                        }
                    }
                }
            });
        }
    },

    handleSuccessRequest: function (headerText) {
        window.location.href = '#/administration/loadprofiletypes';
        this.getApplication().fireEvent('acknowledge', headerText);
    },

    handleFailureRequest: function (response, headerText, retryAction) {
        Ext.EventManager.stopPropagation('requestexception');
        var errormsgs = response.responseText;
        Ext.widget('messagebox', {
            buttons: [
                {
                    text: 'Retry',
                    action: retryAction,
                    ui: 'remove'
                },
                {
                    text: 'Cancel',
                    action: 'cancel',
                    ui: 'link',
                    href: '#/administration/loadprofiletypes/',
                    handler: function (button, event) {
                        this.up('messagebox').hide();
                    }
                }
            ]
        }).show({
            ui: 'notification-error',
            title: headerText,
            msg: errormsgs,
            icon: Ext.MessageBox.ERROR
        })
    },

    checkLoadProfileTypesCount: function () {
        var grid = this.getLoadTypeGrid();
        if (!Ext.isEmpty(grid)) {
            var numberOfLoadTypesContainer = this.getLoadTypeCountContainer(),
                emptyMessage = this.getLoadTypeEmptyListContainer(),
                filtering = this.getLoadProfileFiltering(),
                sorting = this.getLoadProfileSorting(),
                dockedItems = this.getLoadProfileDockedItems(),
                gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                loadTypeCount = this.store.getCount(),
                loadTypesWord;

            if (loadTypeCount == 1) {
                loadTypesWord = ' load profile type'
            } else {
                loadTypesWord = ' load profile types'
            }
            var widget = Ext.widget('container', {
                html: loadTypeCount + loadTypesWord
            });
            Ext.suspendLayouts();
            numberOfLoadTypesContainer.removeAll(true);
            numberOfLoadTypesContainer.add(widget);
            Ext.resumeLayouts();
            if (loadTypeCount < 1) {
                grid.hide();
                emptyMessage.removeAll();
                filtering.hide();
                sorting.hide();
                dockedItems.hide();
                Ext.ComponentQuery.query('loadProfileTypeSetup menuseparator')[0].hide();
                emptyMessage.add(
                    {
                        xtype: 'loadProfileTypeEmptyList',
                        actionHref: '#/administration/loadprofiletypes/create'
                    }
                );
                this.getLoadTypePreview().hide();
            } else {
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
            }
        }
    },

    loadGridItemDetail: function (selectionModel, record) {
        var previewPanel = this.getLoadTypePreview(),
            form = previewPanel.down('form'),
            recordData = record.getData();

        this.displayedItemId = recordData.id;
        previewPanel.setTitle(recordData.name);
        form.loadRecord(record);
    },

    showLoadProfileTypes: function () {
        var me = this,
            preloader = Ext.create('Ext.container.Container'),
            widget;

        me.getApplication().fireEvent('changecontentevent', preloader);
        preloader.setLoading(true);

        Uni.util.Common.loadNecessaryStores([
            'Mdc.store.Intervals'
        ], function () {
            widget = Ext.widget('loadProfileTypeSetup', {
                config: {
                    gridStore: me.store
                }
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            me.selectedRegisterTypesStore.removeAll();
            me.temporallyFormValues = null;
            me.loadProfileAction = null;
            Ext.Array.each(Ext.ComponentQuery.query('[action=editloadprofiletype]'), function (item) {
                item.clearListeners();
                item.on('click', function () {
                    me.editRecord();
                });
            });
            Ext.Array.each(Ext.ComponentQuery.query('[action=deleteloadprofiletype]'), function (item) {
                item.clearListeners();
                item.on('click', function () {
                    me.showConfirmationPanel();
                });
            });
        }, false);
    },

    showEdit: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            returnLink = router.getRoute('administration/loadprofiletypes').buildUrl(),
            currentRoute = router.currentRoute.replace('/addregistertypes', ''),
            addRegisterTypesLink = router.getRoute(currentRoute + '/addregistertypes').buildUrl(),
            intervalsStore = me.getStore('Mdc.store.Intervals'),
            widget,
            form;

        if (me.getEditPage()) {
            this.getEditPage().getLayout().setActiveItem(0);
            return;
        }

        widget = Ext.widget('load-profile-type-edit', {
            currentRoute: router.getRoute(currentRoute).buildUrl()
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        form = widget.down('#load-profile-type-edit-form');
        form.down('combobox[name=timeDuration]').bindStore(intervalsStore);
        intervalsStore.load();

        if (id) {
            widget.setLoading(true);
            me.getModel('Mdc.model.LoadProfileType').load(id, {
                success: function (record) {
                    me.getApplication().fireEvent('loadProfileType', record);

                    if (intervalsStore.getCount()) {
                        form.loadRecord(record);
                    } else {
                        intervalsStore.on('load', function () {
                            form.loadRecord(record);
                        }, me, {single: true});
                    }

                    if (record.get('isLinkedToActiveDeviceConf')) {
                        form.down('[name=timeDuration]').disable();
                        form.down('[name=obisCode]').disable();
                        form.down('#register-types-fieldcontainer').disable();
                        form.down('#register-types-grid').disable();
                        router.getRoute(currentRoute).forward();
                    }
                },
                callback: function () {
                    form.setTitle(Uni.I18n.translate('loadProfileTypes.LoadProfileTypeEdit.editTitle', 'MDC', 'Edit load profile type'));
                    form.setEdit(true, returnLink, addRegisterTypesLink);
                    widget.setLoading(false);
                }
            });
        } else {
            form.setTitle(Uni.I18n.translate('loadProfileTypes.LoadProfileTypeEdit.addTitle', 'MDC', 'Add load profile type'));
            form.setEdit(false, returnLink, addRegisterTypesLink);
            form.loadRecord(Ext.create('Mdc.model.LoadProfileType'));
        }
    },

    showRegisterTypesAddView: function (id) {
        var me = this;

        if (!me.getEditPage()) {
            me.showEdit(id);
        }

        me.getStore('Mdc.store.RegisterTypesToAdd').load(function () {
            me.getEditPage().down('#load-profile-type-add-register-types-grid').getSelectionModel().deselectAll();
        });
        me.getEditPage().getLayout().setActiveItem(1);
    },

    onAllRegisterTypesAdd: function () {
        this.addRegisterTypes([], true);
    },

    onSelectedRegisterTypesAdd: function (selection) {
        this.addRegisterTypes(selection, false);
    },

    addRegisterTypes: function (selection, all) {
        var page = this.getEditPage(),
            registerTypesGrid = page.down('#register-types-grid'),
            registerTypesStore = registerTypesGrid.getStore(),
            router = this.getController('Uni.controller.history.Router');

        router.getRoute(router.currentRoute.replace('/addregistertypes', '')).forward();

        registerTypesStore.removeAll();
        registerTypesStore.add(selection);
        registerTypesGrid.setVisible(!all);
        page.down('#all-register-types').setVisible(all);
        page.down('#all-register-types-field').setValue(all);
    },

    saveLoadProfileType: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            editPage = me.getEditPage(),
            widget = editPage.up('load-profile-type-edit'),
            form = editPage.down('#load-profile-type-edit-form'),
            basicForm = form.getForm(),
            formErrorsPanel = form.down('uni-form-error-message'),
            model = form.getRecord(),
            proxy = model.getProxy(),
            all = form.down('#all-register-types-field').getValue();

        formErrorsPanel.hide();
        basicForm.clearInvalid();
        form.down('#register-types-fieldcontainer').clearInvalid();
        form.updateRecord(model);

        if (model.getId()) {
            widget.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
        } else {
            widget.setLoading(Uni.I18n.translate('general.adding', 'MDC', 'Adding...'));
        }

        proxy.setExtraParam('all', all);

        model.save({
            callback: function (model, operation, success) {
                var messageText,
                    json;

                widget.setLoading(false);

                if (success) {
                    switch (operation.action) {
                        case 'create':
                            messageText = Uni.I18n.translate('loadProfileTypes.loadProfileTypeAdded', 'MDC', 'Load profile type added');
                            break;
                        case 'update':
                            messageText = Uni.I18n.translate('loadProfileTypes.loadProfileTypeUpdated', 'MDC', 'Load profile type saved');
                            break;
                    }
                    me.getApplication().fireEvent('acknowledge', messageText);
                    router.getRoute('administration/loadprofiletypes').forward();
                } else {
                    json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        basicForm.markInvalid(json.errors);
                        me.registerTypesIsValid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            }
        });
    },

    registerTypesIsValid: function (errors) {
        var me = this,
            registerTypesError = Ext.Array.findBy(errors, function (error) {
                return error.id === 'readingType';
            }, me),
            form = me.getEditForm();

        if (form && registerTypesError) {
            form.down('#register-types-fieldcontainer').markInvalid(registerTypesError.msg);
        }
    }
});