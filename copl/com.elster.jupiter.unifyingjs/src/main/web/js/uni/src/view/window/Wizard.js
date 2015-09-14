/**
 * @class Uni.view.window.Wizard
 */
Ext.define('Uni.view.window.Wizard', {
    extend: 'Ext.window.Window',
    constrain: true,

    requires: [
        'Ext.layout.container.Card',
        'Uni.view.navigation.SubMenu'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    minWidth: 400,
    minHeight: 200,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the wizard steps. AN array of components is expected.
     */
    steps: null,

    /**
     * @cfg {String}
     *
     * Configuration of the wizard title.
     */
    title: '',

    /**
     * @cfg {String/Ext.Component}
     *
     * Description text or component that goes below the wizard title.
     */
    description: {
        xtype: 'component',
        html: ''
    },

    items: [
        {
            // Title and description
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    itemId: 'wizardTitle',
                    html: ''
                },
                {
                    xtype: 'container',
                    itemId: 'wizardDescription',
                    html: ''
                }
            ]
        },
        {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'vbox',
                        align: 'stretchmax'
                    },
                    items: [
                        {
                            xtype: 'component',
                            html: '<h3>' + Uni.I18n.translate('window.wizard.menu.title', 'UNI', 'Steps') + '</h3>'
                        },
                        {
                            xtype: 'navigationSubMenu',
                            itemId: 'stepsMenu'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'stepsTitle',
                            html: '&nbsp;'
                        },
                        {
                            xtype: 'container',
                            itemId: 'stepsContainer',
                            layout: 'card',
                            flex: 1,
                            items: []
                        }
                    ]
                }

            ]
        }
    ],

    bbar: [
        {
            xtype: 'component',
            flex: 1
        },
        {
            text: Uni.I18n.translate('window.wizard.tools.previous', 'UNI', '&laquo; Previous'),
            action: 'prev',
            scope: this,
            handler: this.prevStep,
            disabled: true
        },
        {
            text: Uni.I18n.translate('window.wizard.tools.next', 'UNI', 'Next &raquo;'),
            action: 'next',
            scope: this,
            handler: this.nextStep,
            disabled: true
        },
        {
            text: Uni.I18n.translate('window.wizard.tools.finish', 'UNI', 'Finish'),
            action: 'finish'
        },
        {
            text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
            action: 'cancel'
        }
    ],

    initComponent: function () {
        var steps = this.steps;

        if (steps) {
            if (!(steps instanceof Ext.Component)) {
                // Never modify a passed config object, that could break the expectations of the using code.
                steps = Ext.clone(steps);
            }

            // Needs to be mapped to the non-rendered config object.
            this.items[1].items[1].items[1].items = steps;
        }

        this.callParent(arguments);
        this.addCls(Uni.About.baseCssPrefix + 'window-wizard');

        this.setTitle(this.title);
        this.setDescription(this.description);

        if (steps) {
            this.initStepsMenu(steps);
        }

        this.initNavigation();
    },

    initStepsMenu: function (steps) {
        var me = this,
            stepsMenu = this.getStepsMenuCmp();

        Ext.suspendLayouts();

        for (var i = 0; i < steps.length; i++) {
            var step = steps[i];

            var stepButton = stepsMenu.add({
                text: step.title,
                pressed: i === 0,
                stepIndex: i
            });

            stepButton.on('click', function () {
                me.goToStep(this.stepIndex);
            });
        }

        Ext.resumeLayouts();

        me.checkNavigationState();
    },

    initNavigation: function () {
        var me = this,
            prevButton = this.down('button[action=prev]'),
            nextButton = this.down('button[action=next]'),
            cancelButton = this.down('button[action=cancel]');

        prevButton.on('click', me.prevStep, me);
        nextButton.on('click', me.nextStep, me);
        cancelButton.on('click', me.close, me);
    },

    goToStep: function (step) {
        var stepsContainer = this.getStepsContainerCmp();
        stepsContainer.getLayout().setActiveItem(step);
        this.checkNavigationState();
    },

    prevStep: function () {
        var layout = this.getStepsContainerCmp().getLayout(),
            prevCmp = layout.getPrev();

        if (prevCmp) {
            layout.setActiveItem(prevCmp);
        }

        this.checkNavigationState();
    },

    nextStep: function () {
        var layout = this.getStepsContainerCmp().getLayout(),
            nextCmp = layout.getNext();

        if (nextCmp) {
            layout.setActiveItem(nextCmp);
        }

        this.checkNavigationState();
    },

    initStepsTitle: function () {
        var stepsContainer = this.getStepsContainerCmp(),
            stepCmp = stepsContainer.getLayout().getActiveItem(),
            stepsTitle = this.getStepsTitleCmp();

        if (typeof stepCmp !== 'undefined' && stepCmp.hasOwnProperty('title')) {
            stepsTitle.update('<h3>' + stepCmp.title + '</h3>');
        }
    },

    checkNavigationState: function () {
        var menu = this.getStepsMenuCmp(),
            layout = this.getStepsContainerCmp().getLayout(),
            activeItem = layout.getActiveItem(),
            prevCmp = layout.getPrev(),
            prevButton = this.down('button[action=prev]'),
            nextCmp = layout.getNext(),
            nextButton = this.down('button[action=next]');

        for (var i = 0; i < this.getStepsContainerCmp().items.length; i++) {
            var item = this.getStepsContainerCmp().items.items[i];
            if (item.getId() === activeItem.getId()) {
                menu.toggleMenuItem(i);
                break;
            }
        }

        this.initStepsTitle();
        prevButton.setDisabled(!prevCmp);
        nextButton.setDisabled(!nextCmp);
    },

    /**
     * @inheritdoc Ext.panel.Panel#setTitle
     */
    setTitle: function (title) {
        this.callParent(arguments);
        this.getTitleCmp().update('<h2>' + title + '</h2>');
    },

    /**
     * TODO
     *
     * @param {String/Ext.Component} htmlOrCmp
     */
    setDescription: function (htmlOrCmp) {
        Ext.suspendLayouts();
        this.getDescriptionCmp().removeAll();

        if (!(htmlOrCmp instanceof Ext.Component)) {
            // Never modify a passed config object, that could break the expectations of the using code.
            htmlOrCmp = Ext.clone(htmlOrCmp);
        }

        this.getDescriptionCmp().add(htmlOrCmp);
        Ext.resumeLayouts();
    },

    getTitleCmp: function () {
        return this.down('#wizardTitle');
    },

    getDescriptionCmp: function () {
        return this.down('#wizardDescription');
    },

    getStepsMenuCmp: function () {
        return this.down('#stepsMenu');
    },

    getStepsTitleCmp: function () {
        return this.down('#stepsTitle');
    },

    getStepsContainerCmp: function () {
        return this.down('#stepsContainer');
    }

});