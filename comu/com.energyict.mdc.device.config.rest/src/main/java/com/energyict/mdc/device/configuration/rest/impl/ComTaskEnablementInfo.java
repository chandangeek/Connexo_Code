package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ComTaskEnablementInfo {

    public Long id;
    @JsonProperty("comTask")
    public ComTaskInfo comTask;
    @JsonProperty("securityPropertySet")
    public SecurityPropertySetInfo securityPropertySet;
    @JsonProperty("partialConnectionTask")
    public PartialConnectionTaskInfo partialConnectionTask;
    @JsonProperty("protocolDialectConfigurationProperties")
    public ProtocolDialectConfigurationPropertiesInfo protocolDialectConfigurationProperties;
    @JsonProperty("priority")
    public Integer priority;
    @JsonProperty("suspended")
    public Boolean suspended;
    @JsonProperty("ignoreNextExecutionSpecsForInbound")
    public Boolean ignoreNextExecutionSpecsForInbound;
    @JsonProperty("nextExecutionSpecs")
    public TemporalExpressionInfo nextExecutionSpecs;
    @JsonProperty("nextExecutionSpecsName")
    public String nextExecutionSpecsName;

    public ComTaskEnablementInfo() {}

    public static ComTaskEnablementInfo from(ComTaskEnablement comTaskEnablement, Thesaurus thesaurus) {
        ComTaskEnablementInfo comTaskEnablementInfo = new ComTaskEnablementInfo();
        comTaskEnablementInfo.id = comTaskEnablement.getId();
        comTaskEnablementInfo.comTask = ComTaskInfo.from(comTaskEnablement.getComTask());
        comTaskEnablementInfo.securityPropertySet = SecurityPropertySetInfo.from(comTaskEnablement.getSecurityPropertySet());
        comTaskEnablementInfo.partialConnectionTask = PartialConnectionTaskInfo.from(comTaskEnablement.getPartialConnectionTask().orNull(), thesaurus);
        comTaskEnablementInfo.protocolDialectConfigurationProperties = ProtocolDialectConfigurationPropertiesInfo.from(comTaskEnablement.getProtocolDialectConfigurationProperties().orNull(), thesaurus);
        comTaskEnablementInfo.priority = comTaskEnablement.getPriority();
        comTaskEnablementInfo.suspended = comTaskEnablement.isSuspended();
        comTaskEnablementInfo.ignoreNextExecutionSpecsForInbound = comTaskEnablement.isIgnoreNextExecutionSpecsForInbound();
        if (comTaskEnablement.getNextExecutionSpecs() != null) {
            comTaskEnablementInfo.nextExecutionSpecs = TemporalExpressionInfo.from(comTaskEnablement.getNextExecutionSpecs().getTemporalExpression());
            comTaskEnablementInfo.nextExecutionSpecsName = comTaskEnablement.getNextExecutionSpecs().getTemporalExpression().toString();
        } else {
            comTaskEnablementInfo.nextExecutionSpecsName = thesaurus.getString(NextExecutionSpecsInfo.DEFAULT_NEXT_EXECUTION_SPECS_NAME_KEY, NextExecutionSpecsInfo.DEFAULT_NEXT_EXECUTION_SPECS_NAME_KEY);
        }

        return comTaskEnablementInfo;
    }

    public static List<ComTaskEnablementInfo> from(List<ComTaskEnablement> comTaskEnablementList, Thesaurus thesaurus) {
        List<ComTaskEnablementInfo> comTaskEnablementInfos = new ArrayList<>(comTaskEnablementList.size());
        for (ComTaskEnablement comTaskEnablement : comTaskEnablementList) {
            comTaskEnablementInfos.add(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus));
        }
        return comTaskEnablementInfos;
    }

    public void writeTo(ComTaskEnablement comTaskEnablement) {
        comTaskEnablement.setPriority(this.priority);
        comTaskEnablement.setIgnoreNextExecutionSpecsForInbound(this.ignoreNextExecutionSpecsForInbound);
        if(this.nextExecutionSpecs != null) {
            comTaskEnablement.setNextExecutionSpecsFrom(this.nextExecutionSpecs.asTemporalExpression());
        } else {
            comTaskEnablement.removeNextExecutionSpecs();
        }
    }

    public static class ComTaskInfo {
        public Long id;
        public String name;

        public ComTaskInfo() {}

        public static ComTaskInfo from(ComTask comTask) {
            ComTaskInfo comTaskInfo = new ComTaskInfo();
            comTaskInfo.id = comTask.getId();
            comTaskInfo.name = comTask.getName();
            return comTaskInfo;
        }

        public static List<ComTaskInfo> from(List<ComTask> comTaskList) {
            List<ComTaskInfo> comTaskInfos = new ArrayList<>(comTaskList.size());
            for(ComTask comTask : comTaskList) {
                comTaskInfos.add(ComTaskInfo.from(comTask));
            }
            return comTaskInfos;
        }
    }

    public static class SecurityPropertySetInfo {
        public Long id;
        public String name;

        public SecurityPropertySetInfo() {}

        public static SecurityPropertySetInfo from(SecurityPropertySet securityPropertySet) {
            SecurityPropertySetInfo securityPropertySetInfo = new SecurityPropertySetInfo();
            securityPropertySetInfo.id = securityPropertySet.getId();
            securityPropertySetInfo.name = securityPropertySet.getName();
            return securityPropertySetInfo;
        }
    }

    public static class PartialConnectionTaskInfo {
        public static final Long DEFAULT_PARTIAL_CONNECTION_TASK_ID = -1L;
        public static final String DEFAULT_PARTIAL_CONNECTION_TASK_NAME_KEY = "default.partial.connection.task.name";
        public Long id;
        public String name;

        public PartialConnectionTaskInfo() {}

        public static PartialConnectionTaskInfo from(PartialConnectionTask partialConnectionTask, Thesaurus thesaurus) {
            PartialConnectionTaskInfo partialConnectionTaskInfo = new PartialConnectionTaskInfo();
            if(partialConnectionTask ==  null) {
                partialConnectionTaskInfo.id = DEFAULT_PARTIAL_CONNECTION_TASK_ID;
                partialConnectionTaskInfo.name = thesaurus.getString(DEFAULT_PARTIAL_CONNECTION_TASK_NAME_KEY, DEFAULT_PARTIAL_CONNECTION_TASK_NAME_KEY);
            } else {
                partialConnectionTaskInfo.id = partialConnectionTask.getId();
                partialConnectionTaskInfo.name = partialConnectionTask.getName();
            }
            return partialConnectionTaskInfo;
        }
    }

    public static class ProtocolDialectConfigurationPropertiesInfo {
        public static final Long DEFAULT_PROTOCOL_DIALECT_ID = -1L;
        public static final String DEFAULT_PROTOCOL_DIALECT_NAME_KEY = "default.protocol.dialect.name";
        public Long id;
        public String name;

        public ProtocolDialectConfigurationPropertiesInfo() {}

        public static ProtocolDialectConfigurationPropertiesInfo from(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, Thesaurus thesaurus) {
            ProtocolDialectConfigurationPropertiesInfo protocolDialectConfigurationPropertiesInfo = new ProtocolDialectConfigurationPropertiesInfo();
            if(protocolDialectConfigurationProperties == null) {
                protocolDialectConfigurationPropertiesInfo.id = DEFAULT_PROTOCOL_DIALECT_ID;
                protocolDialectConfigurationPropertiesInfo.name = thesaurus.getString(DEFAULT_PROTOCOL_DIALECT_NAME_KEY, DEFAULT_PROTOCOL_DIALECT_NAME_KEY);
            } else {
                protocolDialectConfigurationPropertiesInfo.id = protocolDialectConfigurationProperties.getId();
                protocolDialectConfigurationPropertiesInfo.name = protocolDialectConfigurationProperties.getName();
            }
            return protocolDialectConfigurationPropertiesInfo;
        }
    }

    public static class NextExecutionSpecsInfo {
        public static final String DEFAULT_NEXT_EXECUTION_SPECS_NAME_KEY = "default.next.execution.specs.name";
    }
}
