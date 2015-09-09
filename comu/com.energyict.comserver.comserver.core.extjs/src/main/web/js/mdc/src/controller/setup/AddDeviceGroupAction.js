Ext.define('Mdc.controller.setup.AddDeviceGroupAction', {
    extend: 'Ext.app.Controller',
    views: [
        'Mdc.view.setup.devicegroup.Step1',
        'Mdc.view.setup.devicegroup.Step2',
        'Mdc.view.setup.devicegroup.Browse',
        'Mdc.view.setup.devicegroup.Navigation',
        'Mdc.view.setup.devicegroup.Wizard',
        'Mdc.view.setup.devicegroup.Edit'
    ],
    requires: [
        'Uni.view.window.Wizard',
        'Mdc.view.setup.devicesearch.DevicesSideFilter'
    ],

    stores: [
        'Mdc.store.DevicesBuffered',
        'DeviceGroups',
        'Mdc.model.DeviceType',
        'Mdc.store.DevicesOfDeviceGroupWithoutPaging'
    ],

    refs: [
        {
            ref: 'backButton',
            selector: 'adddevicegroup-wizard #backButton'
        },
        {
            ref: 'nextButton',
            selector: 'adddevicegroup-wizard #nextButton'
        },
        {
            ref: 'confirmButton',
            selector: 'adddevicegroup-wizard #confirmButton'
        },
        {
            ref: 'finishButton',
            selector: 'adddevicegroup-wizard #finishButton'
        },
        {
            ref: 'wizardCancelButton',
            selector: 'adddevicegroup-wizard #wizardCancelButton'
        },
        {
            ref: 'navigationMenu',
            selector: '#devicegroupaddnavigation'
        },
        {
            ref: 'addDeviceGroupWizard',
            selector: '#adddevicegroupwizard'
        },
        {
            ref: 'nameTextField',
            selector: '#deviceGroupNameTextField'
        },
        {
            ref: 'dynamicRadioButton',
            selector: 'devicegroup-wizard-step1 #dynamicDeviceGroup'
        },
        {
            ref: 'step1Form',
            selector: 'devicegroup-wizard-step1 form'
        },
        {
            ref: 'step1FormErrorMessage',
            selector: '#step1-adddevicegroup-errors'
        },
        {
            ref: 'step2FormErrorMessage',
            selector: '#step2-adddevicegroup-errors'
        },
        {
            ref: 'staticGrid',
            selector: 'mdc-search-results #static-grid'
        },
        {
            ref: 'dynamicGrid',
            selector: 'mdc-search-results #dynamic-grid'
        },
        {
            ref: 'staticGridContainer',
            selector: 'mdc-search-results #static-grid-container'
        },
        {
            ref: 'dynamicGridContainer',
            selector: 'mdc-search-results #dynamic-grid-container'
        },
        {
            ref: 'dynamicGridFilter',
            selector: 'devicegroup-wizard-step2 mdc-view-setup-devicesearch-devicestopfilter'
        },
        {
            ref: 'staticGridFilter',
            selector: 'devicegroup-wizard-step2 mdc-view-setup-devicesearch-buffereddevicestopfilter'
        },
        {
            ref: 'editPage',
            selector: 'device-group-edit'
        }
    ],

    stepTwo: 'deviceGroupWizardStep2',

    addDeviceGroupWidget: null,
    dynamic: false,
    router: null,

    init: function () {
        this.control({
            'adddevicegroup-wizard #backButton': {
                click: this.backClick
            },
            'adddevicegroup-wizard #nextButton': {
                click: this.nextClick
            },
            'adddevicegroup-wizard #confirmButton': {
                click: this.confirmClick
            },
            'adddevicegroup-wizard #finishButton': {
                click: this.finishClick
            },
            'adddevicegroup-wizard #wizardCancelButton': {
                click: this.cancelClick
            },
            'device-group-edit #edit-device-group-action': {
                click: this.editDeviceGroup
            }
        });
    },

    backClick: function () {
        var layout = this.getAddDeviceGroupWizard().getLayout(),
            currentCmp = layout.getActiveItem();
        this.getNavigationMenu().movePrevStep();
        this.changeContent(layout.getPrev(), currentCmp);
    },

    nextClick: function () {
        var layout = this.getAddDeviceGroupWizard().getLayout(),
            nameField = this.getNameTextField(),
            step1ErrorMsg = this.getStep1FormErrorMessage();
        if (layout.getNext().name === this.stepTwo && nameField.getValue() === '') {
            Ext.suspendLayouts();
            step1ErrorMsg.show();
            nameField.markInvalid(Uni.I18n.translate('general.fieldIsRequired', 'MDC', 'This field is required'));
            Ext.resumeLayouts(true);
        } else if (layout.getNext().name === this.stepTwo && nameField.getValue() !== '' && this.nameExistsAlready()) {
            Ext.suspendLayouts();
            step1ErrorMsg.show();
            nameField.markInvalid(Uni.I18n.translate('devicegroup.duplicatename', 'MDC', 'A device group with this name already exists.'));
            Ext.resumeLayouts(true);
        } else {
            if (layout.getNext().name === this.stepTwo) {
                if (this.getDynamicRadioButton().checked) {
                    this.getStaticGridFilter().setVisible(false);
                    this.getStaticGridContainer().setVisible(false);
                    this.getDynamicGridFilter().setVisible(true);
                    this.getDynamicGridContainer().setVisible(true);
                } else {
                    this.getDynamicGridFilter().setVisible(false);
                    this.getDynamicGridContainer().setVisible(false);
                    this.getStaticGridFilter().setVisible(true);
                    this.getStaticGridContainer().setVisible(true);
                }
            }
            Ext.suspendLayouts();
            step1ErrorMsg.hide();
            nameField.clearInvalid();
            Ext.resumeLayouts(true);
            this.getNavigationMenu().moveNextStep();
            this.changeContent(layout.getNext(), layout.getActiveItem());
            if (layout.getActiveItem().name == this.stepTwo) {
                this.getApplication().getController('Mdc.controller.setup.DevicesAddGroupController').applyFilters();
            }
        }
        this.getStep2FormErrorMessage().setVisible(false);
    },

    nameExistsAlready: function () {
        var store = this.getDeviceGroupsStore();
        var newName = this.getNameTextField().getValue();
        store.clearFilter();
        store.filter([
            {
                filterFn: function (item) {
                    return item.get("name").toLowerCase() === newName.toLowerCase();
                }
            }
        ]);
        var length = store.data.length;
        store.clearFilter();
        return length > 0;
    },

    confirmClick: function () {

    },

    finishClick: function () {
        if (this.getDynamicRadioButton().checked) {
            this.addDeviceGroupAndReturnToList();
        } else {
            var numberOfDevices = this.getStaticGrid().getSelectionModel().getSelection().length;
            if ((numberOfDevices == 0) && (!(this.getStaticGrid().allChosenByDefault))) {
                this.getStep2FormErrorMessage().setVisible(true);
            } else {
                this.addDeviceGroupAndReturnToList();
            }
        }
    },

    addDeviceGroupAndReturnToList: function () {
        this.addDeviceGroupWidget = null;
        this.addDeviceGroup();
        this.getDeviceGroupsStore().load();
    },

    addDeviceGroup: function () {
        var me = this,
            record = Ext.create('Mdc.model.DeviceGroup'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: this.getAddDeviceGroupWizard()
            });

        preloader.show();

        if (record) {
            record.set('name', this.getNameTextField().getValue());
            var isDynamic = this.getDynamicRadioButton().checked;
            record.set('dynamic', isDynamic);
            record.set('filter', me.getFilterObjectFromQueryString());
            record.set('devices', []);
            if (!isDynamic) {
                var grid = this.getStaticGrid();
                var devicesList = [];
                if (grid.isAllSelected()) {
                    devicesList = null;
                } else {
                    var selection = this.getStaticGrid().getSelectionModel().getSelection();
                    var numberOfDevices = this.getStaticGrid().getSelectionModel().getSelection().length;
                    for (var i = 0; i < numberOfDevices; i++) {
                        devicesList.push(this.getStaticGrid().getSelectionModel().getSelection()[i].data.id);
                    }
                }
                record.set('devices', devicesList);
            }
            record.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('devices/devicegroups').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceGroup.add.success.msg', 'MDC', 'Device group added'));
                },
                failure: function (response) {
                    if (response.status == 400) {
                        var result = Ext.decode(response.responseText, true),
                            errorTitle = Uni.I18n.translate('deviceGroup.add.fail.msg', 'MDC', 'Failed to add'),
                            errorText = Uni.I18n.translate('deviceGroup.add.fail.info', 'MDC',
                                'Device group could not be added. There was a problem accessing the database');

                        if (result !== null) {
                            errorTitle = result.error;
                            errorText = result.message;
                        }
                        self.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                    }
                },
                callback: function () {
                    preloader.destroy();
                }
            });
        }
    },

    cancelClick: function () {
        this.addDeviceGroupWidget = null;
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups').forward();
    },

    showAddDeviceGroupAction: function () {
        this.addDeviceGroupWidget = Ext.widget('add-devicegroup-browse');
        if (this.router) {
            this.router = undefined;
        }
        this.getApplication().fireEvent('changecontentevent', this.addDeviceGroupWidget);
    },

    changeContent: function (nextCmp, currentCmp) {
        var layout = this.getAddDeviceGroupWizard().getLayout();
        layout.setActiveItem(nextCmp);
        this.updateButtonsState(nextCmp);
    },

    updateButtonsState: function (activePage) {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            backBtn = wizard.down('#backButton'),
            nextBtn = wizard.down('#nextButton'),
            finishBtn = wizard.down('#finishButton'),
            cancelBtn = wizard.down('#wizardCancelButton');

        switch (activePage.name) {
            case 'deviceGroupWizardStep1' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(false);
                backBtn.setDisabled(true);
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 'deviceGroupWizardStep2' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(true);
                backBtn.setDisabled(false);
                finishBtn.show();
                cancelBtn.show();
                break;
        }
    },

    showEditDeviceGroup: function (deviceGroupId) {
        var me = this,
            deviceGroupModel = me.getModel('Mdc.model.DeviceGroup'),
            router = me.getController('Uni.controller.history.Router'),
            cancelLink,
            view,
            isDynamic,
            store,
            staticDevices,
            deviceTypesProcessed = false,
            extraQueryStringPart = '';

        if (Ext.isEmpty(Ext.ComponentQuery.query('device-group-edit')[0])) {
            me.fromDeviceGroupDetails = router.queryParams.fromDetails === 'true';
            if (me.fromDeviceGroupDetails) {
                cancelLink = '#/devices/devicegroups/' + encodeURIComponent(deviceGroupId);
            } else {
                cancelLink = '#/devices/devicegroups';
            }
            view = Ext.widget('device-group-edit', {
                returnLink: cancelLink
            });
        } else {
            view = Ext.ComponentQuery.query('device-group-edit')[0];
            view.down('#static-grid-container').selectByDefault = true;
        }
        deviceGroupModel.load(deviceGroupId, {
            success: function (record) {
                me.deviceGroup = record;
                me.dynamic = record.get('dynamic');
                me.deviceGroupName = record.get('name');
                if (!me.router) {
                    Ext.Array.each(record.raw.criteria, function (criteria) {
                        switch (criteria.criteriaName) {
                            case 'serialNumber':
                                router.filter.set('serialNumber', criteria.criteriaValues[0]);
                                extraQueryStringPart = extraQueryStringPart.concat('&serialNumber=', criteria.criteriaValues[0]);
                                break;
                            case 'mRID':
                                router.filter.set('mRID', criteria.criteriaValues[0]);
                                extraQueryStringPart = extraQueryStringPart.concat('&mRID=', criteria.criteriaValues[0]);
                                break;
                            case 'deviceConfiguration.deviceType.name':
                                if (deviceTypesProcessed) {
                                    break;
                                }
                                deviceTypesProcessed = true;
                                router.filter.set('deviceTypes', record.get('deviceTypeIds'));
                                if (Ext.isArray(record.get('deviceTypeIds'))) {
                                    Ext.Array.each(record.get('deviceTypeIds'), function (eachDeviceTypeId) {
                                        extraQueryStringPart = extraQueryStringPart.concat('&deviceTypes=', eachDeviceTypeId);
                                    });
                                } else {
                                    extraQueryStringPart = extraQueryStringPart.concat('&deviceTypes=', record.get('deviceTypeIds'));
                                }
                                if (record.get('deviceTypeIds').length === 1 && !Ext.isEmpty(record.get('deviceConfigurationIds'))) {
                                    router.filter.set('deviceConfigurations', record.get('deviceConfigurationIds'));
                                    if (Ext.isArray(record.get('deviceConfigurationIds'))) {
                                        Ext.Array.each(record.get('deviceConfigurationIds'), function (eachDeviceCfgId) {
                                            extraQueryStringPart = extraQueryStringPart.concat('&deviceConfigurations=', eachDeviceCfgId);
                                        });
                                    } else {
                                        extraQueryStringPart = extraQueryStringPart.concat('&deviceConfigurations=', record.get('deviceConfigurationIds'));
                                    }
                                }
                                break;
                        }
                    });
                    me.router = router;

                    me.getApplication().fireEvent('changecontentevent', view);

                    if (me.dynamic) {
                        me.getStaticGridContainer().setVisible(false);
                        me.getDynamicGridContainer().setVisible(true);
                        if (extraQueryStringPart.length > 0) {
                            view.setDynamicFilter(me.getQueryObjectFromQueryString(extraQueryStringPart));
                        }
                    } else {
                        view.setLoading();
                        Ext.suspendLayouts();
                        me.getDynamicGridContainer().setVisible(false);
                        me.getStaticGridContainer().setVisible(true);
                        Ext.resumeLayouts(true);
                        view.down('#static-grid-container').selectByDefault = false;
                        staticDevices = me.getStore('Mdc.store.DevicesOfDeviceGroupWithoutPaging');
                        staticDevices.getProxy().setUrl(deviceGroupId);
                        staticDevices.load(function (existingRecords) {
                            var staticGrid = me.getStaticGrid();
                            if (staticGrid) {
                                store = staticGrid.getStore();
                                staticGrid.un('selectionchange', staticGrid.onSelectionChange);
                                staticGrid.getUncheckAllButton().on('click', function () {
                                    staticDevices.loadData([], false);
                                    staticGrid.getSelectionCounter().setText(staticGrid.counterTextFn(staticDevices.getCount()));
                                });
                                staticGrid.onSelectionChangeInGroup(existingRecords);
                                staticGrid.on('select', function (selectionModel, record) {
                                    staticDevices.add(record);
                                    staticGrid.onSelectionChangeInGroup(staticDevices.getRange());
                                });
                                staticGrid.on('deselect', function (selectionModel, record) {
                                    staticDevices.remove(record);
                                    staticGrid.onSelectionChangeInGroup(staticDevices.getRange());
                                });
                                store.setFilterModel(router.filter);

                                store.on('prefetch', function (store, records) {
                                    if (!staticGrid.isDestroyed) {
                                        staticGrid.suspendEvent('select');
                                        staticGrid.getSelectionModel().select(Ext.Array.filter(existingRecords, function (existingItem) {
                                            return !!Ext.Array.findBy(records, function (item) {
                                                return existingItem.getId() === item.getId();
                                            });
                                        }), true);
                                        staticGrid.resumeEvent('select');
                                    } else {
                                        store.un('prefetch', this);
                                    }
                                });
                                store.data.clear();
                                view.setLoading(false);
                                store.loadPage(1);
                            }
                        });
                    }
                    Ext.suspendLayouts();
                    view.down('#device-group-edit-panel').setTitle(
                        Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [me.deviceGroupName])
                    );
                    me.getNameTextField().setValue(record.get('name'));
                    isDynamic = record.get('dynamic')
                        ? Uni.I18n.translate('general.dynamic', 'MDC', 'Dynamic')
                        : Uni.I18n.translate('general.static', 'MDC', 'Static');
                    view.down('#device-group-type').setValue(isDynamic);
                    if (me.dynamic) {
                        view.showDynamicFilter();
                    } else {
                        view.showStaticFilter();
                        view.setSelectedRadioBtnFromStaticFilter();
                    }
                    Ext.resumeLayouts(true);
                    me.getApplication().fireEvent('loadDeviceGroup', record);
                }
            }
        });
    },

    editDeviceGroup: function () {
        var me = this,
            page = me.getEditPage(),
            router = me.getController('Uni.controller.history.Router'),
            step1ErrorMsg = me.getStep1FormErrorMessage(),
            nameField = me.getNameTextField(),
            nameValue = nameField.getValue(),
            record = me.deviceGroup,
            selection;

        if (!me.dynamic) {
            selection = me.getStaticGrid().getSelectionModel().getSelection();
        }
        if (nameValue === '') {
            Ext.suspendLayouts();
            step1ErrorMsg.show();
            nameField.markInvalid(Uni.I18n.translate('general.fieldIsRequired', 'MDC', 'This field is required'));
            Ext.resumeLayouts(true);
        } else if (nameValue !== me.deviceGroupName && me.nameExistsAlready()) {
            Ext.suspendLayouts();
            step1ErrorMsg.show();
            nameField.markInvalid(Uni.I18n.translate('general.name.shouldBeUnique', 'MDC', 'Name should be unique'));
            Ext.resumeLayouts(true);
        } else if (!me.dynamic && (selection.length == 0) && !me.getStaticGrid().allChosenByDefault) {
            me.getStep2FormErrorMessage().setVisible(true);
        } else {
            page.setLoading(true);
            record.criteriaStore.removeAll();
            record.set('name', nameValue);
            record.set('dynamic', me.dynamic);
            record.set('filter', me.getFilterObjectFromQueryString());
            record.set('devices', []);
            if (!me.dynamic) {
                var devicesList = [];
                if (me.getStaticGrid().isAllSelected()) {
                    devicesList = null;
                } else {
                    Ext.Array.each(me.getStore('Mdc.store.DevicesOfDeviceGroupWithoutPaging').getRange(), function (item) {
                        devicesList.push(item.get('id'));
                    });
                }
                record.set('devices', devicesList);
            }
            record.save({
                success: function () {
                    if (me.fromDeviceGroupDetails) {
                        router.getRoute('devices/devicegroups/view').forward();
                    } else {
                        router.getRoute('devices/devicegroups').forward();
                    }
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceGroup.edit.success.msg', 'MDC', 'Device group saved'));
                },
                callback: function () {
                    page.setLoading(false);
                }
            });
        }
    },

    getFilterObjectFromQueryString: function() {
        var filterObject = Uni.util.QueryString.getQueryStringValues(false);
        this.adaptFilterObject(filterObject);
        return filterObject;
    },

    getQueryObjectFromQueryString: function(queryString) {
        var queryObject = Ext.Object.fromQueryString(queryString, false);
        this.adaptFilterObject(queryObject);
        return queryObject;
    },

    adaptFilterObject: function(filterObject) {
        // Assure that properties that are expected to be an int array, are indeed int arrays
        var props = ['deviceTypes', 'deviceConfigurations'];
        Ext.Array.each(props, function(prop) {
            if (filterObject.hasOwnProperty(prop)) {
                if (Ext.isArray(filterObject[prop])) {
                    for (i = 0; i < filterObject[prop].length; i++) {
                        filterObject[prop][i] = parseInt(filterObject[prop][i]);
                    }
                } else {
                    var theOneValue = filterObject[prop];
                    filterObject[prop] = [];
                    filterObject[prop][0] = !Ext.isNumber(theOneValue) ? parseInt(theOneValue) : theOneValue;
                }
            }
        });
    }
});
