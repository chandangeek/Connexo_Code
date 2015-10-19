Ext.define('Bpm.store.task.Priorities', {
    extend: 'Ext.data.Store',
    model: 'Bpm.model.task.Priority',
    proxy: {
        type: 'memory'
    },
    data:  [
        {
            name: Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High'),
            value: 3
        },
        {
            name: Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium'),
            value: 6
        },
        {
            name: Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low'),
            value: 10
        }
    ]
});
