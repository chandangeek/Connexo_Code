Ext.define('Imt.registerdata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.registerdata.store.Register',
        'Imt.registerdata.view.Setup',
        'Imt.registerdata.view.Preview',
        'Imt.registerdata.view.ActionMenu'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.registerdata.model.Register',
    ],
    stores: [
        'Imt.registerdata.store.Register',
    ],
    views: [
          'Imt.registerdata.view.RegisterList',
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'registerList', selector: '#registerList'},
        {ref: 'registerListSetup', selector: '#registerListSetup'},
        {ref: 'registerPreview', selector: '#registerPreview'},
        {ref: 'registerActionMenu', selector: '#registerActionMenu'},
        {ref: 'registerPreview', selector: 'tabbedRegisterView'}
    ],
    fromSpecification: false,
    init: function () {
        var me = this;
        me.control({
            '#registerList': {
                select: me.onRegisterListSelect
            },
            '#registerActionMenu': {
                click: me.chooseAction
            },
        });
    },
    showUsagePointRegisters: function (mRID) {
        var me = this,
	        router = me.getController('Uni.controller.history.Router'),
	        registerStore = me.getStore('Imt.registerdata.store.Register'),
	        usagePoint = Ext.create('Imt.usagepointmanagement.model.UsagePoint'),
	        pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        me.mRID = mRID;
	    pageMainContent.setLoading(true);
	    Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
	        success: function (usagepoint) {
	        	var widget = Ext.widget('registerListSetup', {
	        		router: router, 
	        		mRID: mRID,
	        		usagepoint: usagepoint
	        	});
	        	me.getApplication().fireEvent('usagePointLoaded', usagepoint);
	        	me.getApplication().fireEvent('changecontentevent', widget);
	        	registerStore.load(function() {	
	        		router.arguments.version = usagepoint.get('version');
	      	        me.getRegisterList().getSelectionModel().select(0);
	      	        pageMainContent.setLoading(false);
	      	    });
	        	me.getOverviewLink().setText(mRID);
	        }
	    });
	    registerStore.getProxy().setUrl(mRID);
    },
    onRegisterListSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewRegisterData(record);
    },

    previewRegisterData: function (record) {
        var me = this,
        	router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('registerPreview', {router: router}), 
            form = widget.down('#registerPreviewForm'),
            previewContainer = me.getRegisterListSetup().down('#previewComponentContainer');
        
        form.loadRecord(record);
        widget.tools[0].menu.record=record;
        widget.setTitle(record.get('readingTypeFullAliasName'));
        previewContainer.removeAll();
        previewContainer.add(widget);    
    },
    chooseAction: function (menu, item) {
        var me = this,
        router = this.getController('Uni.controller.history.Router'),
        routeParams = router.arguments,
        route,
        filterParams = {};
        router.arguments.registerId = menu.record.get('id');
	    switch (item.action) {
	        case 'validateNow':
	        	this.fromSpecification = false;
	            me.showValidateNowMessage(menu.record);
	            break;
	        case 'viewSuspects':
	        	this.fromSpecification = false;
	            filterParams.suspect = 'suspect';
	            route = 'usagepoints/view/registers/registerdata'; 
	            break;
	    }
	
	    route && (route = router.getRoute(route));
	        route && route.forward(routeParams, filterParams); 
    },
    showValidateNowMessage: function (record) {
    	var me = this,
    	router = me.getController('Uni.controller.history.Router'),
        mRID = me.mRID ? me.mRID : router.arguments.mRID;

        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowRegisterConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'IMT', 'Validate'),
                confirmation: function () {
                    me.activateDataValidation(record, this);
                }
            });
        Ext.Ajax.request({
        	url: '../../api/udr/usagepoints/' + encodeURIComponent(mRID) + '/validationrulesets/validationstatus',
        	method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (res.hasValidation) {
                    if (res.lastChecked) {
                        me.dataValidationLastChecked = new Date(res.lastChecked);
                    } else {
                        me.dataValidationLastChecked = new Date();
                    }
                    confirmationWindow.insert(1, me.getValidationContent());
                    confirmationWindow.show({
                        title: Uni.I18n.translate('registerdata.validation.validateNow', 'IMT', 'Validate data of register configuration {0}?', [record.get('name')]),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translate('registerdata.validateNow.error', 'IMT', 'Failed to validate data of register configuration {0}', [record.get('name')]),
                        message = Uni.I18n.translate('registerdata.validation.noData', 'IMT', 'There is currently no data for this register configuration'),
                        config = {
                            icon: Ext.MessageBox.WARNING
                        };
                    me.getApplication().getController('Uni.controller.Error').showError(title, message, config);
                }
            }
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },
    activateDataValidation: function (record, confWindow) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        record.data.version = router.arguments.version;
        if (confWindow.down('#validateRegisterFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateRegisterDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'IMT', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateRegisterDateErrors').setVisible(true);
        } else {
            confWindow.down('button').setDisabled(true);
            Ext.Ajax.request({
                url: '../../api/udr/usagepoints/' + encodeURIComponent(me.mRID) + '/registers/' + record.get('id') + '/validate',
                method: 'PUT',
                isNotEdit: true,
                jsonData: Ext.merge({
                    lastChecked: confWindow.down('#validateRegisterFromDate').getValue().getTime(),
                }, _.pick(record.getData(), 'id', 'name', 'version')),
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('registerdata.validation.completed', 'IMT', 'Data validation completed'));
                    router.getRoute().forward();
                },
                failure: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                }
            });

        }
    },

    getValidationContent: function () {
        var me = this;
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left',
                labelStyle: 'font-weight: normal; padding-left: 50px'
            },
            items: [
                {
                    xtype: 'datefield',
                    itemId: 'validateRegisterFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('registerdata.validation.item1', 'IMT', 'The data of register configuration will be validated starting from'),
                    labelWidth: 375,
                    labelPad: 0.5
                },
                {
                    xtype: 'panel',
                    itemId: 'validateRegisterDateErrors',
                    hidden: true,
                    bodyStyle: {
                        color: '#eb5642',
                        padding: '0 0 15px 65px'
                    },
                    html: ''
                },
                {
                    xtype: 'displayfield',
                    value: '',
                    fieldLabel: Uni.I18n.translate('registerdata.validateNow.item2', 'IMT', 'Note: The date displayed by default is the last checked (the moment when the last interval was checked in the validation process).'),
                    labelWidth: 500
                }
            ]
        });
    },
    showRegisterDetailsView: function (mRID, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registerStore = me.getStore('Register');
        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (usagepoint) {
                me.getApplication().fireEvent('loadUsagePoint', usagepoint);
                var model = Ext.ModelManager.getModel('Imt.registerdata.model.Register');
                model.getProxy().setExtraParam('mRID', encodeURIComponent(mRID));
                model.load(registerId, {
                    success: function (register) {
                     //   var type = register.get('type');
                        var widget = Ext.widget('tabbedRegisterView', {
                            usagepoint: usagepoint,
                            router: me.getController('Uni.controller.history.Router')
                        });
                        var func = function () {
//                            var customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.RegisterCustomAttributeSets');
//                            customAttributesStore.getProxy().setUrl(mRID, registerId);
//                            customAttributesStore.load(function () {
//                                widget.down('#custom-attribute-sets-placeholder-form-id').loadStore(customAttributesStore);
//                            });
                            me.getApplication().fireEvent('changecontentevent', widget);
                            widget.down('#registerTabPanel').setTitle(register.get('readingType').fullAliasName);
                            var config = Ext.widget('registerPreview', {
                                mRID: encodeURIComponent(mRID),
                                registerId: registerId,
                                router: me.getController('Uni.controller.history.Router')
                            });
                            var form = config.down('#registerPreviewForm');
                            me.getApplication().fireEvent('loadRegister', register);
                            form.loadRecord(register);
                            if (!register.data.validationInfo.validationActive) {
                                config.down('#validateNowRegister').hide();
                            }
                            config.down('#registerActionMenu').record = register;
                            widget.down('#register-specifications').add(config);
                        };
                        if (registerStore.getTotalCount() === 0) {
                            registerStore.getProxy().url = registerStore.getProxy().url.replace('{mRID}', encodeURIComponent(mRID));
                            registerStore.load(function () {
                                func();
                            });
                        } else {
                            func();
                        }
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                        tabController.showTab(0);
                    }
                });
            }
        });
    },
});

