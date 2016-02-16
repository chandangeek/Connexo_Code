package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.Formula;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.impl.MetrologyInMemoryBootstrapModule;
import com.elster.insight.usagepoint.config.impl.UsagePointConfigModule;
import com.elster.insight.usagepoint.config.impl.UsagePointTestCustomPropertySet;
import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertTrue;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.fail;


@RunWith(MockitoJUnitRunner.class)
public class FormulaCrudTest {


    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    private UsagePointConfigurationService getUsagePointConfigurationService() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    @Test
    // formula = 10 (constant)
    public void test1LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            ConstantNode node = new ConstantNode(BigDecimal.TEN);
            Formula formula = upcService.newFormula(myMode, node);
            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = upcService.findFormula(formulaId);
            if (!loadedFormula.isPresent()) {
                fail("No formula found");
            }
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();
            assertThat(myNode.equals(node));
            if (!(myNode instanceof ConstantNode)) {
                fail("Node should be a ConstantNode");
            }
            ConstantNode constantNode = (ConstantNode) myNode;
            assertThat(constantNode.getValue().equals(BigDecimal.TEN));
        }
    }

    @Test
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            FunctionCallNode node =
                    new FunctionCallNode(
                            Arrays.asList(new ConstantNode(BigDecimal.TEN), new ConstantNode(BigDecimal.ZERO)),
                            myFunction);
            Formula formula = upcService.newFormula(myMode, node);
            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = upcService.findFormula(formulaId);
            if (!loadedFormula.isPresent()) {
                fail("No formula found");
            }
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();
            assertThat(myNode.equals(node));
            if (!(myNode instanceof FunctionCallNode)) {
                fail("Node should be a FunctionCallNode");
            }
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<AbstractNode> children = functionCallNode.getChildren();
            if (children.size() != 2) {
                fail("2 children expected");
            }
            AbstractNode child1 = children.get(0);
            AbstractNode child2 = children.get(1);
            if (!(child1 instanceof ConstantNode)) {
                fail("child1 should be a ConstantNode");
            }
            if (!(child2 instanceof ConstantNode)) {
                fail("child2 should be a ConstantNode");
            }
            ConstantNode constant1 = (ConstantNode) child1;
            assertThat(constant1.getValue().equals(BigDecimal.TEN));
            ConstantNode constant2 = (ConstantNode) child2;
            assertThat(constant2.getValue().equals(BigDecimal.ZERO));
        }
    }

    @Test
    // formula = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrud()  {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            UsagePointConfigurationService upcService = getUsagePointConfigurationService();
            FunctionCallNode node =
                    new FunctionCallNode(
                            Arrays.asList(new ConstantNode(BigDecimal.TEN),
                                    new OperationNode(
                                            Operator.PLUS,
                                            new ConstantNode(BigDecimal.TEN),
                                            new ConstantNode(BigDecimal.ZERO))
                                    ),
                            myFunction);
            Formula formula = upcService.newFormula(myMode, node);
            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = upcService.findFormula(formulaId);
            if (!loadedFormula.isPresent()) {
                fail("No formula found");
            }
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();
            assertThat(myNode.equals(node));
            if (!(myNode instanceof FunctionCallNode)) {
                fail("Node should be a FunctionCallNode");
            }
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<AbstractNode> children = functionCallNode.getChildren();
            if (children.size() != 2) {
                fail("2 children expected");
            }
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            if (!(child1 instanceof ConstantNode)) {
                fail("child1 should be a ConstantNode");
            }
            if (!(child2 instanceof OperationNode)) {
                fail("child2 should be a OperationNode");
            }
            ConstantNode constant = (ConstantNode) child1;
            assertThat(constant.getValue().equals(BigDecimal.TEN));
            OperationNode operation = (OperationNode) child2;
            assertThat(operation.getOperator().equals(Operator.PLUS));



        }


    }





}

