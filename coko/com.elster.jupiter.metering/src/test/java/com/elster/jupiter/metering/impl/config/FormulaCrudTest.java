package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.ExpressionNodeBuilder;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FormulaCrudTest {

    @Mock
    private ReadingTypeRequirement readingTypeRequirement1;
    @Mock
    private ReadingTypeRequirement readingTypeRequirement2;
    @Mock
    Thesaurus thesaurus;

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    @Test
    // formula = Requirement
    public void testRequirementNodeCrud() {
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService service = getMetrologyConfigurationService();


            Optional<ServiceCategory> serviceCategory =
                    inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
            assertThat(serviceCategory.isPresent());
            MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                    service.newMetrologyConfiguration("test", serviceCategory.get());
            MetrologyConfiguration config = metrologyConfigurationBuilder.create();
            assertThat(config != null);
            ReadingType readingType =
                    inMemoryBootstrapModule.getMeteringService().createReadingType(
                            "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "test");
            assertThat(readingType != null);
            config.addReadingTypeRequirement("Aplus").withReadingType(readingType);

            assertThat(config.getRequirements().size() == 1);
            ReadingTypeRequirement req = service.findReadingTypeRequirement(
                    config.getRequirements().get(0).getId()).get();

            ServerFormulaBuilder builder = (ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT);
            ExpressionNodeBuilder nodeBuilder = builder.requirement(req);
            builder.init(nodeBuilder).build();
            context.commit();
        }
    }

    @Test
    // formula = 10 (constant)
    public void test1LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ServerFormulaBuilder builder = (ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT);

            ExpressionNodeBuilder nodeBuilder = builder.constant(10);
            Formula formula = builder.init(nodeBuilder).build();
            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = myFormula.getExpressionNode();
            ConstantNode node = (ConstantNode) nodeBuilder.create();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
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
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ServerFormulaBuilder builder = (ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT);

            ExpressionNodeBuilder nodeBuilder = builder.maximum(
                    builder.constant(10),
                    builder.constant(0));
            Formula formula = builder.init(nodeBuilder).build();

            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = myFormula.getExpressionNode();
            FunctionCallNodeImpl node = (FunctionCallNodeImpl) nodeBuilder.create();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<ExpressionNode> children = functionCallNode.getChildren();
            assertThat(children).hasSize(2);
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
            assertThat(child2).isInstanceOf(ConstantNodeImpl.class);
            ConstantNode constant1 = (ConstantNode) child1;
            assertThat(constant1.getValue().equals(BigDecimal.TEN));
            ConstantNode constant2 = (ConstantNode) child2;
            assertThat(constant2.getValue().equals(BigDecimal.ZERO));
        }
    }


    @Test
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrud()  {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ServerFormulaBuilder builder = (ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT);

            ExpressionNodeBuilder nodeBuilder = builder.maximum(
                    builder.constant(10),
                    builder.plus(
                            builder.constant(10),
                            builder.constant(0)));
            Formula formula = builder.init(nodeBuilder).build();


            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = myFormula.getExpressionNode();

            FunctionCallNodeImpl node = (FunctionCallNodeImpl) nodeBuilder.create();

            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<ExpressionNode> children = functionCallNode.getChildren();
            assertThat(children).hasSize(2);
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
            assertThat(child2).isInstanceOf(OperationNodeImpl.class);
            ConstantNode constant = (ConstantNode) child1;
            assertThat(constant.getValue().equals(BigDecimal.TEN));
            OperationNodeImpl operation = (OperationNodeImpl) child2;
            assertThat(operation.getOperator().equals(Operator.PLUS));
        }
    }

    @Test
    // formula = 10 (constant)
    public void test1LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ExpressionNode node = new ExpressionNodeParser(thesaurus, service).parse("constant(10)");

            Formula formula = ((ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT)).init(node).build();
            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = myFormula.getExpressionNode();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
            ConstantNode constantNode = (ConstantNode) myNode;
            assertThat(constantNode.getValue().equals(BigDecimal.TEN));
        }
    }

    @Test
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ExpressionNode node = new ExpressionNodeParser(thesaurus, service).parse("max(constant(10), constant(0))");

            Formula formula = ((ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT)).init(node).build();

            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = myFormula.getExpressionNode();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
            FunctionCallNodeImpl functionCallNode = (FunctionCallNodeImpl) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<ExpressionNode> children = functionCallNode.getChildren();
            assertThat(children).hasSize(2);
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
            assertThat(child2).isInstanceOf(ConstantNodeImpl.class);
            ConstantNode constant1 = (ConstantNode) child1;
            assertThat(constant1.getValue().equals(BigDecimal.TEN));
            ConstantNode constant2 = (ConstantNode) child2;
            assertThat(constant2.getValue().equals(BigDecimal.ZERO));
        }
    }

    @Test
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrudUsingParser()  {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ExpressionNode node = new ExpressionNodeParser(thesaurus, service).parse("max(constant(1), plus(constant(2), constant(3)))");

            Formula formula = ((ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT)).init(node).build();

            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = myFormula.getExpressionNode();

            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<ExpressionNode> children = functionCallNode.getChildren();
            assertThat(children).hasSize(2);
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
            assertThat(child2).isInstanceOf(OperationNodeImpl.class);
            ConstantNode constant = (ConstantNode) child1;
            assertThat(constant.getValue().equals(BigDecimal.TEN));
            OperationNode operation = (OperationNode) child2;
            assertThat(operation.getOperator().equals(Operator.PLUS));
        }
    }

    @Test
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test4LevelNodeStructureCrudUsingParser()  {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            String formulaString = "max(constant(1), min(constant(2), constant(3), constant(4)))";
            ExpressionNode node = new ExpressionNodeParser(thesaurus, service).parse("max(constant(1), min(constant(2), constant(3), constant(4)))");

            Formula formula = ((ServerFormulaBuilder) service.newFormulaBuilder(myMode)).init(node).build();

            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = myFormula.getExpressionNode();

            assertThat(myNode.equals(node));
            assertThat(myNode.toString().equals(formulaString));
        }
    }

    @Test
    public void testParser()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
            String formulaString = "multiply(sum(max(constant(10), constant(0)), constant(5), constant(3)), constant(2))";
            ExpressionNode node = new ExpressionNodeParser(thesaurus, service).parse(formulaString);
            ((ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT)).init(node).build();
            context.commit();
            List<Formula> formulas = service.findFormulas();
            for (Formula f : formulas) {
                System.out.println(f.toString());
            }
        }
    }

    @Test
    // formula = 10 (constant)
    public void testDelete() {
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ServerFormulaBuilder builder = (ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT);

            ExpressionNodeBuilder nodeBuilder = builder.constant(10);
            Formula formula = builder.init(nodeBuilder).build();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            myFormula.delete();
            loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isEmpty();
            context.commit();
        }
    }

    @Test
    // formula = 10 (constant)
    public void testUpdate() {
        try (TransactionContext context = getTransactionService().getContext()) {
            ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

            ServerFormulaBuilder builder = (ServerFormulaBuilder) service.newFormulaBuilder(Formula.Mode.EXPERT);

            ExpressionNodeBuilder nodeBuilder = builder.constant(10);
            Formula formula = builder.init(nodeBuilder).build();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();

            ExpressionNode newExpression = builder.constant(99).create();
            myFormula.updateExpression(newExpression);
            context.commit();

            loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(Formula.Mode.EXPERT));
            ExpressionNode myNode = myFormula.getExpressionNode();
            assertThat(myNode.equals(newExpression));
            assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
            ConstantNode constantNode = (ConstantNode) myNode;
            assertThat(constantNode.getValue().equals(new BigDecimal(99)));
        }
    }

}