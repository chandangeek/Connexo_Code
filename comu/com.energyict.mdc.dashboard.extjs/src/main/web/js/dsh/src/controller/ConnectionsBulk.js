Ext.define('Dsh.controller.ConnectionsBulk', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dsh.store.ConnectionTasksBuffered',
        'Dsh.store.ConnectionTaskProperties'
    ],

    views: [
        'Dsh.view.connectionsbulk.Browse'
    ],

    config: {
        connectionType: '',
        properties: null
    },

    refs: [
        {
            ref: 'wizard',
            selector: 'connections-bulk-browse #connections-bulk-wizard'
        },
        {
            ref: 'navigation',
            selector: 'connections-bulk-browse #connections-bulk-navigation'
        }
    ],

    nextBtnCounter: 0,

    init: function () {
        this.control({
            'connections-bulk-browse connections-bulk-wizard button[action=step-next]': {
                click: this.moveTo
            },
            'connections-bulk-browse connections-bulk-wizard button[action=step-back]': {
                click: this.moveTo
            },
            'connections-bulk-browse connections-bulk-wizard button[action=confirm-action]': {
                click: this.moveTo
            },
            'connections-bulk-browse #connections-bulk-navigation': {
                movetostep: this.moveTo
            }
        });
    },

    showOverview: function () {
        var me = this,
            connectionTasksBufferedStore = me.getStore('Dsh.store.ConnectionTasksBuffered');

        this.getApplication().fireEvent('changecontentevent', Ext.widget('connections-bulk-browse', {
            router: me.getController('Uni.controller.history.Router')
        }));

        connectionTasksBufferedStore.data.clear();
        me.setLoading(true);
        connectionTasksBufferedStore.on({
            load: {fn: this.onStoreLoaded, scope: this, single: true}
        });
        connectionTasksBufferedStore.loadPage(1, {
            params: {
                filter : me.getFilterObjectStringFromQueryString()
            }
        });
    },

    onStoreLoaded: function(records, operation, success) {
        this.setLoading(false);
        this.setConnectionType(success && records.data.first ? records.data.first.value[0].data.connectionType : '');
    },

    setLoading: function(loadingState) {
        this.getWizard().setLoading(loadingState);
    },

    doRequest: function () {
        var me = this,
            wizard = me.getWizard(),
            selectionGrid = wizard.down('bulk-selection-grid'),
            action = wizard.down('#connections-bulk-action-radiogroup').getValue().action,
            data = {},
            url;

        if (selectionGrid.isAllSelected()) {
            var filterItems = me.getFilterObjectFromQueryString();
            data.filter = {};
            for (var dataIndex in filterItems) {
                var value = filterItems[dataIndex];
                if (filterItems.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                    data.filter[dataIndex] = value;
                }
            }
        } else {
            data.connections = [];
            Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                data.connections.push(item.getId());
            });
        }

        switch (action) {
            case 'runNow':
                url = '/api/dsr/connections/run';
                break;

            case 'adjustAttributes':
                url = '/api/dsr/connections/properties';
                data.properties = [];
                me.getProperties().properties().each(function(property) {
                    if (property.isEdited) {
                        var propertyToPass = {};
                        propertyToPass.key = property.data.key;
                        propertyToPass.required = property.data.required;
                        propertyToPass.propertyTypeInfo = property.raw.propertyTypeInfo;
                        propertyToPass.propertyValueInfo = property.raw.propertyValueInfo;
                        propertyToPass.propertyValueInfo.value = property.data.value;
                        data.properties.push(propertyToPass);
                    }
                });
                break;
        }

        wizard.setLoading(true);

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: data,
            callback: function (options, success) {
                if (wizard.rendered) {
                    wizard.setLoading(false);
                    wizard.down('#cnbw-step5').setResultMessage(action, success);
                }
            }
        });
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getWizard().getLayout(),
            currentStep = parseInt(wizardLayout.getActiveItem().getItemId().replace('cnbw-step', '')),
            direction,
            nextStep;

        if (button.action === 'step-next' || button.action === 'confirm-action') {
            direction = 1;
            nextStep = currentStep + direction;
            if (currentStep === 2 && me.wasRunNowChosen()) {
                nextStep++;
            }
        } else {
            direction = -1;
            if (button.action === 'step-back') {
                nextStep = currentStep + direction;
                if (currentStep === 4 && me.wasRunNowChosen()) {
                    nextStep--;
                }
            } else {
                nextStep = button;
            }
        }

        if (me.wasAdjustAttributesChosen() && currentStep === 2 && direction === 1) {
            me.validateCurrentStep(currentStep); /* and doMove() will be triggered by a callback function */
        } else {
            me.doMove(currentStep, nextStep, direction > 0);
        }
    },

    doMove: function(currentStep, nextStep, validate) {
        var me = this,
            wizardLayout = me.getWizard().getLayout();

        Ext.suspendLayouts();

        if (validate) {
            if (!me.validateCurrentStep(currentStep)) {
                Ext.resumeLayouts(true);
                return
            }
        }

        me.prepareNextStep(currentStep, nextStep);
        wizardLayout.setActiveItem(nextStep - 1);
        me.getNavigation().moveToStep(nextStep);
        if (nextStep === 4 && me.wasRunNowChosen()) {
            me.getNavigation().makeStepXCompletedAndUnclickable(3);
        }

        Ext.resumeLayouts(true);
    },

    validateCurrentStep: function (stepNumber) {
        var me = this,
            valid = true,
            step1View,
            selectionGrid,
            connectionPropertiesStore = me.getStore('Dsh.store.ConnectionTaskProperties'),
            connections;

        switch (stepNumber) {
            case 1:
                step1View = me.getWizard().down('#cnbw-step1');
                selectionGrid = step1View.down('bulk-selection-grid');
                valid = !(!selectionGrid.isAllSelected() && !selectionGrid.getSelectionModel().getSelection().length);
                step1View.down('#step1-error-message').setVisible(!valid);
                step1View.down('#selection-grid-error').setVisible(!valid);
                break;
            case 2:
                step1View = me.getWizard().down('#cnbw-step1');
                selectionGrid = step1View.down('bulk-selection-grid');
                if (me.wasAdjustAttributesChosen()) {
                    me.setLoading(true);
                    if (selectionGrid.isAllSelected()) {
                        connectionPropertiesStore.load({
                            params: {
                                filter : me.getFilterObjectStringFromQueryString()
                            },
                            callback: function (records, operation, success) {
                                me.setLoading(false);
                                if (success) {
                                    me.setProperties(records[0]);
                                    me.doMove(stepNumber, stepNumber + 1, false);
                                }
                            },
                            single: true
                        });
                    } else {
                        connectionPropertiesStore.on('beforeload', function(store, operation) {
                            delete operation.filters; // remove the filters parameter that is automatically added
                        }, me, {single: true});

                        connections = [];
                        Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                            connections.push(item.getId());
                        });

                        connectionPropertiesStore.load({
                            params: {
                                connections: Ext.encode(connections)
                            },
                            callback: function (records, operation, success) {
                                me.setLoading(false);
                                if (success) {
                                    me.setProperties(records[0]);
                                    me.doMove(stepNumber, stepNumber + 1, false);
                                }
                            },
                            single: true
                        });
                    }
                }
                break;
        }
        return valid;
    },

    prepareNextStep: function (currentStep, nextStep) {
        var me = this,
            wizard = me.getWizard(),
            buttons = wizard.getDockedComponent('cnbw-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm-action]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]');

        nextBtn.enable();
        backBtn.enable();
        switch (nextStep) {
            case 1:
                nextBtn.show();
                backBtn.show();
                backBtn.disable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 2:
                nextBtn.show();
                backBtn.show();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 3:
                wizard.down('#cnbw-step3').setConnectionType(me.getConnectionType());
                wizard.down('#cnbw-step3').setProperties(me.getProperties());
                me.passOnEditFunc();
                me.nextBtnCounter = 0;
                nextBtn.show();
                nextBtn.disable();
                backBtn.show();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 4:
                wizard.down('#cnbw-step4').setConfirmationMessage(wizard.down('#connections-bulk-action-radiogroup').getValue().action);
                if (currentStep === 3) {
                    wizard.down('#cnbw-step3').updateRecord(); // So that the values are still there when pressing 'Back'
                    wizard.down('#cnbw-step4').setProperties(me.getProperties());
                }
                nextBtn.hide();
                backBtn.show();
                confirmBtn.show();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 5:
                me.doRequest();
                me.getNavigation().jumpBack = false;
                nextBtn.hide();
                backBtn.hide();
                confirmBtn.hide();
                finishBtn.show();
                cancelBtn.hide();
                break;
        }
    },

    wasRunNowChosen: function() {
        return 'runNow' === this.getWizard().down('#connections-bulk-action-radiogroup').getValue().action;
    },

    wasAdjustAttributesChosen: function() {
        return 'adjustAttributes' === this.getWizard().down('#connections-bulk-action-radiogroup').getValue().action;
    },

    passOnEditFunc: function() {
        var me = this,
            form = me.getWizard().down('#cnbw-step3 #dsh-connections-bulk-attributes-form');
        if (form) {
            form.getRecord().properties().each(function (property) {
                var editor = form.getPropertyField(property.get('key')); // Uni.property.view.property.Base
                editor.onEditFunc = {
                    func: me.onPropEdit,
                    scope: me
                }
            });
        }
    },

    onPropEdit: function(para, scope) {
        var me = scope,
            wizard = me.getWizard(),
            buttons = wizard.getDockedComponent('cnbw-buttons'),
            nextBtn = buttons.down('[action=step-next]');

        if (para>0) {
            me.nextBtnCounter++;
        } else if (para<0) {
            me.nextBtnCounter--;
        }
        if (me.nextBtnCounter === 0) {
            nextBtn.disable();
        } else {
            nextBtn.enable();
        }
    },

    getFilterObjectStringFromQueryString: function() {
        var filterObject = this.getFilterObjectFromQueryString(),
            result = [];

        for (var dataIndex in filterObject) {
            var value = filterObject[dataIndex];

            if (filterObject.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                var filter = {
                    property: dataIndex,
                    value: value
                };
                result.push(filter);
            }
        }
        return Ext.encode(result);
    },

    getFilterObjectFromQueryString: function() {
        var filterObject = Uni.util.QueryString.getQueryStringValues(false);
        this.adaptFilterObject(filterObject);
        return filterObject;
    },

    adaptFilterObject: function(filterObject) {
        // Assure that properties that are expected to be an int array, are indeed int arrays
        var props = ['deviceTypes', 'deviceGroups', 'comPortPools', 'connectionTypes'];
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