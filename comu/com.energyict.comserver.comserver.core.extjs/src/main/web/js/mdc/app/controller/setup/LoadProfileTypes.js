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
        'setup.loadprofiletype.LoadProfileTypeDockedItems',
        'setup.loadprofiletype.LoadProfileTypeEmptyList',
        'setup.loadprofiletype.LoadProfileTypeForm',
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesView',
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesDockedItems',
        'setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid',
        'setup.loadprofiletype.LoadProfileTypeFloatingPanel'
    ],

    stores: [
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.RegisterTypes',
        'Mdc.store.SelectedMeasurementTypesForLoadProfileType',
        'Mdc.store.Intervals'
    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
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
    ],

    init: function () {
        this.control({
            'loadProfileTypeSetup': {
                afterrender: this.loadStore
            },
            'loadProfileTypeSetup loadProfileTypeGrid': {
                itemclick: this.loadGridItemDetail
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
            'menu menuitem[action=editloadprofiletype]': {
                click: this.editRecord
            },
            'menu menuitem[action=deleteloadprofiletype]': {
                click: this.showConfirmationPanel
            },
            'loadProfileTypeFloatingPanel button[action=cancel]': {
                click: this.closeFloatingMessage
            },
            'loadProfileTypeFloatingPanel button[action=delete]': {
                click: this.deleteRecord
            },
            '#loadProfileReadingTypeBtn': {
                showReadingTypeInfo: this.showReadingType
            },
            'button[action=loadprofiletypenotificationerrorretry]': {
                click: this.retrySubmit
            }
        });

        this.listen({
            store: {
                '#LoadProfileTypes': {
                    load: this.checkLoadProfileTypesCount
                },
                '#RegisterTypes': {
                    load: this.checkAndMarkMeasurementTypes
                }
            }
        });

        this.intervalStore = this.getStore('Intervals');
        this.store = this.getStore('LoadProfileTypes');
        this.measurementTypesStore = this.getStore('RegisterTypes');
        this.selectedMeasurementTypesStore = this.getStore('SelectedMeasurementTypesForLoadProfileType');
    },

    loadStore: function () {
        this.store.load({
            params: {
                sort: 'name'
            }
        });
    },

    measurementTypesLoad: function () {
        this.measurementTypesStore.load();
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


    enableOverviewPanel: function () {
        var overviewPanel = Ext.ComponentQuery.query('loadProfileTypeSetup')[0];
        if (!Ext.isEmpty(overviewPanel)) {
            overviewPanel.enable();
        }
    },

    closeFloatingMessage: function (btn) {
        this.enableOverviewPanel();
        btn.up('loadProfileTypeFloatingPanel').hide();
    },

    showConfirmationPanel: function () {
        var grid = this.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            overview = Ext.ComponentQuery.query('loadProfileTypeSetup')[0],
            confirmationMessage = Ext.widget('loadProfileTypeFloatingPanel', {
                width: overview.getWidth() / 3,
                title: "Remove " + lastSelected.getData().name + "?",
                html: "<br>This load profile type will no longer be available<br><br>"
            });
        overview.disable();
        confirmationMessage.show();
    },

    deleteRecord: function (btn) {
        var grid = this.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            me = this;
        btn.up('loadProfileTypeFloatingPanel').hide();
        this.enableOverviewPanel();
        Ext.Ajax.request({
            url: '/api/mds/loadprofiles/' + lastSelected.getData().id,
            method: 'DELETE',
            waitMsg: 'Removing...',
            success: function () {
                successMessage = Ext.widget('loadProfileTypeFloatingPanel', {
                    title: "Success",
                    html: "<br>Load profile type was removed successfully <br><br>",
                    autoCloseDelay: 2000
                });
                successMessage.getDockedItems()[0].hide();
                successMessage.show();
                me.store.load();
            },
            failure: function (result, request) {
                var data = Ext.JSON.decode(result.responseText);
                var errorMessage = Ext.widget('loadProfileTypeFloatingPanel', {
                    width: grid.getWidth() / 2,
                    title: "Error during removing of load profile",
                    html: "<br>" + data.error + ": " + data.message + "<br><br>"
                });
                errorMessage.down('button[name=delete]').hide();
                errorMessage.show();
            }
        });
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
            formErrorsPanel = formPanel.down('panel[name=errors]'),
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
            switch (btn.text) {
                case 'Add':
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
                            me.handleSuccessRequest('Successfully created');
                        },
                        failure: function (response) {
                            me.handleFailureRequest(response, 'Error during create');
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
                case 'Save':
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
                            me.handleSuccessRequest('Successfully updated');
                        },
                        failure: function (response) {
                            me.handleFailureRequest(response, 'Error during update');
                        },
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
            formErrorsPanel.hide();
            formErrorsPanel.removeAll();
            formErrorsPanel.add({
                html: 'There are errors on this page that require your attention.',
                style: {
                    color: 'red'
                }
            });
            formErrorsPanel.show();
        }
    },

    handleSuccessRequest: function (headerText) {
        window.location.href = '#/administration/loadprofiletypes';
        Ext.create('widget.uxNotification', {
            html: headerText,
            ui: 'notification-success'
        }).show();
    },

    handleFailureRequest: function (response, headerText) {
        var result = Ext.JSON.decode(response.responseText),
            errormsgs = '';
        Ext.each(result.errors, function(error) {
            errormsgs += error.msg + '<br>'
        });
        Ext.widget('messagebox', {
            buttons: [
                {
                    text: 'Retry',
                    action: 'loadprofiletypenotificationerrorretry',
                    ui: 'delete'
                },
                {
                    text: 'Cancel',
                    action: 'cancel',
                    ui: 'link',
                    handler:function(button,event){
                        this.up('messagebox').hide();
                        Ext.History.back();
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

    checkAllMeasurementTypes: function () {
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

    loadGridItemDetail: function (grid, record) {
        var form = Ext.ComponentQuery.query('loadProfileTypeSetup loadProfileTypePreview form')[0],
            previewPanel = this.getLoadTypePreview(),
            recordData = record.getData(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: form
            });
        if (this.displayedItemId != recordData.id) {
            grid.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = recordData.id;
        previewPanel.setTitle(recordData.name);
        form.loadRecord(record);
        preloader.destroy();
    },

    showLoadProfileTypes: function () {
        var widget = Ext.widget('loadProfileTypeSetup', { intervalStore: this.intervalStore });
        widget.down('#loadProfileTypesTitle').html = '<h1>' + Uni.I18n.translate('loadprofiletype.loadprofiletypes', 'MDC', 'Load profile types') + '</h1>';
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        this.overviewBreadCrumbs(null, null);
        this.selectedMeasurementTypesStore.removeAll();
        this.temporallyFormValues = null;
        this.loadProfileAction = null;
    },

    showLoadProfileTypesCreateView: function () {
        var widget = Ext.widget('loadProfileTypeForm', { loadProfileTypeHeader: 'Add load profile type', loadProfileTypeAction: 'Add', loadProfileTypeActionHref: 'create'}),
            intervalCombobox = widget.down('combobox[name=timeDuration]');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        this.breadcrumbActionHref = 'create';
        this.loadProfileAction = 'Add load profile type';
        this.overviewBreadCrumbs(this.breadcrumbActionHref, this.loadProfileAction, null);
        intervalCombobox.store = this.intervalStore;
        if (!Ext.isEmpty(this.temporallyFormValues)) {
            this.loadTemporallyValues();
        }
    },

    showLoadProfileTypesEditView: function (loadProfileTypeId) {
        var me = this;
        me.loadProfileTypeId = loadProfileTypeId;
        Ext.Ajax.request({
            url: '/api/mds/loadprofiles/' + me.loadProfileTypeId,
            params: {},
            method: 'GET',
            success: function (response) {
                var loadProfileType = Ext.JSON.decode(response.responseText),
                    widget = Ext.widget('loadProfileTypeForm', { loadProfileTypeHeader: 'Edit load profile type', loadProfileTypeAction: 'Save', loadProfileTypeActionHref: me.loadProfileTypeId + '/edit' }),
                    intervalCombobox = widget.down('combobox[name=timeDuration]');
                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                widget.down('textfield[name=name]').setValue(loadProfileType.name);
                widget.down('textfield[name=obisCode]').setValue(loadProfileType.obisCode);
                intervalCombobox.store = me.intervalStore;
                intervalCombobox.setValue(loadProfileType.timeDuration.id);
                Ext.each(loadProfileType.measurementTypes, function (measurementType) {
                    me.selectedMeasurementTypesStore.add(measurementType);
                });
                me.loadProfileAction = 'Edit load profile type';
                me.breadcrumbActionHref = me.loadProfileTypeId + '/edit';
                me.overviewBreadCrumbs(me.breadcrumbActionHref, me.loadProfileAction, null);
            }
        });
    },

    showMeasurementTypesAddView: function () {
        var widget = Ext.widget('loadProfileTypeAddMeasurementTypesView'),
            form = this.getLoadTypeForm();
        if (!Ext.isEmpty(form)) {
            this.saveTemporallyValues(form);
            this.getApplication().getController('Mdc.controller.Main').showContent(widget);
            this.overviewBreadCrumbs(this.breadcrumbActionHref, this.loadProfileAction, 'Add measurement types');
        }
    },

    saveTemporallyValues: function (form) {
        this.temporallyFormValues = form.getValues();
    },

    loadTemporallyValues: function () {
        var form = this.getLoadTypeForm();
        form.getForm().setValues(this.temporallyFormValues);
    },

    overviewBreadCrumbs: function (actionHref, action, nextAction) {
        var breadcrumbLoadProfileTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('loadprofiletype.loadprofiletypes', 'MDC', 'Load profile types'),
            href: 'loadprofiletypes'
        });

        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#/administration'
        });

        if (Ext.isEmpty(action)) {
            breadcrumbParent.setChild(breadcrumbLoadProfileTypes);
        } else {
            var breadcrumbLoadProfileTypesAction = Ext.create('Uni.model.BreadcrumbItem', {
                text: action,
                href: actionHref
            });
            if (Ext.isEmpty(nextAction)) {
                breadcrumbParent.setChild(breadcrumbLoadProfileTypes).setChild(breadcrumbLoadProfileTypesAction);
            } else {
                var breadcrumbLoadProfileTypesNextAction = Ext.create('Uni.model.BreadcrumbItem', {
                    text: nextAction,
                    href: 'addmeasurementtypes'
                });
                breadcrumbParent.setChild(breadcrumbLoadProfileTypes).setChild(breadcrumbLoadProfileTypesAction).setChild(breadcrumbLoadProfileTypesNextAction);
            }
        }

        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    }

});