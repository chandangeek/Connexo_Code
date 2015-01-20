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
        'Mdc.model.DeviceType'
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
            ref: 'addDeviceGroupSideFilter',
            selector: 'add-devicegroup-browse #addDeviceGroupSideFilter'
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
            ref: 'filterForm',
            selector: '#addDeviceGroupSideFilter form'
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
            ref: 'step1FormNameErrorMessage',
            selector: '#step1-adddevicegroup-name-errors'
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
            ref: 'editPage',
            selector: 'device-group-edit'
        }
    ],

    addDeviceGroupWidget: null,
    dynamic: false,
    createWidget: true,
    router: null,

    init: function () {
        this.createWidget = true;
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
        this.createWidget = false;
        var layout = this.getAddDeviceGroupWizard().getLayout();
        if ((layout.getNext().name == 'deviceGroupWizardStep2') &&
            (this.getNameTextField().getValue() == '')) {
            this.getStep1FormErrorMessage().setVisible(true);
            this.getStep1FormNameErrorMessage().setVisible(false);
            this.createWidget = true;
        } else if ((layout.getNext().name == 'deviceGroupWizardStep2') &&
            (this.getNameTextField().getValue() !== '') &&
            (this.nameExists())) {
            this.getStep1FormNameErrorMessage().setVisible(true);
            this.getStep1FormErrorMessage().setVisible(false);
            this.createWidget = true;
        } else {
            if (this.createWidget) {
                this.createWidget = false;
            }
            if (layout.getNext().name == 'deviceGroupWizardStep2') {
                if (this.getDynamicRadioButton().checked) {
                    this.getStaticGridContainer().setVisible(false);
                    this.getDynamicGridContainer().setVisible(true);
                } else {
                    this.getDynamicGridContainer().setVisible(false);
                    this.getStaticGridContainer().setVisible(true);
                }
            }
            this.getStep1FormErrorMessage().setVisible(false);
            this.getStep1FormNameErrorMessage().setVisible(false);
            this.getNavigationMenu().moveNextStep();
            this.changeContent(layout.getNext(), layout.getActiveItem());
            if (layout.getActiveItem().name == 'deviceGroupWizardStep2') {
                this.getApplication().getController('Mdc.controller.setup.DevicesAddGroupController').applyFilter();
            }
        }
        this.getStep2FormErrorMessage().setVisible(false);
    },

    nameExists: function () {
        var store = this.getDeviceGroupsStore();
        var newName = this.getNameTextField().getValue();
        store.clearFilter();
        store.filter([
            {filterFn: function (item) {
                return item.get("name").toLowerCase() === newName.toLowerCase();
            }}
        ]);
        var length = store.data.length;
        store.clearFilter();
        return length > 0;
    },

    confirmClick: function () {

    },

    finishClick: function () {
        if (!(this.getDynamicRadioButton().checked)) {
            var numberOfDevices = this.getStaticGrid().getSelectionModel().getSelection().length;
            if ((numberOfDevices == 0) && (!(this.getStaticGrid().allChosenByDefault))) {
                this.getStep2FormErrorMessage().setVisible(true);
            }
            else {
                this.addDeviceGroupAndReturnToList();
            }
        } else {
            this.addDeviceGroupAndReturnToList();
        }
    },

    disableCreateWidget: function () {
        this.createWidget = false;
    },

    addDeviceGroupAndReturnToList: function () {
        this.addDeviceGroupWidget = null;
        this.addDeviceGroup();
        this.getDeviceGroupsStore().load();
        //var router = this.getController('Uni.controller.history.Router');
        //router.getRoute('devices/devicegroups').forward();
    },

    addDeviceGroup: function () {
        var me = this;
        var record = Ext.create('Mdc.model.DeviceGroup');
        var router = this.getController('Uni.controller.history.Router');
        var preloader = Ext.create('Ext.LoadMask', {
            msg: "Loading...",
            target: this.getAddDeviceGroupWizard()
        });
        preloader.show();
        if (record) {
            record.set('name', this.getNameTextField().getValue());
            var isDynamic = this.getDynamicRadioButton().checked;
            record.set('dynamic', isDynamic);
            record.set('filter', this.getController('Uni.controller.history.Router').filter.data);
            if (!isDynamic) {
                var grid = this.getStaticGrid();
                var devicesList = [];
                if (grid.isAllSelected()) {
                    devicesList = null;
                } else {
                    var selection = this.getStaticGrid().getSelectionModel().getSelection();
                    var numberOfDevices = this.getStaticGrid().getSelectionModel().getSelection().length;
                    for (i = 0; i < numberOfDevices; i++) {
                        devicesList.push(this.getStaticGrid().getSelectionModel().getSelection()[i].data.id);
                    }
                }
                record.set('devices', devicesList);
            }
            record.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('devices/devicegroups').forward();
                    me.getApplication().fireEvent('acknowledge', 'Device group added');
                },
                failure: function (response) {
                    if (response.status == 400) {
                        var result = Ext.decode(response.responseText, true),
                            errorTitle = 'Failed to add',
                            errorText = 'Device group could not be added. There was a problem accessing the database';

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
        if (this.createWidget) {
            this.addDeviceGroupWidget = Ext.widget('add-devicegroup-browse');
            if (this.router) {
                this.router = undefined;
            }
            this.getApplication().fireEvent('changecontentevent', this.addDeviceGroupWidget);
            this.getApplication().getController('Mdc.controller.setup.DevicesAddGroupController').initFilterModel();
            this.getAddDeviceGroupSideFilter().setVisible(false);
        }
        this.createWidget = true;
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
                this.getAddDeviceGroupSideFilter().setVisible(false);
                break;
            case 'deviceGroupWizardStep2' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(true);
                backBtn.setDisabled(false);
                finishBtn.show();
                cancelBtn.show();
                this.getAddDeviceGroupSideFilter().setVisible(true);
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
            staticDevices = [];

        if (Ext.isEmpty(Ext.ComponentQuery.query('device-group-edit')[0])) {
            me.fromDeviceGroupDetails = router.queryParams.fromDetails === 'true';
            if (me.fromDeviceGroupDetails) {
                cancelLink = '#/devices/devicegroups/' + deviceGroupId;
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
                                break;
                            case 'mRID':
                                router.filter.set('mRID', criteria.criteriaValues[0]);
                                break;
                            case 'deviceConfiguration.deviceType.name':
                                router.filter.set('deviceTypes', record.get('deviceTypeIds'));
                                if (record.get('deviceTypeIds').length === 1 && !Ext.isEmpty(record.get('deviceConfigurationIds'))) {
                                    router.filter.set('deviceConfigurations', record.get('deviceConfigurationIds'));
                                }
                                break;
                        }
                    });
                    me.router = router;

                    me.getApplication().fireEvent('changecontentevent', view);

                    if (me.dynamic) {
                        me.getStaticGridContainer().setVisible(false);
                        me.getDynamicGridContainer().setVisible(true);
                        me.getApplication().getController('Mdc.controller.setup.DevicesAddGroupController').applyFilter();
                    } else {
                        store = me.getStaticGrid().getStore();
                        store.setFilterModel(router.filter);
                        store.load(function() {
                                Ext.Array.each(record.get('selectedDevices'), function (item) {
                                    var rec = store.getById(item);
                                    if (rec) {
                                        staticDevices.push(rec);
                                    }
                                });
                                me.getDynamicGridContainer().setVisible(false);
                                me.getStaticGridContainer().setVisible(true);
                                view.down('#static-grid-container').selectByDefault = false;
                                me.getStaticGrid().getSelectionModel().select(staticDevices);
                        });
                    }
                    view.down('#device-group-edit-panel').setTitle(Uni.I18n.translate('communicationtasks.edit', 'MDC', 'Edit') + " '" + me.deviceGroupName + "'");
                    me.getNameTextField().setValue(record.get('name'));
                    isDynamic = record.get('dynamic') ? Uni.I18n.translate('general.dynamic', 'MDC', 'Dynamic') : Uni.I18n.translate('general.static', 'MDC', 'Static');
                    view.down('#device-group-type').setValue(isDynamic);
                    me.getApplication().fireEvent('loadDeviceGroup', record);
                }
            }
        });
    },

    editDeviceGroup: function () {
        var me = this,
            page = me.getEditPage(),
            router = me.getController('Uni.controller.history.Router'),
            nameValue = me.getNameTextField().getValue(),
            record = me.deviceGroup,
            numberOfDevices,
            selection;

        if (!me.dynamic) {
            selection = me.getStaticGrid().getSelectionModel().getSelection();
        }

        if (nameValue == '') {
            me.getStep1FormErrorMessage().setVisible(true);
            me.getStep1FormNameErrorMessage().setVisible(false);
        } else if ((nameValue !== me.deviceGroupName) && me.nameExists()) {
            me.getStep1FormNameErrorMessage().setVisible(true);
            me.getStep1FormErrorMessage().setVisible(false);
        } else if (!me.dynamic && (selection.length == 0) && !me.getStaticGrid().allChosenByDefault) {
            me.getStep2FormErrorMessage().setVisible(true);
        } else {
            page.setLoading(true);
            record.criteriaStore.removeAll();
            record.set('name', nameValue);
            record.set('dynamic', me.dynamic);
            record.set('filter', router.filter.data);
            if (!me.dynamic) {
                var devicesList = [];
                if (me.getStaticGrid().isAllSelected()) {
                    devicesList = null;
                } else {
                    numberOfDevices = selection.length;
                    for (var i = 0; i < numberOfDevices; i++) {
                        devicesList.push(selection[i].data.id);
                    }
                }
                record.set('devices', devicesList);
            }
            record.save({
                success: function () {
                    console.info(me.fromDeviceGroupDetails);
                    if (me.fromDeviceGroupDetails) {
                        router.getRoute('devices/devicegroups/view').forward();
                    } else {
                        router.getRoute('devices/devicegroups').forward();
                    }
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceGroup.edit.success.msg', 'MDC', 'Device group edited'));
                },
                callback: function () {
                    page.setLoading(false);
                }
            });
        }
    }
});
