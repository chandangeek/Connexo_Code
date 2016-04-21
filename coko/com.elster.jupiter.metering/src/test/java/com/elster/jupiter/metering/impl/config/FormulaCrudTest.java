package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.nls.Thesaurus;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class FormulaCrudTest {

    @Mock
    private ReadingTypeRequirement readingTypeRequirement1;
    @Mock
    private ReadingTypeRequirement readingTypeRequirement2;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MetrologyConfiguration config;

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

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

    @Test
    @Transactional
    // formula = Requirement
    public void testRequirementNodeCrud() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType readingType =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.1.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype");
        assertThat(readingType).isNotNull();
        config.newReadingTypeRequirement("Aplus").withReadingType(readingType);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);
        ExpressionNodeBuilder nodeBuilder = builder.requirement(req);
        Formula formula = builder.init(nodeBuilder).build();
        assertThat(formula.getExpressionNode()).isInstanceOf(ReadingTypeRequirementNode.class);
        ReadingTypeRequirementNode reqNode = (ReadingTypeRequirementNode) formula.getExpressionNode();
        assertThat(reqNode.getReadingTypeRequirement()).isEqualTo(req);
    }

    @Test
    // formula = 10 (constant)
    @Transactional
    public void test1LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.constant(10);
        Formula formula = builder.init(nodeBuilder).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constantNode = (ConstantNode) myNode;
        assertThat(constantNode.getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @Transactional
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(myMode);

        ExpressionNodeBuilder nodeBuilder =
                builder.maximum(Arrays.asList(
                    builder.constant(10),
                    builder.constant(0)));
        Formula formula = builder.init(nodeBuilder).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
        assertThat(functionCallNode.getFunction()).isEqualTo(myFunction);
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constant1 = (ConstantNode) child1;
        assertThat(constant1.getValue()).isEqualTo(BigDecimal.TEN);
        ConstantNode constant2 = (ConstantNode) child2;
        assertThat(constant2.getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrud() {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder =
                builder.maximum(Arrays.asList(
                            builder.constant(10),
                            builder.plus(
                                    builder.constant(10),
                                    builder.constant(0))));
        Formula formula = builder.init(nodeBuilder).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();

        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
        assertThat(functionCallNode.getFunction()).isEqualTo(myFunction);
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(OperationNodeImpl.class);
        ConstantNode constant = (ConstantNode) child1;
        assertThat(constant.getValue()).isEqualTo(BigDecimal.TEN);
        OperationNodeImpl operation = (OperationNodeImpl) child2;
        assertThat(operation.getOperator()).isEqualTo(Operator.PLUS);
    }

    @Test
    @Transactional
    // formula = 10 (constant)
    public void test1LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(thesaurus, service, config, myMode).parse("constant(10)");

        Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode).isEqualTo(node);
        assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constantNode = (ConstantNode) myNode;
        assertThat(constantNode.getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @Transactional
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, config, myMode).parse("max(constant(10), constant(0))");

        Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode).isEqualTo(node);
        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNodeImpl functionCallNode = (FunctionCallNodeImpl) myNode;
        assertThat(functionCallNode.getFunction()).isEqualTo(myFunction);
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constant1 = (ConstantNode) child1;
        assertThat(constant1.getValue()).isEqualTo(BigDecimal.TEN);
        ConstantNode constant2 = (ConstantNode) child2;
        assertThat(constant2.getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrudUsingParser() {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(thesaurus, service, config, myMode).parse("max(constant(1), plus(constant(2), constant(3)))");

        Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();

        assertThat(myNode).isEqualTo(node);
        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
        assertThat(functionCallNode.getFunction()).isEqualTo(myFunction);
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(OperationNodeImpl.class);
        ConstantNode constant = (ConstantNode) child1;
        assertThat(constant.getValue()).isEqualTo(BigDecimal.ONE);
        OperationNode operation = (OperationNode) child2;
        assertThat(operation.getOperator()).isEqualTo(Operator.PLUS);
        assertThat(operation.getChildren()).hasSize(2);
        assertThat(operation.getLeftOperand()).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) operation.getLeftOperand()).getValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(operation.getRightOperand()).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) operation.getRightOperand()).getValue()).isEqualTo(BigDecimal.valueOf(3));
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void testNoAggregationFunctionsAllowedInAutoMode() {
        Formula.Mode myMode = Formula.Mode.AUTO;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        try {
            new ExpressionNodeParser(service.getThesaurus(), service, config, myMode).parse("maxOf(max(constant(1), constant(2)), plus(constant(2), constant(3)))");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessageSeed(), MessageSeeds.FUNCTION_NOT_ALLOWED_IN_AUTOMODE);
            assertEquals(e.get("Function"), Function.MAX_AGG);
            throw e;
        }
    }

    @Test
    @Transactional
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test4LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        String formulaString = "max(constant(1), min(constant(2), constant(3), constant(4)))";
        ExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, config, myMode).parse("max(constant(1), min(constant(2), constant(3), constant(4)))");

        Formula formula = service.newFormulaBuilder(myMode).init(node).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();

        assertThat(myNode).isEqualTo(node);
        assertThat(myNode.toString()).isEqualTo(formulaString);
    }

    @Test
    @Transactional
    public void testParser() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        String formulaString = "multiply(sum(hour, max(hour, constant(10), constant(0)), constant(5), constant(3)), constant(2))";
        ExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, config, Formula.Mode.EXPERT).parse(formulaString);
        service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();
        List<Formula> formulas = service.findFormulas();
        for (Formula f : formulas) {
            System.out.println(f.toString());
        }
    }

    @Test
     @Transactional
     // formula = 10 (constant)
     public void testDelete() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.constant(10);
        Formula formula = builder.init(nodeBuilder).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        ((ServerFormula) myFormula).delete();
        loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isEmpty();
    }

    @Test
    @Transactional
    // formula = 10 (constant)
    public void testUpdate() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.constant(10);
        Formula formula = builder.init(nodeBuilder).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();

        ExpressionNode newExpression = builder.constant(99).create();
        myFormula.updateExpression(newExpression);

        loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(Formula.Mode.EXPERT);
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode).isEqualTo(newExpression);
        assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constantNode = (ConstantNode) myNode;
        assertThat(constantNode.getValue()).isEqualTo(new BigDecimal(99));
    }

    @Test
    @Transactional
    public void testDeliverableCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        String name = "deliverable";
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test5", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.3.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "test");
        assertThat(readingType).isNotNull();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, myMode);

        ReadingTypeDeliverable deliverable = builder.build(builder.maximum(builder.constant(10), builder.constant(20)));
        assertThat(deliverable.getFormula().getExpressionNode().toString()).isEqualTo("max(constant(10), constant(20))");
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void createDeliverableWithRequirementThatIsOnADifferentMetrologyConfig() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType readingType =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "test");
        assertThat(readingType).isNotNull();
        config.newReadingTypeRequirement("Aplus").withReadingType(readingType);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test2", serviceCategory.get());
        MetrologyConfiguration config2 = metrologyConfigurationBuilder.create();

        ReadingTypeDeliverableBuilder builder =
                config2.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);

        try {
            builder.build(builder.requirement(req));
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "The requirement with id '" + req.getId() + "' cannot be used because it has a different metrology configuration.");
            throw e;
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void createDeliverableOnARequirementThatIsDimensionless() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType readingTypeRequirement =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.12.0.41.109.0.0.0.0.0.0.0.0.0.281.0", "readingtype for requirement");
        assertThat(readingTypeRequirement).isNotNull();
        config.newReadingTypeRequirement("consumption").withReadingType(readingTypeRequirement);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingType readingTypeDeliverable =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype for deliverable");
        assertThat(readingTypeDeliverable).isNotNull();

        ReadingTypeDeliverableBuilder builder =
                config.newReadingTypeDeliverable("deliverable", readingTypeDeliverable, Formula.Mode.AUTO);

        builder.build(builder.requirement(req));
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void createDeliverableOnARequirementWithIncompatibleReadingType() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test4", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType readingTypeRequirement =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "readingtype for requirement2");
        assertThat(readingTypeRequirement).isNotNull();
        config.newReadingTypeRequirement("cons").withReadingType(readingTypeRequirement);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingType readingTypeDeliverable =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype for deliverable2");
        assertThat(readingTypeDeliverable).isNotNull();

        ReadingTypeDeliverableBuilder builder =
                config.newReadingTypeDeliverable("deliverable", readingTypeDeliverable, Formula.Mode.AUTO);

        try {
            builder.build(builder.requirement(req));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(),
                    "The readingtype \"" + readingTypeDeliverable .getMRID() + " (" + readingTypeDeliverable.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            "deliverable" + " = R(" +  req.getId() + ")"
                            + "\".");
            throw e;
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testUpdateReadingTypeOfDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");
        assertThat(conskWhRT15min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.requirement(req));

        ReadingType temperatureRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "temp");
        try {
            deliverable1.setReadingType(temperatureRT);
            deliverable1.update();
            fail("ConstraintViolationException expected");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(),
                    "The readingtype \"" + temperatureRT .getMRID() + " (" + temperatureRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable1.getName() + " = " +  deliverable1.getFormula().getExpressionNode().toString()
                    + "\".");
        }

        ReadingType conskWhMonthlyRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhMonthlyRT");

        try {
            deliverable1.setReadingType(conskWhMonthlyRT);
            deliverable1.update();
            assertEquals(deliverable1.getReadingType(), conskWhMonthlyRT);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }

    }

    @Test
    @Transactional
    // formula = Requirement
    public void testUpdateReadingTypeOfDeliverableThatIsusedinAnotherDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");

        ReadingType conskWhRT60min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhRT60min");

        assertThat(conskWhRT15min).isNotNull();
        assertThat(conskWhRT60min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.requirement(req));

        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Del2", conskWhRT60min, Formula.Mode.AUTO);
        builder2.build(builder2.deliverable(deliverable1));

        ReadingType temperatureRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "temp");
        try {
            deliverable1.setReadingType(temperatureRT);
            deliverable1.update();
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(),
                    "The readingtype \"" + temperatureRT .getMRID() + " (" + temperatureRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable1.getName() + " = " +  deliverable1.getFormula().getExpressionNode().toString()
                            + "\".");
        }

        ReadingType conskWhMonthlyRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhMonthlyRT");

        try {
            deliverable1.setReadingType(conskWhMonthlyRT);
            deliverable1.update();
            assertEquals(deliverable1.getReadingType(), conskWhMonthlyRT);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void testIrregularReadingTypeOfDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0", "regRT");
        assertThat(regRT).isNotNull();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("DelOnReg", regRT, Formula.Mode.AUTO);

        try {
            builder.build(builder.constant(10));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "Irregular readingtypes are not allowed for a deliverable.");
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void testIrregularReadingTypeOfRequirementByBuilder() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0", "regRT");
        assertThat(regRT).isNotNull();

        config.newReadingTypeRequirement("Req2").withReadingType(regRT);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();


        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del3", regRT, Formula.Mode.AUTO);
        try {
            builder.build(builder.requirement(req));
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Irregular readingtypes are not allowed for a requirement.");
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void testIrregularReadingTypeOfRequirementByParser() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0", "regRT");
        assertThat(regRT).isNotNull();

        config.newReadingTypeRequirement("ReqWithIrregularRT").withReadingType(regRT);
        ReadingType conskWhMonthlyRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhMonthlyRT");
        assertThat(conskWhMonthlyRT).isNotNull();

        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        try {
            new ExpressionNodeParser(service.getThesaurus(), service, config, Formula.Mode.AUTO).parse("R(" + req.getId() + ")");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Irregular readingtypes are not allowed for a requirement.");
            throw e;
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void test15MinDeliverableOnMonthlyRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType monthly =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "monthly");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(monthly).isNotNull();
        assertThat(fifteenMinRT).isNotNull();
        config.newReadingTypeRequirement("monthly").withReadingType(monthly);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("fifteenMinDeliverable", fifteenMinRT, Formula.Mode.AUTO);
        try {
            builder.build(builder.requirement(req));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "The interval of the output reading type should be larger or equal to interval of the requirements in the formula.");
            throw e;
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testMonthlyMinDeliverableOn15MinRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType monthly =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "monthly");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(monthly).isNotNull();
        assertThat(fifteenMinRT).isNotNull();
        config.newReadingTypeRequirement("15Min").withReadingType(fifteenMinRT);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("monthly", monthly, Formula.Mode.AUTO);
        builder.build(builder.requirement(req));
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void test30MinDeliverableOn15MinAnd60MinRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType thirtyMinTR =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "monthly");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        ReadingType sixtyMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "sixtyMinRT");

        assertThat(thirtyMinTR).isNotNull();
        assertThat(fifteenMinRT).isNotNull();
        assertThat(sixtyMinRT).isNotNull();

        config.newReadingTypeRequirement("sixtyMinRT").withReadingType(sixtyMinRT);
        config.newReadingTypeRequirement("fifteenMinRT").withReadingType(fifteenMinRT);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement req1 = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement req2 = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("deliverable", thirtyMinTR, Formula.Mode.AUTO);
        try {
            builder.build(builder.plus(builder.requirement(req1), builder.requirement(req2)));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "The interval of the output reading type should be larger or equal to interval of the requirements in the formula.");
            throw e;
        }
    }

    @Test(expected=ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void test30MinDeliverableOn15MinAndWildcardRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType thirtyMinTR =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "thirtyMinTR");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(thirtyMinTR).isNotNull();
        assertThat(fifteenMinRT).isNotNull();

        config.newReadingTypeRequirement("fifteenMinRT").withReadingType(fifteenMinRT);

        ReadingTypeTemplate template = service.createReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS).done();

        config.newReadingTypeRequirement("template").withReadingTypeTemplate(template);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement req1 = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement req2 = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        //30 min = 15 min + *
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("deliverable", thirtyMinTR, Formula.Mode.AUTO);
        try {
            builder.build(builder.plus(builder.requirement(req1), builder.requirement(req2)));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "MINUTE15 values cannot be aggregated to MINUTE30 values.");
            throw e;
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void test30MinDeliverableOn15MinAnd5MinRequirementWithWildcard() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType thirtyMinTR =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "thirtyMinTR");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(thirtyMinTR).isNotNull();
        assertThat(fifteenMinRT).isNotNull();

        config.newReadingTypeRequirement("fifteenMinRT").withReadingType(fifteenMinRT);

        ReadingTypeTemplate template = service.createReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS)
                .setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE5.getId()).done();

        config.newReadingTypeRequirement("template").withReadingTypeTemplate(template);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement req1 = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement req2 = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        try {
        //30 min = 15 min + 5min
            ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("deliverable", thirtyMinTR, Formula.Mode.AUTO);
            builder.build(builder.plus(builder.requirement(req1), builder.requirement(req2)));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "MINUTE15 values cannot be aggregated to MINUTE30 values.");
            throw e;
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void test30MinDeliverableOn15MinAnd60MinRequirementWithWildcard() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType thirtyMinTR =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "thirtyMinTR");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(thirtyMinTR).isNotNull();
        assertThat(fifteenMinRT).isNotNull();

        config.newReadingTypeRequirement("fifteenMinRT").withReadingType(fifteenMinRT);

        ReadingTypeTemplate template = service.createReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS)
                .setAttribute(ReadingTypeTemplateAttributeName.TIME, TimeAttribute.MINUTE60.getId()).done();

        config.newReadingTypeRequirement("template").withReadingTypeTemplate(template);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement req1 = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement req2 = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        //30 min = 15 min + 5min
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("deliverable", thirtyMinTR, Formula.Mode.AUTO);
        try {
            builder.build(builder.plus(builder.requirement(req1), builder.requirement(req2)));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "The interval of the output reading type should be larger or equal to interval of the requirements in the formula.");
            throw e;
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testMonthlyMinDeliverableOn15MinDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType monthly =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "monthly");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        ReadingType sixtyMinTR =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "60MinTR");

        assertThat(monthly).isNotNull();
        assertThat(fifteenMinRT).isNotNull();
        assertThat(sixtyMinTR).isNotNull();
        config.newReadingTypeRequirement("15Min").withReadingType(fifteenMinRT);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("thirtyMinDelivrable", sixtyMinTR, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.requirement(req));
        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("monthlyDelivrable", monthly, Formula.Mode.AUTO);
        builder2.build(builder2.deliverable(deliverable));
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void testWrongUpdateReadingTypeOfDeliverableThatIsUsedInAnotherDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");

        ReadingType conskWhRT60min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhRT60min");
        assertThat(conskWhRT15min).isNotNull();
        assertThat(conskWhRT60min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Deliverable1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));

        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Deliverable2", conskWhRT60min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable2 = builder2.build(builder2.deliverable(deliverable1));

        ReadingType temperatureRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "temp");
        try {
            deliverable1.setReadingType(temperatureRT);
            deliverable1.update();
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(),
                    "The readingtype \"" + conskWhRT60min.getMRID() + " (" + conskWhRT60min.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable2.getName() + " = " +  deliverable2.getFormula().getExpressionNode().toString()
                            + "\".");
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void testCombinationOfAutoModeAndExpertMode() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");
        assertThat(conskWhRT15min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Deliverable1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));

        try {
            ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Deliverable2", conskWhRT15min, Formula.Mode.EXPERT);
            builder2.build(builder2.deliverable(deliverable1));
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Auto mode and export mode cannot be combined.");
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void testCombinationOfExperAndAutoMode() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");
        assertThat(conskWhRT15min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Deliverable1", conskWhRT15min, Formula.Mode.EXPERT);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));

        try {
            ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Deliverable2", conskWhRT15min, Formula.Mode.AUTO);
            builder2.build(builder2.deliverable(deliverable1));
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Auto mode and export mode cannot be combined.");
            throw e;
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void inconsistentAggregationLevelsInAggregationFunctions() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config4", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType conskWhRT15min = inMemoryBootstrapModule.getMeteringService().createReadingType( "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");
        assertThat(conskWhRT15min).isNotNull();
        FullySpecifiedReadingTypeRequirement requirement = config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        try {
            ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("conskWhRT15min", conskWhRT15min, Formula.Mode.EXPERT);
            builder.build(builder.plus(builder.maximum(AggregationLevel.DAY, builder.requirement(requirement)), builder.maximum(AggregationLevel.MONTH, builder.requirement(requirement))));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "All aggregation functions must use the same aggregation level argument.");
            throw e;
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void testInvalidReadingTypeOfDeliverableInAutoMode() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config4", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType status =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.12.0.41.109.0.0.0.0.0.0.0.0.0.108.0", "status");
        assertThat(status).isNotNull();

        try {
            ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("InvalidDeliverable", status, Formula.Mode.AUTO);
            builder.build(builder.constant(10));
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "The readingtype for the deliverable is not valid, it should represent a numerical value.");
            throw e;
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testInvalidReadingTypeOfDeliverableInExpertMode() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config4", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType status =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.12.0.41.109.0.0.0.0.0.0.0.0.0.108.0", "status");
        assertThat(status).isNotNull();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("InvalidDeliverable", status, Formula.Mode.EXPERT);
        builder.build(builder.constant(10));
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void testInvalidReadingTypeOfRequirementByBuilder() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.12.0.41.109.0.0.0.0.0.0.0.0.0.108.0", "status");
        assertThat(regRT).isNotNull();

        config.newReadingTypeRequirement("Req2").withReadingType(regRT);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del3", regRT, Formula.Mode.AUTO);
        try {
            builder.build(builder.requirement(req));
        } catch (InvalidNodeException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INVALID_READINGTYPE_IN_REQUIREMENT);
            throw e;
        }
    }


    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void testInvalidReadingTypeOfRequirementByParser() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.12.0.41.109.0.0.0.0.0.0.0.0.0.108.0", "status");
        assertThat(regRT).isNotNull();

        config.newReadingTypeRequirement("ReqWithInvalidRT").withReadingType(regRT);

        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        try {
            new ExpressionNodeParser(service.getThesaurus(), service, config, Formula.Mode.AUTO).parse("R(" + req.getId() + ")");
        } catch (InvalidNodeException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INVALID_READINGTYPE_IN_REQUIREMENT);
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    // formula = Requirement
    public void testInvalidReadingTypeForRequirementTemplate() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.12.0.41.109.0.0.0.0.0.0.0.0.0.108.0", "status");
        assertThat(regRT).isNotNull();

        ReadingTypeTemplate template = service.createReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS)
                .setAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, ReadingTypeUnit.BOOLEAN.getId()).done();

        config.newReadingTypeRequirement("template").withReadingTypeTemplate(template);

        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        try {
            new ExpressionNodeParser(service.getThesaurus(), service, config, Formula.Mode.AUTO).parse("R(" + req.getId() + ")");
        } catch (InvalidNodeException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INVALID_READINGTYPE_IN_REQUIREMENT);
            throw e;
        }

    }

    @Test
    @Transactional
    // formula = max(10, 0) function call + constants
    public void testMinusUsingParser() {
        Formula.Mode myMode = Formula.Mode.AUTO;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node =
                new ExpressionNodeParser(service.getThesaurus(), service, config, myMode)
                        .parse("minus(constant(10), constant(5))");

        Formula formula = service.newFormulaBuilder(myMode).init(node).build();
        assertThat(formula).isNotNull();
    }


    @Test
    @Transactional
    // formula = Requirement
    public void test15MinDeliverableOn10MinRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config10", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType tenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.1.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "10MinRT");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(tenMinRT).isNotNull();
        assertThat(fifteenMinRT).isNotNull();
        config.newReadingTypeRequirement("tenMin").withReadingType(tenMinRT);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("fifteenMinDeliverable", fifteenMinRT, Formula.Mode.AUTO);
        try {
            ReadingTypeDeliverable deliverable = builder.build(builder.requirement(req));
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "MINUTE10 values cannot be aggregated to MINUTE15 values.");
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void test10MinDeliverableOn5MinAnd3MinRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType tenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.1.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "10MinRT");

        ReadingType fiveMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.6.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "5MinRT");

        ReadingType threeMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.14.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "3MinRT");

        assertThat(tenMinRT).isNotNull();
        assertThat(fiveMinRT).isNotNull();
        assertThat(threeMinRT).isNotNull();

        config.newReadingTypeRequirement("5MinRT").withReadingType(fiveMinRT);
        config.newReadingTypeRequirement("3MinRT").withReadingType(threeMinRT);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement req1 = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement req2 = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("deliverable", tenMinRT, Formula.Mode.AUTO);
        try {
            ReadingTypeDeliverable deliverable = builder.build(builder.plus(builder.requirement(req1), builder.requirement(req2)));
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(), "MINUTE5 values cannot be aggregated to MINUTE10 values.");
        }
    }


    @Test
    @Transactional
    // formula = Requirement
    public void test60MinDeliverableOn15MinAndWildcardRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType sixtyMinTR =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "sixtyMinTR");

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(sixtyMinTR).isNotNull();
        assertThat(fifteenMinRT).isNotNull();

        config.newReadingTypeRequirement("fifteenMinRT").withReadingType(fifteenMinRT);

        ReadingTypeTemplate template = service.createReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS).done();

        config.newReadingTypeRequirement("template").withReadingTypeTemplate(template);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement req1 = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement req2 = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        //60 min = 15 min + *
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("deliverable", sixtyMinTR, Formula.Mode.AUTO);

        // Business method
        builder.build(builder.plus(builder.requirement(req1), builder.requirement(req2)));
    }


    @Test
    @Transactional
    // formula = Requirement
    public void testDivisionByConstant() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(fifteenMinRT).isNotNull();
        config.newReadingTypeRequirement("15Min").withReadingType(fifteenMinRT);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("monthly", fifteenMinRT, Formula.Mode.AUTO);

        // Business method
        builder.build(builder.divide(builder.requirement(req), builder.constant(10)));
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testSafeDivisionWithConstantOne() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");

        assertThat(fifteenMinRT).isNotNull();
        config.newReadingTypeRequirement("15Min").withReadingType(fifteenMinRT);

        assertThat(config.getRequirements()).hasSize(1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("monthly", fifteenMinRT, Formula.Mode.AUTO);

        // Business method
        builder.build(
                builder.safeDivide(
                        builder.requirement(req),
                        builder.constant(0),
                        builder.constant(1)));
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testSafeDivisionWithOtherRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");
        ReadingType fifteenMinWhRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "fifteenMinWhRT");
        ReadingTypeRequirement req_kWh = config.newReadingTypeRequirement("15Min_kWh").withReadingType(fifteenMinRT);
        ReadingTypeRequirement req_Wh = config.newReadingTypeRequirement("15Min_Wh").withReadingType(fifteenMinWhRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("monthly", fifteenMinRT, Formula.Mode.AUTO);

        // Business method
        builder.build(
                builder.safeDivide(
                        builder.requirement(req_kWh),
                        builder.constant(0),
                        builder.constant(1)));
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testSafeDivisionWithNull() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT");
        ReadingTypeRequirement req_kWh = config.newReadingTypeRequirement("15Min_kWh").withReadingType(fifteenMinRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("monthly", fifteenMinRT, Formula.Mode.AUTO);

        // Business method
        builder.build(
                builder.safeDivide(
                        builder.requirement(req_kWh),
                        builder.constant(0),
                        builder.nullValue()));
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testUpdateReadingTypeOfDeliverableThatIsusedinAnotherDeliverable2() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType AplusRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");

        ReadingType AminRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0", "AminRT");

        ReadingType noUnitRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0").get();

        ReadingType otherRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "otherRT");

        assertThat(AplusRT).isNotNull();
        assertThat(AminRT).isNotNull();
        config.newReadingTypeRequirement("AplusRT").withReadingType(AplusRT);
        config.newReadingTypeRequirement("AminRT").withReadingType(AminRT);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement aPlusReq = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement aMinReq = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", noUnitRT, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.divide(builder.requirement(aPlusReq), builder.requirement(aMinReq)));

        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Del2", otherRT, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable2 = builder2.build(builder2.deliverable(deliverable1));

        ReadingType incompatibleRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0", "incompatibleRT");
        try {
            deliverable1.setReadingType(incompatibleRT);
            deliverable1.update();
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(),
                    "The readingtype \"" + otherRT .getMRID() + " (" + otherRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable2.getName() + " = " +  deliverable2.getFormula().getExpressionNode().toString()
                            + "\".");
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testUpdateReadingTypeOfDeliverableThatIsusedinAnotherDeliverable3() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType AplusRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");

        ReadingType AminRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0", "AminRT");

        ReadingType noUnitRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0").get();

        ReadingType otherRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "m3");

        assertThat(AplusRT).isNotNull();
        assertThat(AminRT).isNotNull();
        config.newReadingTypeRequirement("AplusRT").withReadingType(AplusRT);
        config.newReadingTypeRequirement("AminRT").withReadingType(AminRT);

        assertThat(config.getRequirements()).hasSize(2);
        ReadingTypeRequirement aPlusReq = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();
        ReadingTypeRequirement aMinReq = service.findReadingTypeRequirement(
                config.getRequirements().get(1).getId()).get();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", noUnitRT, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.divide(builder.requirement(aPlusReq), builder.requirement(aMinReq)));

        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Del2", otherRT, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable2 = builder2.build(builder2.deliverable(deliverable1));

        ReadingType incompatibleRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0", "incompatibleRT");
        try {
            ReadingTypeDeliverable toUpdate = config.getDeliverables().get(0);
            assertThat(toUpdate.getName()).isEqualTo("Del1");
            toUpdate.setReadingType(incompatibleRT);
            toUpdate.update();
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().iterator().next().getMessage(),
                    "The readingtype \"" + otherRT .getMRID() + " (" + otherRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable2.getName() + " = " +  deliverable2.getFormula().getExpressionNode().toString()
                            + "\".");
        }
    }

    @Test(expected = ReadingTypeAlreadyUsedOnMetrologyConfiguration.class)
    @Transactional
    // formula = Requirement
    public void testMultipleDeliverableWithSamereadingTypeOnSameMetrologyConfig() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType AplusRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");

        assertThat(AplusRT).isNotNull();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));

        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Del2", AplusRT, Formula.Mode.AUTO);

        try {
            builder2.build(builder2.constant(10));
        } catch (ReadingTypeAlreadyUsedOnMetrologyConfiguration e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.READING_TYPE_FOR_DELIVERABLE_ALREADY_USED);
            throw e;
        }
    }

    @Test
    @Transactional
    // formula = 10 (constant)
    public void testSafeDivideUsingParser() {
        Formula.Mode myMode = Formula.Mode.AUTO;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(thesaurus, service, config, myMode).parse(
                "safe_divide(constant(10), constant(20), constant(30))");

        Formula formula = service.newFormulaBuilder(myMode).init(node).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode).isEqualTo(node);
        assertThat(myNode).isInstanceOf(OperationNodeImpl.class);
        OperationNode operationNode = (OperationNode) myNode;
        assertThat(operationNode.getOperator()).isEqualTo(Operator.SAFE_DIVIDE);
        assertThat(operationNode.getLeftOperand()).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getLeftOperand()).getValue()).isEqualTo(new BigDecimal(10));
        assertThat(operationNode.getRightOperand()).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getRightOperand()).getValue()).isEqualTo(new BigDecimal(20));
        assertThat(operationNode.getChildren().get(2)).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getChildren().get(2)).getValue()).isEqualTo(new BigDecimal(30));
    }

    @Test
    @Transactional
    // formula = 10 (constant)
    public void testSafeDivideWithNullUsingParser() {
        Formula.Mode myMode = Formula.Mode.AUTO;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(thesaurus, service, config, myMode).parse(
                "safe_divide(constant(10), constant(20), null)");

        Formula formula = service.newFormulaBuilder(myMode).init(node).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId()).isEqualTo(formulaId);
        assertThat(myFormula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode).isEqualTo(node);
        assertThat(myNode).isInstanceOf(OperationNodeImpl.class);
        OperationNode operationNode = (OperationNode) myNode;
        assertThat(operationNode.getOperator()).isEqualTo(Operator.SAFE_DIVIDE);
        assertThat(operationNode.getLeftOperand()).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getLeftOperand()).getValue()).isEqualTo(new BigDecimal(10));
        assertThat(operationNode.getRightOperand()).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getRightOperand()).getValue()).isEqualTo(new BigDecimal(20));
        assertThat(operationNode.getChildren().get(2)).isInstanceOf(NullNodeImpl.class);
    }

    @Test
    @Transactional
    public void testSafeDivideWithNullUsingBuilder() {
        Formula.Mode myMode = Formula.Mode.AUTO;
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType AplusRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");

        assertThat(AplusRT).isNotNull();

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.safeDivide(builder.constant(10), builder.constant(20), builder.nullValue()));

        // Asserts
        assertThat(deliverable).isNotNull();
        Formula formula = deliverable.getFormula();
        assertThat(formula.getMode()).isEqualTo(myMode);
        ExpressionNode myNode = formula.getExpressionNode();
        assertThat(myNode).isInstanceOf(OperationNodeImpl.class);
        OperationNode operationNode = (OperationNode) myNode;
        assertThat(operationNode.getOperator()).isEqualTo(Operator.SAFE_DIVIDE);
        assertThat(operationNode.getLeftOperand()).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getLeftOperand()).getValue()).isEqualTo(new BigDecimal(10));
        assertThat(operationNode.getRightOperand()).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getRightOperand()).getValue()).isEqualTo(new BigDecimal(20));
        assertThat(operationNode.getChildren().get(2)).isInstanceOf(NullNodeImpl.class);
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void minimumAggregationOfRequirement() {
        Optional<ServiceCategory> serviceCategory =inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        assertThat(AplusRT).isNotNull();
        FullySpecifiedReadingTypeRequirement aPlus = config.newReadingTypeRequirement("AplusRT").withReadingType(AplusRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.minimum(AggregationLevel.DAY, builder.requirement(aPlus)));

        // Asserts
        assertThat(deliverable).isNotNull();
        assertThat(deliverable.getFormula()).isNotNull();
        assertThat(deliverable.getFormula().getExpressionNode()).isNotNull();
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void maximumAggregationOfRequirement() {
        Optional<ServiceCategory> serviceCategory =inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        assertThat(AplusRT).isNotNull();
        FullySpecifiedReadingTypeRequirement aPlus = config.newReadingTypeRequirement("AplusRT").withReadingType(AplusRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.maximum(AggregationLevel.DAY, builder.requirement(aPlus)));

        // Asserts
        assertThat(deliverable).isNotNull();
        assertThat(deliverable.getFormula()).isNotNull();
        assertThat(deliverable.getFormula().getExpressionNode()).isNotNull();
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void averageAggregationOfRequirement() {
        Optional<ServiceCategory> serviceCategory =inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        assertThat(AplusRT).isNotNull();
        FullySpecifiedReadingTypeRequirement aPlus = config.newReadingTypeRequirement("AplusRT").withReadingType(AplusRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.average(AggregationLevel.DAY, builder.requirement(aPlus)));

        // Asserts
        assertThat(deliverable).isNotNull();
        assertThat(deliverable.getFormula()).isNotNull();
        assertThat(deliverable.getFormula().getExpressionNode()).isNotNull();
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void sumAggregationOfRequirement() {
        Optional<ServiceCategory> serviceCategory =inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        assertThat(AplusRT).isNotNull();
        FullySpecifiedReadingTypeRequirement aPlus = config.newReadingTypeRequirement("AplusRT").withReadingType(AplusRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.sum(AggregationLevel.DAY, builder.requirement(aPlus)));

        // Asserts
        assertThat(deliverable).isNotNull();
        assertThat(deliverable.getFormula()).isNotNull();
        assertThat(deliverable.getFormula().getExpressionNode()).isNotNull();
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void minimumAggregationFunctionCallNodeWithoutAggregationLevel() {
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        try {
            // Business method
            builder.minimum(null, Collections.emptyList()).create();
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.AGGREGATION_FUNCTION_REQUIRES_AGGREGATION_LEVEL);
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void minimumAggregationFunctionCallNodeWithoutArguments() {
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        try {
            // Business method
            builder.minimum(AggregationLevel.HOUR, Collections.emptyList()).create();
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INVALID_ARGUMENTS_AT_LEAST_ONE_CHILD_REQUIRED);
            throw e;
        }
    }

}