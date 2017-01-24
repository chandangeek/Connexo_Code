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
        'Mdc.store.Intervals',
        'Mdc.store.Clipboard'
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
        {ref: 'setupPage', selector: 'loadProfileTypeSetup'},
        {ref: 'addRegisterTypesGrid', selector: 'load-profile-type-add-register-types-grid'},
        {ref: 'registerTypesGrid', selector: '#register-types-grid'}
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
        window.location.href = '#/administration/loadprofiletypes/' + encodeURIComponent(lastSelected.getData().id) + '/edit';
    },

    showConfirmationPanel: function () {
        var me = this,
            grid = me.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileTypes.confirmWindow.removeMsg', 'MDC', 'This load profile type will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?",[lastSelected.get('name')]),
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
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('loadProfileTypes.removeSuccessMsg', 'MDC', 'Load profile type removed'));
                },
                callback: function () {
                    widget.setLoading(false);
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
                    text: Uni.I18n.translate('general.retry','MDC','Retry'),
                    action: retryAction,
                    ui: 'remove'
                },
                {
                    text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
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
                html: Ext.String.htmlEncode(loadTypeCount + loadTypesWord)
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
                        actionHref: '#/administration/loadprofiletypes/add'
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
        previewPanel.setTitle(Ext.String.htmlEncode(recordData.name));
        previewPanel.updateRegisterTypes(record);
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

    showEdit: function (loadProfileTypeId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            returnLink = router.getRoute('administration/loadprofiletypes').buildUrl(),
            currentRoute = router.currentRoute.replace('/addregistertypes', ''),
            addRegisterTypesLink = router.getRoute(currentRoute + '/addregistertypes').buildUrl(),
            intervalsStore = me.getStore('Mdc.store.Intervals'),
            editPage = me.getEditPage(),
            widget,
            form;

        if (editPage) {
            if (!loadProfileTypeId) {
                Ext.suspendLayouts();
                editPage.setTitle(Uni.I18n.translate('loadProfileTypes.add', 'MDC', 'Add load profile type'));
                editPage.getLayout().setActiveItem(0);
                editPage.down('#load-profile-type-edit-form').showGridOrMessage();
                Ext.resumeLayouts(true);
                return;
            }
        }

        widget = Ext.widget('load-profile-type-edit', {
            currentRoute: router.getRoute(currentRoute).buildUrl()
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        form = widget.down('#load-profile-type-edit-form');
        form.down('combobox[name=timeDuration]').bindStore(intervalsStore);
        intervalsStore.load(function() {
            if (loadProfileTypeId) {
                widget.setLoading(true);
                me.getModel('Mdc.model.LoadProfileType').load(loadProfileTypeId, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadProfileType', record);

                        if (intervalsStore.getCount()) {
                            me.loadRecordOrClipboard(record)
                        } else {
                            intervalsStore.on('load', function () {
                                me.loadRecordOrClipboard(record)
                            }, me, {single: true});
                        }

                        if (record.get('isLinkedToActiveDeviceConf')) {
                            form.down('[name=timeDuration]').disable();
                            form.down('[name=obisCode]').disable();
                            form.down('#register-types-fieldcontainer').disable();
                            form.down('#register-types-grid').disable();
                        }
                    },
                    callback: function (record) {
                        if (router.currentRoute !== 'administration/loadprofiletypes/edit/addregistertypes') {
                            me.getEditPage().setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", record.get('name')));
                        }
                        widget.setLoading(false);
                        form.setEdit(true, returnLink, addRegisterTypesLink);
                        form.showGridOrMessage();
                    }
                });
            } else {
                me.getEditPage().setTitle(Uni.I18n.translate('loadProfileTypes.add', 'MDC', 'Add load profile type'));
                form.setEdit(false, returnLink, addRegisterTypesLink);
                form.loadRecord(Ext.create('Mdc.model.LoadProfileType'));
                var storeIndex = intervalsStore.findExact("asSeconds", 900);
                var toSelect = storeIndex!=-1 ? intervalsStore.getAt(storeIndex) : 0;
                form.down('[name=timeDuration]').select(toSelect);
                form.showGridOrMessage();
            }
        });
    },

    loadRecordOrClipboard: function(record) {
        var me = this,
            clipBoardRecord = me.getStore('Mdc.store.Clipboard').get('model');

        if(clipBoardRecord && (!clipBoardRecord.get('id') || clipBoardRecord.get('id') === record.get('id'))) {
                record = me.getStore('Mdc.store.Clipboard').get('model');
        } else {
            me.getRegisterTypesGrid().getStore().loadData(record.get('registerTypes'), false);
            me.getStore('Mdc.store.Clipboard').removeAll(true);
        }
        me.getEditForm().loadRecord(record);
    },

    storeCurrentValues: function () {
        var clipboard = this.getStore('Mdc.store.Clipboard');
        this.getEditForm().updateRecord();
        clipboard.set('model', this.getEditForm().getRecord());
    },

    showRegisterTypesAddView: function (id) {
        var me = this,
            store = Ext.getStore('Mdc.store.RegisterTypesToAdd'),
            editPage = me.getEditPage();

        if (!editPage) {
            me.showEdit(id);
            editPage = me.getEditPage();
        }
        if (editPage && id != null) {
            var loadProfileModel = me.getModel('Mdc.model.LoadProfileType');
            store.getProxy().url = loadProfileModel.getProxy().url + '/' + id + '/measurementtypes';
        } else {
            store.getProxy().url = store.getProxy().baseUrl;
        }
        me.storeCurrentValues();

        Ext.suspendLayouts();
        editPage.getLayout().setActiveItem(1);
        editPage.setTitle(Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddRegisterTypesView.title', 'MDC', 'Add register types'));
        Ext.resumeLayouts(true);

        var registerTypesStore = editPage.down('#register-types-grid').getStore();
        if (id) {
            if (editPage.down('#load-profile-type-edit-form').getRecord()) {
                me.loadRegisterTypesToAddStore(registerTypesStore.getRange());
            } else {
                registerTypesStore.on('refresh', function () {
                    me.loadRegisterTypesToAddStore(registerTypesStore.getRange());
                }, me, {single: true});
            }
        } else {
            me.loadRegisterTypesToAddStore(registerTypesStore.getRange());
        }
    },

    loadRegisterTypesToAddStore: function (assignedRegisterTypes) {
        var me = this,
            assignedRegisterTypesIds = [],
            grid = me.getAddRegisterTypesGrid(),
            store = Ext.getStore('Mdc.store.RegisterTypesToAdd');

        store.data.clear();
        store.clearFilter(true);
        grid.getSelectionModel().deselectAll(true);
        Ext.each(assignedRegisterTypes, function (item) {
            assignedRegisterTypesIds.push(item.getId());
        });

        store.on('load', function () {
            if (assignedRegisterTypes && assignedRegisterTypes.length && me.getEditPage()) {
                me.getEditPage().down('radiogroup').items.items[1].setValue(true);
                grid.setGridVisible(true);
            }
        }, me, {single: true});

        (assignedRegisterTypes && assignedRegisterTypes.length) ? store.filter('ids', assignedRegisterTypesIds) : store.loadPage(1);
    },

    onAllRegisterTypesAdd: function () {
        this.addRegisterTypes([], true);
    },

    onSelectedRegisterTypesAdd: function () {
        var me = this, selection = me.getAddRegisterTypesGrid().getSelectionModel().getSelection();
        me.addRegisterTypes(selection, false);
    },

    addRegisterTypes: function (selection, all) {
        var page = this.getEditPage(),
            registerTypesGrid = page.down('#register-types-grid'),
            registerTypesStore = registerTypesGrid.getStore(),
            router = this.getController('Uni.controller.history.Router');

        router.getRoute(router.currentRoute.replace('/addregistertypes', '')).forward();

        if (all) {
            registerTypesStore.removeAll();
        } else {
            registerTypesStore.add(selection);
        }
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
            all = form.down('#all-register-types-field').getValue(),
            backUrl = router.getRoute('administration/loadprofiletypes').buildUrl();

        formErrorsPanel.hide();
        basicForm.clearInvalid();
        form.down('#register-types-fieldcontainer').clearInvalid();
        if (!form.isValid()){
            formErrorsPanel.show();
            return;
        }
        form.updateRecord(model);

        if (model.getId()) {
            widget.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
        } else {
            widget.setLoading(Uni.I18n.translate('general.adding', 'MDC', 'Adding...'));
        }

        proxy.setExtraParam('all', all);

        model.save({
            backUrl: backUrl,
            success: function (record, operation) {
                var messageText;

                switch (operation.action) {
                    case 'create':
                        messageText = Uni.I18n.translate('loadProfileTypes.loadProfileTypeAdded', 'MDC', 'Load profile type added');
                        break;
                    case 'update':
                        messageText = Uni.I18n.translate('loadProfileTypes.loadProfileTypeUpdated', 'MDC', 'Load profile type saved');
                        break;
                }
                me.getApplication().fireEvent('acknowledge', messageText);
                window.location.href = backUrl;
            },
            failure: function (record, operation) {
                if (operation.response.status === 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        Ext.Array.each(json.errors, function (item) {
                            if (item.id.indexOf("interval") !== -1) {
                                me.getEditPage().down('#timeDuration').setActiveError(item.msg);
                            }
                        });
                        basicForm.markInvalid(json.errors);
                        me.registerTypesIsValid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    },

    registerTypesIsValid: function (errors) {
        var me = this,
            registerTypesError = Ext.Array.findBy(errors, function (error) {
                return error.id === 'registerTypes';
            }, me),
            form = me.getEditForm();

        if (form && registerTypesError) {
            form.down('#register-types-fieldcontainer').markInvalid(registerTypesError.msg);
        }
    }
});