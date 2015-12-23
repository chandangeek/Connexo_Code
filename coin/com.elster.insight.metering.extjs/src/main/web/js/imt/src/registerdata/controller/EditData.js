Ext.define('Imt.registerdata.controller.EditData', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.registerdata.view.RegisterDataEdit',
        'Imt.registerdata.view.RegisterDataList',
    ],

    refs: [
        {ref: 'registerDataList', selector: '#registerDataList'},
        {ref: 'registerDataEdit', selector: '#registerDataEdit'},
        {ref: 'registerDataEditForm', selector: '#registerDataEditForm'}
    ],

    init: function () {
        var me = this;

        me.control({
//            'menu menuitem[action=editData]': {
//                click: me.showRegisterDataEditView2 //me.editRegisterDataHistory
//            },
//            'menu menuitem[action=removeData]': {
//                click: me.removeRegisterData
//            },
            '#addEditButton[action=addRegisterDataAction]': {
                click: me.addRegisterData
            },
            '#addEditButton[action=editRegisterDataAction]': {
                click: me.editRegisterData
            },
        });
    },

	showRegisterDataAddView: function (mRID, registerId) {
	  var me = this,
	      contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
	      router = me.getController('Uni.controller.history.Router');
	
	  contentPanel.setLoading(true);
	  Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
	      success: function (usagepoint) {
	          me.getApplication().fireEvent('loadUsagePoint', usagepoint);
	          var model = Ext.ModelManager.getModel('Imt.registerdata.model.Register');
	          model.getProxy().setExtraParam('mRID', encodeURIComponent(mRID));
	          model.load(registerId, {
	              success: function (register) {
	                  var widget = Ext.widget('registerDataEdit', {
	                      edit: false,
	                      returnLink: router.getRoute('usagepoints/view/registers/register').buildUrl({mRID: encodeURIComponent(mRID), registerId: registerId}),
	                      mRID: mRID,
	                      registerId: registerId,
	                      router: router
	                  });
	                  widget.setValues(register);
	                  me.getApplication().fireEvent('loadRegister', register);
	                  me.getApplication().fireEvent('changecontentevent', widget);
	                  widget.down('#stepsMenu #editReading').setText(Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'));
	              },
	
	              callback: function () {
	                  contentPanel.setLoading(false);
	              }
	          });
	      }
	  });
	},
    showRegisterDataEditView2: function(mrid, registerid) {
    	alert('blahblahblah');	
    },
	showRegisterDataEditView: function (mRID, registerId, timestamp) {
	    var me = this,
	    	registerModel = me.getModel('Imt.registerdata.model.Register'),
	      contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
	      router = me.getController('Uni.controller.history.Router');
	
	    contentPanel.setLoading(true);
	    Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
	      success: function (usagepoint) {
	          me.getApplication().fireEvent('loadUsagePoint', usagepoint);
	          registerModel.getProxy().setUrl({mRID: mRID, registerId: registerId});
	 	      registerModel.load(registerId, {
	             success: function (register) {
	                  model = Ext.ModelManager.getModel('Imt.registerdata.model.RegisterData');
	                 // model.getProxy().extraParams = ({mRID: mRID, registerId: registerId});
	                  model.getProxy().setUrl({mRID: mRID, registerId: registerId});
	                  model.load(timestamp, {
	                      success: function (reading) {
	                          var widget = Ext.widget('registerDataEdit', {
	                              edit: true,
	                              returnLink: router.getRoute('usagepoints/view/registers/register').buildUrl({mRID: encodeURIComponent(mRID), registerId: registerId}),
	                              router: router
	                          });
	                          me.getApplication().fireEvent('loadRegister', register);
	                          widget.down('form').loadRecord(reading);
	                          widget.setValues(register);
	                          me.getApplication().fireEvent('changecontentevent', widget);
	                          widget.down('#stepsMenu').setTitle(Ext.util.Format.date(new Date(reading.get('readingTime')), 'M j, Y \\a\\t G:i'));
	                          widget.down('#stepsMenu #editReading').setText(Uni.I18n.translate('registerdata.editReading', 'IMT', 'Edit reading'));
	                      },
	
	                      callback: function () {
	                          contentPanel.setLoading(false);
	                      }
	                  });
	              }
	          });
	      }
	  });
	},
	
    addRegisterData: function () {
        var me = this;
        me.setPreLoader(me.getRegisterDataEdit(), Uni.I18n.translate('registerdata.creating', 'IMT', 'Creating register data'));
        me.updateRegisterData('add');
    },

    editRegisterData: function () {
        var me = this;

        me.setPreLoader(me.getRegisterDataEdit(), Uni.I18n.translate('registerdata.updating', 'IMT', 'Updating register data'));
        me.updateRegisterData('edit');
    },

    updateRegisterData: function (operation) {
        var me = this,
            form = me.getRegisterDataEdit().down('#registerDataEditForm');
        if (form.isValid()) {
            me.hideErrorPanel();
            me[operation + 'RegisterDataRecord'](form.getValues(), {operation: operation});
        } else {
            me.clearPreLoader();
            me.showErrorPanel();
        }
    },

    editRegisterDataRecord: function (values, cfg) {
        var me = this,
            record = me.getRegisterDataEdit().down('#registerDataEditForm').getRecord();
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('registerdata.updated', 'IMT', 'Register data saved')
        }, cfg));
    },

    addRegisterDataRecord: function (values, cfg) {
        var me = this,
            record = Ext.create('Imt.registerdata.model.RegisterData');
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('registerdata.created', 'IMT', 'Register data saved')
        }, cfg));
    },

    removeRegisterData: function () {
  //  	alert('remove register data');
        var me = this,
            grid = me.getRegisterDataList(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Ext.String.format(Uni.I18n.translate('registerdata.delete.message', 'IMT', 'The register reading with measurement time {0} will no longer be available'), Ext.util.Format.date(new Date(lastSelected.get('readingTime')), 'M j, Y \\a\\t G:i')),
            title: Uni.I18n.translate('registerdata.delete.title.question', 'IMT', 'Remove the reading?'),
            config: {
                readingToDelete: lastSelected,
                me: me
            },
            fn: me.removeRegisterDataRecord
        });
    },

    removeRegisterDataRecord: function (btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                readingToDelete = cfg.config.readingToDelete,
                router = me.getController('Uni.controller.history.Router');
//                type = readingToDelete.get("type"),
//                dataStore = me.getStore(me.getReadingTypePrefix(type));

  //          readingToDelete.getProxy().extraParams = ({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
            readingToDelete.getProxy().setUrl({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
            readingToDelete.destroy({
                callback: function (record, operation) {
                    if (operation.wasSuccessful()) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerdata.removed', 'IMT', 'Register data successfully removed'));
                        router.getRoute('usagepoints/view/registers/register').forward();
//                        dataStore.load();
                    }
                }
            });
        }
    },

//    getReadingTypePrefix: function (type) {
//        if (!Ext.isEmpty(type)) {
//            return (type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData');
//        }
//        return 'RegisterData';
//    },
//
//    getReadingModelClassByType: function (type) {
//        var me = this;
//
//        return ('Mdc.model.' + me.getReadingTypePrefix(type));
//    },
//
//    getReadingModelInstanceByType: function (type) {
//        var me = this,
//            modelClass = me.getReadingModelClassByType(type),
//            record = Ext.create(modelClass);
//
//        record.set("type", type);
//
//        return record;
//    },

    updateRecord: function (record, values, cfg) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (record) {
            me.setRecordValues(record, values);
            //record.getProxy().extraParams = ({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
            record.getProxy().setUrl({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
            record.save({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', cfg.successMessage);
                    router.getRoute('usagepoints/view/registers/register').forward();
                   // router.getRoute('usagepoints/view/registers/register').buildUrl({mRID: encodeURIComponent(mRID), registerId: registerId}),
                },
                failure: function (record, resp) {
                    var response = resp.response;
                    if (response.status == 400) {
                        var responseText = Ext.decode(response.responseText, true);
                        if (responseText && responseText.errors) {
                            me.getRegisterDataEditForm().getForm().markInvalid(responseText.errors);
                            me.showErrorPanel();
                        }
                    }
                },
                callback: function () {
                    me.clearPreLoader();
                }
            });
        }
    },

    setRecordValues: function (record, values) {
        if (!Ext.isEmpty(values.value)) {
            record.data.value = values.value;
        }
        record.set("readingTime", values.readingTime);

        record.get('isConfirmed') && record.set('isConfirmed', false);
    },

    editRegisterDataHistory: function () {
        var me = this,
            grid = me.getRegisterDataList(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('usagepoints/registers/edit').forward({timestamp: lastSelected.getData().readingTime});
    },




    setPreLoader: function (target, message) {
        var me = this;
        me.preloader = Ext.create('Ext.LoadMask', {
            msg: message,
            target: target
        });
        me.preloader.show();
    },

    clearPreLoader: function () {
        var me = this;
        if (!Ext.isEmpty(me.preloader)) {
            me.preloader.destroy();
            me.preloader = null;
        }
    },

    showErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getRegisterDataEdit().down('#registerDataEditForm #registerDataEditFormErrors');

        formErrorsPlaceHolder.hide();
        Ext.suspendLayouts();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('general.formErrors', 'IMT', 'There are errors on this page that require your attention.')
        });
        Ext.resumeLayouts();
        formErrorsPlaceHolder.show();
    },

    hideErrorPanel: function () {
        var me = this,
            formErrorsPlaceHolder = me.getRegisterDataEdit().down('#registerDataEditForm #registerDataEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
    }
});

