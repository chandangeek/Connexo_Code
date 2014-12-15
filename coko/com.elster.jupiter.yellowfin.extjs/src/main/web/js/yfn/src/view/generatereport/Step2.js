Ext.define('Yfn.view.generatereport.Step2', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step2',
    name: 'generateReportWizardStep2',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    requires: [
        'Uni.util.FormErrorMessage'
    ],


    title: Uni.I18n.translate('generatereport.wizard.step3title', 'YFN', 'Step 2 of 3:  Select report filter'),

    items: [
        {
            itemId: 'step2-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('generatereport.noDevicesSelected', 'MDC', 'Please select at least one device.')
        },
        {
            xtype: 'uni-form-info-message',
            itemId: 'info-no-fields',
            title: Uni.I18n.translate('generatereport.noReportFilters', 'YFN', 'No report filters defined.'),
            text:Uni.I18n.translate('generatereport.noReportFilter', 'YFN', 'There are no filter defined for this report. You could continue with next step'),
            hidden: true

        },
        {
            xtype: 'form',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            flex:1,
            itemId: 'filters-form',
            items:[
                {
                    xtype: 'displayfield',
                    itemId:'report-description',
                    fieldLabel: '',
                    labelCls:'x-panel-header-text-container-medium',
                    labelAlign: 'top',
                    value: ''
                },
                {
                    xtype: 'fieldcontainer',
                    itemId:'report-mandatory-filters',
                    labelCls:'x-panel-header-text-container-small',
                    labelAlign: 'top',
                    fieldLabel:Uni.I18n.translate('generatereport.wizard.mandatoryFilters', 'YFN', 'Mandatory filters'),
                    layout: 'column',
                    hidden:true,
                    listeners: {
                        after2render: function (component) {
                            new Ext.ToolTip({
                                target: component.getEl(),
                                html: Uni.I18n.translate('generatereport.wizard.mandatoryFiltersTooltip',
                                    'YFN',
                                    'Reports allow you to look up a lot of data. A mandatory filter is a minimum ' +
                                    'required selection to help narrow down the results, so to avoid performance ' +
                                    'issues when generation the report')

                            });
                        }
                    }
                },
                {
                    xtype: 'fieldcontainer',
                    labelCls:'x-panel-header-text-container-small',
                    labelAlign: 'top',
                    fieldLabel:Uni.I18n.translate('generatereport.wizard.optionalFilters', 'YFN', 'In report filters'),
                    itemId:'report-optional-filters',
                    hidden:true,
                    layout: 'column',
                    listeners: {
                        after2render: function (component) {
                            new Ext.ToolTip({
                                target: component.getEl(),
                                html: Uni.I18n.translate('generatereport.wizard.optionalFiltersTooltip', 'YFN', '')

                            });
                        }
                    }
                }
            ]

        }
    ]


});
