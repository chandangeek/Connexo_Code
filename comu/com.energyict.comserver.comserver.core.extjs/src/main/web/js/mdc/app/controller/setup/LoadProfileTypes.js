Ext.define('Mdc.controller.setup.LoadProfileTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.register.ReadingTypeDetails',
        'setup.loadprofiletype.LoadProfileTypeSetup',
        'setup.loadprofiletype.LoadProfileTypeSideFilter',
        'setup.loadprofiletype.LoadProfileTypeSorting',
        'setup.loadprofiletype.LoadProfileTypeFiltering',
        'setup.loadprofiletype.LoadProfileTypeGrid',
        'setup.loadprofiletype.LoadProfileTypePreview',
        'setup.loadprofiletype.LoadProfileTypeForm',
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesView',
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesDockedItems',
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid'
    ],

    stores: [
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.MeasurementTypesToAdd',
        'Mdc.store.SelectedMeasurementTypesForLoadProfileType',
        'Mdc.store.Intervals'
    ],

    refs: [
        {ref: 'loadTypeGrid', selector: 'loadProfileTypeSetup #loadProfileTypeGrid'},
        {ref: 'loadTypePreview', selector: 'loadProfileTypeSetup #loadProfileTypePreview'},
        {ref: 'loadTypeCountContainer', selector: 'loadProfileTypeSetup #loadProfileTypesCountContainer'},
        {ref: 'loadTypeEmptyListContainer', selector: 'loadProfileTypeSetup #loadProfileTypeEmptyListContainer'},
        {ref: 'addMeasurementTypesGrid', selector: '#loadProfileTypeAddMeasurementTypesGrid'},
        {ref: 'addMeasurementTypesView', selector: '#loadProfileTypeAddMeasurementTypesView'},
        {ref: 'addMeasurementTypesCount', selector: '#measurementTypesCountContainer'},
        {ref: 'uncheckMeasurementButton', selector: '#uncheckAllMeasurementTypes'},
        {ref: 'loadTypeForm', selector: '#LoadProfileTypeFormId'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'loadProfileSorting', selector: '#LoadProfileTypeSorting'},
        {ref: 'loadProfileFiltering', selector: '#LoadProfileTypeFiltering'},
        {ref: 'loadProfileDockedItems', selector: '#LoadProfileTypeDockedItems'}

    ],

    init: function () {
        this.control({
            'loadProfileTypeSetup loadProfileTypeGrid': {
                select: this.loadGridItemDetail
            },
            'loadProfileTypeAddMeasurementTypesView': {
                afterrender: this.measurementTypesLoad
            },
            'loadProfileTypeAddMeasurementTypesView radiogroup[name=AllOrSelectedMeasurementTypes]': {
                change: this.selectRadioButton
            },
            'loadProfileTypeAddMeasurementTypesView loadProfileTypeAddMeasurementTypesGrid': {
                selectionchange: this.getCountOfCheckedMeasurementTypes
            },
            'loadProfileTypeAddMeasurementTypesView loadProfileTypeAddMeasurementTypesDockedItems button[action=uncheckallmeasurementtypes]': {
                click: this.unCheckAllMeasurementTypes
            },
            'loadProfileTypeAddMeasurementTypesView button[name=addmeasurementtypestoloadprofiletype]': {
                click: this.addMeasurementTypes
            },
            'loadProfileTypeForm button[name=loadprofiletypeaction]': {
                click: this.onSubmit
            },
            '#LoadProfileTypeFormId #MeasurementTypesGrid actioncolumn': {
                click: this.removeMeasurementType
            },
            '#loadProfileReadingTypeBtn': {
                showReadingTypeInfo: this.showReadingType
            },
            'button[action=loadprofiletypenotificationerrorretry]': {
                click: this.retrySubmit
            }
        });

        this.intervalStore = this.getStore('Intervals');
        this.store = this.getStore('LoadProfileTypes');
        this.measurementTypesStore = this.getStore('MeasurementTypesToAdd');
        this.selectedMeasurementTypesStore = this.getStore('SelectedMeasurementTypesForLoadProfileType');
    },

    measurementTypesLoad: function () {
        var me = this;
        this.measurementTypesStore.load({callback: function () {
            me.checkAndMarkMeasurementTypes();
        }});
    },

    removeMeasurementType: function (grid, index, id, row, event, record) {
        this.selectedMeasurementTypesStore.remove(record);
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
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
            msg: Uni.I18n.translate('loadProfileTypes.confirmWindow.removeMsg', 'MDC', 'This load profile type will no longer be available'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' ' + lastSelected.get('name') + '?',
            config: {
                me: me
            },
            fn: me.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            grid = me.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        if (state === 'confirm') {
            this.close();
            Ext.Ajax.request({
                url: '/api/mds/loadprofiles/' + lastSelected.getData().id,
                method: 'DELETE',
                waitMsg: 'Removing...',
                success: function () {
                    me.handleSuccessRequest(Uni.I18n.translate('loadProfileTypes.removeSuccessMsg', 'MDC', 'Load profile type was removed successfully'));
                    me.store.loadPage(1);
                },
                failure: function (response) {
                    var errorText,
                        errorTitle;

                    if (response.status == 400) {
                        errorText = Ext.decode(response.responseText, true).message;
                        errorTitle = Uni.I18n.translate('loadProfileTypes.removeErrorMsg', 'MDC', 'Error during removing of load profile');

                        me.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                    }
                }
            });
        } else if (state === 'cancel') {
            this.close();
        }
    },

    retrySubmit: function (btn) {
        var formPanel = this.getLoadTypeForm();
        btn.up('messagebox').hide();
        if (!Ext.isEmpty(formPanel)) {
            var submitBtn = formPanel.down('button[name=loadprofiletypeaction]');
            if (!Ext.isEmpty(submitBtn)) {
                this.onSubmit(submitBtn);
            }
        }
    },


    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getLoadTypeForm(),
            form = formPanel.getForm(),
            formErrorsPanel = formPanel.down('uni-form-error-message[name=errors]'),
            formValue = form.getValues(),
            timeDurationId = formValue.timeDuration,
            measurementTypesErrorPanel = formPanel.down('fieldcontainer').down('panel[name=measurementTypesErrors]'),
            preloader,
            jsonValues;
        formValue['measurementTypes'] = [];
        if (form.isValid() && me.selectedMeasurementTypesStore.getCount() > 0) {
            me.selectedMeasurementTypesStore.each(function (value) {
                formValue.measurementTypes.push({'id': value.getData().id});
            });
            formValue.timeDuration = { id: timeDurationId };
            jsonValues = Ext.JSON.encode(formValue);
            formErrorsPanel.hide();
            switch (btn.action) {
                case 'add':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Creating load profile type",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/mds/loadprofiles',
                        method: 'POST',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest('Load profile type saved');
                        },
//                        failure: function (response) {
//                            me.handleFailureRequest(response, 'Error during update', 'loadprofiletypenotificationerrorretry');
//                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
                case 'save':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Updating load profile form",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/mds/loadprofiles/' + me.loadProfileTypeId,
                        method: 'PUT',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest('Load profile type saved');
                        },
//                        failure: function (response) {
//                            me.handleFailureRequest(response, 'Error during update', 'loadprofiletypenotificationerrorretry');
//                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
            }
        } else {
            if (me.selectedMeasurementTypesStore.getCount() == 0) {
                measurementTypesErrorPanel.hide();
                measurementTypesErrorPanel.removeAll();
                measurementTypesErrorPanel.add({
                    html: 'You need to add at least one measurement type',
                    style: {
                        color: 'red'
                    }
                });
                measurementTypesErrorPanel.show();
            }
            formErrorsPanel.show();
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


    addMeasurementTypes: function () {
        var me = this,
            grid = this.getAddMeasurementTypesGrid(),
            selectedArray = grid.getView().getSelectionModel().getSelection();
        me.selectedMeasurementTypesStore.removeAll();
        Ext.each(selectedArray, function (measurementType) {
            me.selectedMeasurementTypesStore.add(measurementType);
        });
        Ext.History.back();
    },

    unCheckAllMeasurementTypes: function () {
        var grid = this.getAddMeasurementTypesGrid();
        grid.getView().getSelectionModel().deselectAll();
    },

    selectRadioButton: function (radiogroup) {
        var radioValue = radiogroup.getValue().measurementTypeRange;
        switch (radioValue) {
            case 'ALL':
                this.checkAllMeasurementTypes();
                break;
            case 'SELECTED':
                this.checkSelectedMeasurementTypes();
                break;
        }
    },

    checkSelectedMeasurementTypes: function () {
        var grid = this.getAddMeasurementTypesGrid(),
            view = this.getAddMeasurementTypesView(),
            uncheckMeasurement = this.getUncheckMeasurementButton();
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(view)) {
            grid.enable();
            uncheckMeasurement.enable();
        }
    },

    getCountOfCheckedMeasurementTypes: function () {
        var grid = this.getAddMeasurementTypesGrid(),
            measurementTypesCountSelected = grid.getView().getSelectionModel().getSelection().length,
            measurementTypesCountContainer = this.getAddMeasurementTypesCount(),
            measurementTypeWord;
        if (measurementTypesCountSelected == 1) {
            measurementTypeWord = ' measurement type selected'
        } else {
            measurementTypeWord = ' measurement types selected'
        }
        var widget = Ext.widget('container', {
            html: measurementTypesCountSelected + measurementTypeWord
        });

        measurementTypesCountContainer.removeAll(true);
        measurementTypesCountContainer.add(widget);
    },

    checkAllMeasurementTypes: function (store, records) {
        var grid = this.getAddMeasurementTypesGrid(),
            uncheckMeasurement = this.getUncheckMeasurementButton(),
            selectionModel = grid.getView().getSelectionModel();
        selectionModel.selectAll();
        grid.disable();
        uncheckMeasurement.disable();
    },

    checkAndMarkMeasurementTypes: function () {
        var grid = this.getAddMeasurementTypesGrid(),
            view = this.getAddMeasurementTypesView(),
            uncheckMeasurement = this.getUncheckMeasurementButton(),
            selectionModel = grid.getView().getSelectionModel(),
            radiogroup = Ext.ComponentQuery.query('loadProfileTypeAddMeasurementTypesView radiogroup[name=AllOrSelectedMeasurementTypes]')[0],
            recordsArray = [];
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(view)) {
            if (this.selectedMeasurementTypesStore.getCount() == 0) {
                selectionModel.selectAll();
                grid.disable();
                uncheckMeasurement.disable();
            } else {
                radiogroup.items.items[1].setValue(true);
                this.selectedMeasurementTypesStore.each(function (value) {
                    recordsArray.push(value);
                });
                selectionModel.select(recordsArray);
            }

        }
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

            numberOfLoadTypesContainer.removeAll(true);
            numberOfLoadTypesContainer.add(widget);

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
        var self = this,
            loadProfileTypesStore = self.getStore('Mdc.store.LoadProfileTypes'),
            widget;

        var showPage = function () {
            widget = Ext.widget('loadProfileTypeSetup', {
                config: {
                    gridStore: self.store
                }
            });
            self.getApplication().fireEvent('changecontentevent', widget);
            self.selectedMeasurementTypesStore.removeAll();
            self.temporallyFormValues = null;
            self.loadProfileAction = null;
            Ext.Array.each(Ext.ComponentQuery.query('[action=editloadprofiletype]'), function (item) {
                item.clearListeners();
                item.on('click', function () {
                    self.editRecord();
                });
            });
            Ext.Array.each(Ext.ComponentQuery.query('[action=deleteloadprofiletype]'), function (item) {
                item.clearListeners();
                item.on('click', function () {
                    self.showConfirmationPanel();
                });
            });
        };

        if (loadProfileTypesStore.getCount()) {
            showPage();
        } else {
            loadProfileTypesStore.load(showPage);
        }
    },

    showLoadProfileTypesCreateView: function () {
        var me = this,
            widget = Ext.widget('loadProfileTypeForm', { loadProfileTypeHeader: 'Add load profile type', loadProfileTypeAction: 'Add', loadButtonAction: 'add', loadProfileTypeActionHref: 'create'}),
            intervalCombobox = widget.down('combobox[name=timeDuration]');
        me.measurementTypesStore.removeAll();
        me.getApplication().fireEvent('changecontentevent', widget);
        intervalCombobox.store = me.intervalStore;
        me.intervalStore.load({callback: function () {
            if (!Ext.isEmpty(me.temporallyFormValues)) {
                me.loadTemporallyValues();
            } else {
                intervalCombobox.setValue(0);
            }
        }})

    },

    showLoadProfileTypesEditView: function (loadProfileTypeId) {
        var me = this;
        me.loadProfileTypeId = loadProfileTypeId;
        this.measurementTypesStore.removeAll();
        Ext.Ajax.request({
            url: '/api/mds/loadprofiles/' + me.loadProfileTypeId,
            params: {},
            method: 'GET',
            success: function (response) {
                var loadProfileType = Ext.JSON.decode(response.responseText),
                    widget = Ext.widget('loadProfileTypeForm', { loadProfileTypeHeader: Uni.I18n.translate('loadprofiletype.editloadprofiletypes', 'MDC', 'Edit load profile type'), loadProfileTypeAction: 'Save', loadButtonAction: 'save', loadProfileTypeActionHref: me.loadProfileTypeId + '/edit' }),
                    nameField = widget.down('textfield[name=name]'),
                    intervalCombobox = widget.down('combobox[name=timeDuration]'),
                    obisCodeField = widget.down('textfield[name=obisCode]'),
                    measurementTypesView = widget.down('fieldcontainer');

                me.getApplication().fireEvent('loadProfileType', loadProfileType);
                me.getApplication().fireEvent('changecontentevent', widget);

                nameField.setValue(loadProfileType.name);
                nameField.focus();

                intervalCombobox.store = me.intervalStore;
                intervalCombobox.setValue(loadProfileType.timeDuration.id);

                obisCodeField.setValue(loadProfileType.obisCode);

                if (loadProfileType.isLinkedToActiveDeviceConf) {
                    intervalCombobox.disable();
                    obisCodeField.disable();
                    Ext.each(measurementTypesView.items.items, function (item) {
                        item.disable();
                    });
                    measurementTypesView.disable();
                }

                Ext.each(loadProfileType.measurementTypes, function (measurementType) {
                    me.selectedMeasurementTypesStore.add(measurementType);
                });

                me.loadProfileAction = 'Edit load profile type';
                if (!Ext.isEmpty(this.temporallyFormValues)) {
                    this.loadTemporallyValues();
                }
            }
        });
    },

    showMeasurementTypesAddView: function (id) {
        var form = this.getLoadTypeForm(),
            loadProfileAction,
            breadcrumbActionHref;
        if (!Ext.isEmpty(form)) {
            this.saveTemporallyValues(form);
        }
        var widget = Ext.widget('loadProfileTypeAddMeasurementTypesView');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    saveTemporallyValues: function (form) {
        this.temporallyFormValues = form.getValues();
    },

    loadTemporallyValues: function () {
        var form = this.getLoadTypeForm();
        form.getForm().setValues(this.temporallyFormValues);
    }
});