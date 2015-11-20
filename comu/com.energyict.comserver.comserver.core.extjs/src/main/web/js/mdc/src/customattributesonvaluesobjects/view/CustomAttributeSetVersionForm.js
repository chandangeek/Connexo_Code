Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.custom-attribute-set-version-form',
    itemId: 'centerContainer',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    overflowX: 'auto',
    margin: '0 20',

    requires: [
        'Uni.property.form.Property',
        'Mdc.customattributesonvaluesobjects.view.form.VersionDateField',
        'Mdc.customattributesonvaluesobjects.view.form.OverlapGrid',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsPreview',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
        'Mdc.customattributesonvaluesobjects.service.RouteMap'
    ],

    buttonText: Uni.I18n.translate('general.save', 'MDC', 'Save'),
    suspendCheckVersion: true,
    isForcedSave: false,

    initComponent: function () {
        var me = this,
            versionRoute = Mdc.customattributesonvaluesobjects.service.RouteMap.getRoute(me.type, true, 'version'),
            saveBtnText;

        me.backUrl = me.router.getRoute(versionRoute).buildUrl();
        me.savedStartDate = null;
        me.savedStartDate = null;
        me.recordToSave = null;

        switch (me.pageType) {
            case 'add':
                saveBtnText = Uni.I18n.translate('general.add', 'MDC', 'Add');
                me.successAcknowledgement = Uni.I18n.translate('general.version.added', 'MDC', 'Version added');
                break;
            case 'edit':
                saveBtnText = Uni.I18n.translate('general.save', 'MDC', 'Save');
                me.successAcknowledgement = Uni.I18n.translate('general.version.saved', 'MDC', 'Version saved');
                break;
            case 'clone':
                saveBtnText = Uni.I18n.translate('general.clone', 'MDC', 'Clone');
                me.successAcknowledgement = Uni.I18n.translate('general.version.cloned', 'MDC', 'Version cloned');
                break;
        }

        me.items = [
            {
                xtype: 'form',
                ui: 'large',
                title: me.router.getRoute().getTitle(),
                width: 650,
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'errors',
                        hidden: true,
                        width: 600,
                        margin: '0 0 0 0'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.start', 'MDC', 'Start'),
                        groupName: 'startGroup',
                        xtype: 'custom-attributes-version-date-field',
                        itemId: 'custom-attribute-set-version-start-date-field'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.start', 'MDC', 'End'),
                        groupName: 'endGroup',
                        xtype: 'custom-attributes-version-date-field',
                        itemId: 'custom-attribute-set-version-end-date-field'
                    },
                    {
                        margin: '20 0 0 0',
                        xtype: 'property-form',
                        itemId: 'custom-attribute-set-version-property-form-id'
                    },
                    {
                        xtype: 'container',
                        margin: '20 0 0 265',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },

                        items: [
                            {
                                xtype: 'button',
                                itemId: 'custom-attributes-versions-save-btn',
                                ui: 'action',
                                text: saveBtnText,
                                handler: function () {
                                    me.saveRecord();
                                }
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.restoretodefaults', 'MDC', 'Restore to defaults'),
                                icon: '../sky/build/resources/images/form/restore.png',
                                itemId: 'custom-attributes-versions-restore-to-default-btn',
                                handler: function () {
                                    me.restoreDefaultCustomAttributes();
                                }
                            },
                            {
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'custom-attributes-versions-cancel-btn',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                handler: function () {
                                    me.fireEvent('moveToVersionsPage', me.type);
                                }
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'overlap-grid-field-container',
                margin: '40 0 0 0',
                hidden: true,
                width: 900,
                items: [
                    {
                        xtype: 'custom-attribute-set-versions-overlap-grid',
                        store: me.overlapStore
                    },
                    {
                        xtype: 'custom-attribute-set-versions-preview',
                        flex: 1,
                        hideAction: true
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (recordToLoad, recordToSave, startTime, endTime) {
        var me = this;

        recordToSave.set('parent', recordToLoad.get('parent'));
        recordToSave.set('version', recordToLoad.get('version'));
        me.recordToSave = recordToSave;
        me.down('#custom-attribute-set-version-property-form-id').loadRecord(recordToLoad);
        me.setDateValues(startTime, endTime);
    },

    setDateValues: function (startdate, enddate) {
        var me = this,
            startDateField = me.down('#custom-attribute-set-version-start-date-field'),
            endDateField = me.down('#custom-attribute-set-version-end-date-field'),
            onChangeFunc;

        onChangeFunc = function () {
            if (!me.suspendCheckVersion) {
                me.checkRecord();
            }
        };

        startDateField.setValue(startdate);
        endDateField.setValue(enddate);
        startDateField.on('change', onChangeFunc);
        endDateField.on('change', onChangeFunc);
    },

    saveRecord: function () {
        var me = this,
            record = me.recordToSave,
            propertyForm = me.down('#custom-attribute-set-version-property-form-id'),
            overlapContainer = me.down('#overlap-grid-field-container'),
            startDateField = me.down('#custom-attribute-set-version-start-date-field'),
            endDateField = me.down('#custom-attribute-set-version-end-date-field'),
            startDate = startDateField.getValue(),
            endDate = endDateField.getValue();

        Ext.suspendLayouts();
        me.setLoading(true);
        me.savedStartDate = startDate;
        me.savedEndDate = endDate;
        me.down('uni-form-error-message').hide();
        startDateField.enable();
        endDateField.enable();
        startDateField.clearInvalid();
        endDateField.clearInvalid();
        propertyForm.updateRecord();
        record.propertiesStore = propertyForm.getRecord().properties();
        record.set('startTime', startDate);
        record.set('endTime', endDate);
        overlapContainer.hide();
        Ext.resumeLayouts(true);

        record.save({
            backUrl: me.backUrl,
            params: {
                'forced': me.isForcedSave
            },
            success: function () {
                me.fireEvent('moveToVersionsPage', me.type, true, me.successAcknowledgement);
            },
            failure: function (record, meta) {
                var response = Ext.decode(meta.response.responseText);

                me.down('uni-form-error-message').show();
                if (response.errors && response.errors.length > 0) {
                    me.isForcedSave = false;
                    Ext.each(response.errors, function (error) {
                        switch (error.id) {
                            case 'startTime':
                                startDateField.markInvalid(error.msg);
                                break;
                            case 'endTime':
                                endDateField.markInvalid(error.msg);
                                break;
                        }
                    });
                } else {
                    Ext.suspendLayouts();
                    overlapContainer.show();
                    startDateField.disable();
                    endDateField.disable();
                    me.isForcedSave = true;
                    me.suspendCheckVersion = false;
                    Ext.resumeLayouts(true);
                    me.checkRecord();
                }
            },
            callback: function () {
                me.setLoading(false);
            }
        });
    },

    checkRecord: function () {
        var me = this,
            overlapGrid = me.down('#custom-attribute-set-versions-overlap-grid-id'),
            overlapStore = overlapGrid.getStore(),
            overlapContainer = me.down('#overlap-grid-field-container'),
            startDateField = me.down('#custom-attribute-set-version-start-date-field'),
            endDateField = me.down('#custom-attribute-set-version-end-date-field'),
            startTime = startDateField.getValue(),
            endTime = endDateField.getValue();

        if (!startTime || !endTime || startTime < endTime ) {
            overlapStore.load({
                params: {
                    'startTime': startTime,
                    'endTime': endTime
                },
                callback: function () {
                    if (this.getCount() === 0) {
                        Ext.suspendLayouts();
                        overlapContainer.hide();
                        me.down('uni-form-error-message').hide();
                        startDateField.enable();
                        endDateField.enable();
                        me.isForcedSave = false;
                        me.suspendCheckVersion = true;
                        Ext.resumeLayouts(true);
                    }
                }
            });
        } else {
            me.fireEvent('exceedstime');
        }
    },

    restoreDefaultCustomAttributes: function () {
        this.down('#custom-attribute-set-version-property-form-id').restoreAll();
    }
});