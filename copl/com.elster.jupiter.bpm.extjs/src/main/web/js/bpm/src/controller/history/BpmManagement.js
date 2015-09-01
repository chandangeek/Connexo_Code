Ext.define('Bpm.controller.history.BpmManagement', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,



    routeConfig: {
        bpm: {
            title: Uni.I18n.translate('bpm.instance.workspace', 'BPM', 'Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                processes: {
                    title: Uni.I18n.translate('bpm.instance.title', 'BPM', 'Processes'),
                    route: 'processes',
                    controller: 'Bpm.controller.ProcessInstances',
                    action: 'showProcessInstances',
                    items: {
                        view: {
                            title: Uni.I18n.translate('bpm.process', 'BPM', 'Process'),
                            route: '{deploymentId}/{instanceId}',
                            controller: 'Bpm.controller.ProcessInstances',
                            action: 'showProcessInstanceOverview',
                            callback: function(route) {
                                this.getApplication().on('viewProcessInstance', function(record) {
                                    var title = Uni.I18n.translate('bpm.instance.overview.title',  'BPM', 'Process {0} of \'{1}\'');
                                    route.setTitle(Ext.String.format(title, record.get('id'), Ext.String.htmlEncode(record.get('name'))));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                }
            }
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    }
});