Ext.define('Imt.metrologyconfiguration.view.validation.RuleSetView', {
    extend: 'Ext.panel.Panel',
    xtype: 'validation-ruleset-view',
    ui: 'medium',
    padding: 0,

    requires: [
        'Imt.metrologyconfiguration.view.validation.RuleActionMenu',
        'Imt.metrologyconfiguration.view.validation.RuleSetVersionsGrid',
        'Cfg.view.validation.RulePreview',
        'Uni.view.container.PreviewContainer',
		'Imt.metrologyconfiguration.view.validation.RuleSetVersionPreview',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    validationRuleSetId: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {

                xtype: 'preview-container',
				selectByDefault: false,
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'IMT', 'No validation rule set versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'IMT', 'No validation rule set versions have been added yet.'),
                    ],
                    stepItems: [
                        {
							text: Uni.I18n.translate('validation.addValidationRulesetVersion', 'IMT', 'Add validation rule set version'),
                            privileges : Cfg.privileges.Validation.deviceConfiguration,
                            ui: 'action',
                            action: 'addValidationVersion',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/versions/add'
                        }
                    ]
                },
                grid: {
                    xtype: 'validation-versions-grid',
                    itemId: 'grd-validation-versions',
                    validationRuleSetId: me.validationRuleSetId
                },
                previewComponent: {
                    xtype: 'validation-versions-view'
                }
            }
        ];

        me.callParent(arguments);
    },

    updateValidationRuleSet: function (validationRuleSet) {
        var me = this,
            grid = me.down('validation-versions-grid'),
            addButton = me.down('button[action=addValidationVersion]'),
            href = '#/administration/validation/rulesets/' + validationRuleSet.get('id') + '/versions/add';

        me.setTitle(validationRuleSet.get('name'));
        me.validationRuleSetId = validationRuleSet.get('id');
        if (addButton.rendered) {
            addButton.setHref(href);
        } else {
            addButton.on('afterrender', function() {
                addButton.setHref(href);
            }, me, {single:true});
        }

        grid.store.load({params: {
            ruleSetId: me.validationRuleSetId
        }});
    }
});