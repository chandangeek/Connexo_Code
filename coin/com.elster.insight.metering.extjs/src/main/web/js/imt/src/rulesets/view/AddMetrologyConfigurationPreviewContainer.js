/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.view.AddMetrologyConfigurationPreviewContainer',{
    extend: 'Ext.container.Container',
    alias: 'widget.add-metrology-configuration-preview-container',
    requires: [
        'Imt.rulesets.view.AddUsagePointStatesGrid',
        'Imt.rulesets.view.MetrologyConfigurationPurposeDetails'
    ],

    usagePointStatesStore: null,

    bottomToolbar: null,

    initComponent: function(){
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                itemId: 'add-metrology-configuration-purpose-checkbox',
                defaultType: 'radiofield',
                layout: 'vbox',
                labelWidth: 200,
                fieldLabel: Uni.I18n.translate('general.usagePointStates', 'IMT', 'Usage point states'),
                items: [
                    {
                        boxLabel: Uni.I18n.translate('general.allUsagePointStates', 'IMT', 'All usage point states'),
                        name: 'states',
                        inputValue: 'all',
                        checked: true,
                        id: 'all-states'
                    },
                    {
                        boxLabel: Uni.I18n.translate('general.selectedUsagePointStates', 'IMT', 'Selected usage point states'),
                        name: 'states',
                        inputValue: 'selected',
                        id: 'selected-states',
                        listeners: {
                            change: function(fld, newValue, oldValue, eOpts){
                                var statesGrid = me.down('#add-usage-point-states-grid');
                                var statesErrorMessage = me.down('#add-usage-point-states-error');
                                if(!newValue){
                                    statesGrid.triggerAllSelection(newValue);
                                }
                                statesErrorMessage.hide();
                                statesGrid.setDisabled(!newValue);
                                statesGrid.allStatesSelected = !newValue;
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'add-usage-point-states-grid',
                itemId: 'add-usage-point-states-grid',
                disabled: true,
                bottomToolbarHidden: true,
                store: me.usagePointStatesStore,
                allStatesSelected: true,
            },
            {
                xtype: 'container',
                margin: '-20 0 10 0',
                itemId: 'add-usage-point-states-error',
                hidden: true,
                html: '<span style="color: #eb5642">' + Uni.I18n.translate('general.usagePointLifeCycle.no.stages.selected', 'IMT', 'Select at least 1 usage point stage') + '</span>'
            },
            me.bottomToolbar,
            {
                xtype: 'metrology-configuration-purpose-details',
                itemId: 'metrology-configuration-purpose-preview',
                frame: true
            }
        ];

        me.callParent(arguments);
    }
});