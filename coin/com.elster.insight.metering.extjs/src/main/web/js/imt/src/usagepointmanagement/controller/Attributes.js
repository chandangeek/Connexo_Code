Ext.define('Imt.usagepointmanagement.controller.Attributes', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.usagepointmanagement.store.PhaseCodes'
    ],

    models: [

    ],

    views: [
        'Imt.usagepointmanagement.view.Attributes',
        'Uni.view.window.Confirmation'
    ],

    refs: [
        {
            ref: 'page',
            selector: '#usage-point-attributes'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#usage-point-attributes view-edit-form': {
                save: me.saveAttributes,
                edit: me.editAttributes
            },
            '#usage-point-attributes #usage-point-attributes-actions-menu': {
                click: me.chooseAction
            }
        });
    },

    showUsagePointAttributes: function (mRID) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            dependenciesCounter = 4,
            showPage = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    mainView.setLoading(false);
                    Ext.suspendLayouts();
                    app.fireEvent('usagePointLoaded', usagePoint);
                    app.fireEvent('changecontentevent', Ext.widget('usage-point-attributes', {
                        itemId: 'usage-point-attributes',
                        router: router,
                        usagePoint: usagePoint
                    }));
                    Ext.resumeLayouts(true);
                }
            },
            usagePoint;

        mainView.setLoading();
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(showPage);
        me.getStore('Imt.usagepointmanagement.store.PhaseCodes').load(showPage);
        me.getStore('Imt.usagepointmanagement.store.BypassStatuses').load(showPage);
        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                usagePoint = record;
                showPage();
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this;

        me.editAttributes(me.getPage().down('#' + item.linkedForm));
    },

    editAttributes: function (form) {
        var me = this,
            editedForm = me.getPage().down('[displayMode=edit]'),
            confirmationWindow;

        if (!editedForm) {
            form.switchDisplayMode('edit');
        } else if (editedForm.itemId !== form.itemId) {
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.editGeneralInformation.discard', 'IMT', 'Discard'),
                confirmation: function () {
                    Ext.suspendLayouts();
                    editedForm.switchDisplayMode('view');
                    form.switchDisplayMode('edit');
                    confirmationWindow.destroy();
                    Ext.resumeLayouts(true);
                }
            });
            confirmationWindow.show({
                width: 500,
                msg: Uni.I18n.translate('general.editGeneralInformation.lostData', 'IMT', 'You will lost unsolved data.'),
                title: Uni.I18n.translate('general.editGeneralInformation.discardChanges', 'IMT', "Discard '{0}' changes?", [editedForm.title])
            });
        }
    },

    saveAttributes: function (form) {
        var me = this,
            page = me.getPage(),
            usagePoint = page.usagePoint,
            record = form.getRecord();

        switch (Ext.getClassName(record)) {
            case 'Imt.usagepointmanagement.model.UsagePoint':
                usagePoint = record;
                break;
            case 'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint':
                usagePoint.customPropertySets().add(record);
                break;
            default:
                usagePoint.set('techInfo', record.getData());
        }

        form.clearInvalid();
        page.setLoading();
        usagePoint.save({
            isNotEdit: true,
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePoint.acknowledge.updateSuccess', 'IMT', 'Usage point saved'));
            },
            failure: function (record, response) {
                var responseText = Ext.decode(response.response.responseText, true);

                if (responseText && Ext.isArray(responseText.errors)) {
                    form.markInvalid(responseText.errors);
                    record.reject();
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});