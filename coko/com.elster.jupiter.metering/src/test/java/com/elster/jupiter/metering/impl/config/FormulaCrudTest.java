/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
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
import com.elster.jupiter.metering.impl.TableSpecs;
import com.elster.jupiter.metering.impl.aggregation.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.util.time.Interval;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Before
    public void before() {
        inMemoryBootstrapModule.getOrmService().invalidateCache(MeteringService.COMPONENTNAME, TableSpecs.MTR_READINGTYPE.name());
    }

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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.1.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.1.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype"));
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
        List<? extends ExpressionNode> children = functionCallNode.getChildren();
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
        List<? extends ExpressionNode> children = functionCallNode.getChildren();
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

        ServerExpressionNode node = new ExpressionNodeParser(thesaurus, service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode).parse("constant(10)");

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

        ServerExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode).parse("max(constant(10), constant(0))");

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
        List<? extends ExpressionNode> children = functionCallNode.getChildren();
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

        ServerExpressionNode node = new ExpressionNodeParser(thesaurus, service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode).parse("max(constant(1), plus(constant(2), constant(3)))");

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
        List<? extends ExpressionNode> children = functionCallNode.getChildren();
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
            new ExpressionNodeParser(service.getThesaurus(), service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode).parse("maxOf(max(constant(1), constant(2)), plus(constant(2), constant(3)))");
        } catch (InvalidNodeException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.FUNCTION_NOT_ALLOWED_IN_AUTOMODE);
            assertThat(e.get("Function")).isEqualTo(Function.MAX_AGG);
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
        ServerExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode).parse("max(constant(1), min(constant(2), constant(3), constant(4)))");

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
        List<Formula> formulasBefore = service.findFormulas();
        String formulaString = "multiply(sum(hour, max(hour, constant(10), constant(0)), constant(5), constant(3)), constant(2))";
        ServerExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, inMemoryBootstrapModule.getCustomPropertySetService(), config, Formula.Mode.EXPERT).parse(formulaString);
        service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();

        List<Formula> formulas = service.findFormulas();
        assertThat(formulas.size()).isEqualTo(formulasBefore.size() + 1);
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
        ServerFormula myFormula = (ServerFormula) loadedFormula.get();

        ServerExpressionNode newExpression = builder.constant(99).create();
        myFormula.updateExpression(newExpression);

        Optional<Formula> reloadedFormula = service.findFormula(formulaId);
        assertThat(reloadedFormula).isPresent();
        ServerFormula reloadedServerFormula = (ServerFormula) reloadedFormula.get();
        assertThat(reloadedServerFormula.getId()).isEqualTo(formulaId);
        assertThat(reloadedServerFormula.getMode()).isEqualTo(Formula.Mode.EXPERT);
        ExpressionNode myNode = reloadedServerFormula.getExpressionNode();
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
            assertThat(e.getMessage()).isEqualTo("The requirement with id '" + req.getId() + "' cannot be used because it has a different metrology configuration.");
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
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype for deliverable");
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
                        "11.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype for deliverable2");
        assertThat(readingTypeDeliverable).isNotNull();

        ReadingTypeDeliverableBuilder builder =
                config.newReadingTypeDeliverable("deliverable", readingTypeDeliverable, Formula.Mode.AUTO);

        try {
            builder.build(builder.requirement(req));
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .startsWith(
                    "The readingtype \"" + readingTypeDeliverable.getMRID() + " (" + readingTypeDeliverable.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable");
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
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
            deliverable1.startUpdate().setReadingType(temperatureRT).complete();
            fail("ConstraintViolationException expected");
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo(
                    "The readingtype \"" + temperatureRT.getMRID() + " (" + temperatureRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable1.getName() + " = " + deliverable1.getFormula().getExpressionNode().toString()
                            + "\".");
        }

        ReadingType conskWhMonthlyRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhMonthlyRT");

        try {
            deliverable1.startUpdate().setReadingType(conskWhMonthlyRT).complete();
            assertThat(deliverable1.getReadingType()).isEqualTo(conskWhMonthlyRT);
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh"));

        ReadingType conskWhRT60min =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhRT60min"));

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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "temp"));

        try {
            deliverable1.startUpdate().setReadingType(temperatureRT).complete();
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo(
                    "The readingtype \"" + temperatureRT.getMRID() + " (" + temperatureRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable1.getName() + " = " + deliverable1.getFormula().getExpressionNode().toString()
                            + "\".");
        }

        ReadingType conskWhMonthlyRT =
                inMemoryBootstrapModule.getMeteringService()
                        .getReadingType(
                                "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0")
                        .orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                                "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhMonthlyRT"));

        try {
            deliverable1.startUpdate().setReadingType(conskWhMonthlyRT).complete();
            assertThat(deliverable1.getReadingType()).isEqualTo(conskWhMonthlyRT);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
    }

    @Test
    @Transactional
    public void testIrregularDeliverableWithConstantsOnly() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType irregularRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.72.0", "irregularRT");
        assertThat(irregularRT).isNotNull();
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Irregular", irregularRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        // Asserts
        assertThat(deliverable).isNotNull();
    }

    @Test
    @Transactional
    public void testIrregularDeliverableWithIrregularRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType irregularRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.72.0", "irregularRT");
        FullySpecifiedReadingTypeRequirement regularRequirement = config.newReadingTypeRequirement("Irregular").withReadingType(irregularRT);
        assertThat(irregularRT).isNotNull();
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("IrregularDelOnIregularReq", irregularRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.requirement(regularRequirement));

        // Asserts
        assertThat(deliverable).isNotNull();
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void testIrregularDeliverableWithRegularRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regularRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh"));
        FullySpecifiedReadingTypeRequirement regularRequirement = config.newReadingTypeRequirement("Regular").withReadingType(regularRT);
        ReadingType irregularRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.2.12.0.0.0.0.0.0.0.0.0.72.0", "irregularRT");
        assertThat(irregularRT).isNotNull();
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Irregular", irregularRT, Formula.Mode.AUTO);

        try {
            // Business method
            builder.build(builder.requirement(regularRequirement));
        } catch (ConstraintViolationException e) {
            // Asserts
            assertThat(e.getMessage()).contains(MessageSeeds.IRREGULAR_READING_TYPE_DELIVERABLE_ONLY_SUPPORTS_SIMPLE_FORMULAS.getDefaultFormat());
            throw e;
        }
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void testRegularDeliverableWithIrregularRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory).isPresent();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType regularRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh"));
        ReadingType irregularRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.2.12.0.0.0.0.0.0.0.0.0.72.0", "irregularRT");
        FullySpecifiedReadingTypeRequirement regularRequirement = config.newReadingTypeRequirement("Irregular").withReadingType(irregularRT);
        assertThat(irregularRT).isNotNull();
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Regular", regularRT, Formula.Mode.AUTO);

        try {
            // Business method
            builder.build(builder.requirement(regularRequirement));
        } catch (ConstraintViolationException e) {
            // Asserts
            assertThat(e.getMessage()).contains(MessageSeeds.REGULAR_READING_TYPE_DELIVERABLE_DOES_NOT_SUPPORT_IRREGULAR_REQUIREMENTS.getDefaultFormat());
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "monthly"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo("The interval of the output reading type should be larger or equal to interval of the requirements in the formula.");
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "monthly"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.5.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "thirtyMinTR"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

        ReadingType sixtyMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "sixtyMinRT"));

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
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo("The interval of the output reading type should be larger or equal to interval of the requirements in the formula.");
            throw e;
        }
    }

    @Test(expected = ConstraintViolationException.class)
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.5.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "thirtyMinTR"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo("MINUTE15 values cannot be aggregated to MINUTE30 values.");
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.5.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "thirtyMinTR"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo("MINUTE15 values cannot be aggregated to MINUTE30 values.");
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.5.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.5.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "thirtyMinTR"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo("The interval of the output reading type should be larger or equal to interval of the requirements in the formula.");
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "monthly"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

        ReadingType sixtyMinTR =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "60MinTR"));


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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh"));

        ReadingType conskWhRT60min =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhRT60min"));

        assertThat(conskWhRT15min).isNotNull();
        assertThat(conskWhRT60min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Deliverable1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));

        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Deliverable2", conskWhRT60min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable2 = builder2.build(builder2.deliverable(deliverable1));

        ReadingType temperatureRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "temp"));

        try {
            deliverable1.startUpdate().setReadingType(temperatureRT).complete();
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo(
                    "The readingtype \"" + conskWhRT60min.getMRID() + " (" + conskWhRT60min.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable2.getName() + " = " + deliverable2.getFormula().getExpressionNode().toString()
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh"));
        assertThat(conskWhRT15min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Deliverable1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));

        try {
            ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Deliverable2", conskWhRT15min, Formula.Mode.EXPERT);
            builder2.build(builder2.deliverable(deliverable1));
        } catch (InvalidNodeException e) {
            assertThat(e.getMessage()).isEqualTo("Auto mode and export mode cannot be combined.");
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh"));
        assertThat(conskWhRT15min).isNotNull();
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements()).hasSize(1);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Deliverable1", conskWhRT15min, Formula.Mode.EXPERT);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));

        try {
            ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Deliverable2", conskWhRT15min, Formula.Mode.AUTO);
            builder2.build(builder2.deliverable(deliverable1));
        } catch (InvalidNodeException e) {
            assertThat(e.getMessage()).isEqualTo("Auto mode and export mode cannot be combined.");
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

        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh"));

        assertThat(conskWhRT15min).isNotNull();
        FullySpecifiedReadingTypeRequirement requirement = config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        try {
            ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("conskWhRT15min", conskWhRT15min, Formula.Mode.EXPERT);
            builder.build(builder.plus(builder.maximum(AggregationLevel.DAY, builder.requirement(requirement)), builder.maximum(AggregationLevel.MONTH, builder.requirement(requirement))));
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo("All aggregation functions must use the same aggregation level argument.");
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
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo("The readingtype for the deliverable is not valid, it should represent a numerical value.");
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
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INVALID_READINGTYPE_UNIT_IN_REQUIREMENT);
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
            new ExpressionNodeParser(service.getThesaurus(), service, inMemoryBootstrapModule.getCustomPropertySetService(), config, Formula.Mode.AUTO).parse("R(" + req.getId() + ")");
        } catch (InvalidNodeException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INVALID_READINGTYPE_UNIT_IN_REQUIREMENT);
            throw e;
        }
    }

    @Test(expected = VerboseConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void testBulkReadingTypeInRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType bulkRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.5.1.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Bulk");
        assertThat(bulkRT).isNotNull();
        ReadingType regRT  = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.5.4.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Regular");
        assertThat(regRT).isNotNull();

        FullySpecifiedReadingTypeRequirement bulkRequirement = config.newReadingTypeRequirement("Bulk").withReadingType(bulkRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del3", regRT, Formula.Mode.AUTO);
        try {
            builder.build(builder.requirement(bulkRequirement));
        } catch (VerboseConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo(MessageSeeds.BULK_READINGTYPE_NOT_ALLOWED.getDefaultFormat());
            throw e;
        }
    }

    @Test(expected = VerboseConstraintViolationException.class)
    @Transactional
    // formula = Requirement
    public void testBulkReadingTypeDeliverableWithDeltaRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType bulkRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.5.1.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Bulk");
        assertThat(bulkRT).isNotNull();
        ReadingType deltaRT  = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.5.4.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Delta");
        assertThat(deltaRT).isNotNull();

        FullySpecifiedReadingTypeRequirement deltaRequirement = config.newReadingTypeRequirement("Delta").withReadingType(deltaRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del3", bulkRT, Formula.Mode.AUTO);
        try {
            builder.build(builder.requirement(deltaRequirement));
        } catch (VerboseConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo(MessageSeeds.BULK_DELIVERABLES_CAN_ONLY_USE_BULK_READINGTYPES.getDefaultFormat());
            throw e;
        }
    }

    @Test(expected = VerboseConstraintViolationException.class)
    @Transactional
    // formula = other deliverable
    public void testBulkReadingTypeInDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType bulkRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.5.1.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Bulk");
        assertThat(bulkRT).isNotNull();
        ReadingType regRT  = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.5.4.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Regular");
        assertThat(regRT).isNotNull();

        ReadingTypeDeliverableBuilder bulkBuilder = config.newReadingTypeDeliverable("Bulk", bulkRT, Formula.Mode.AUTO);
        ReadingTypeDeliverable bulkDeliverable = bulkBuilder.build(bulkBuilder.constant(BigDecimal.TEN));

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("DelUsingBulkDel", regRT, Formula.Mode.AUTO);
        try {
            builder.build(builder.deliverable(bulkDeliverable));
        } catch (VerboseConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo(MessageSeeds.BULK_READINGTYPE_NOT_ALLOWED.getDefaultFormat());
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
            new ExpressionNodeParser(service.getThesaurus(), service, inMemoryBootstrapModule.getCustomPropertySetService(), config, Formula.Mode.AUTO).parse("R(" + req.getId() + ")");
        } catch (InvalidNodeException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INVALID_READINGTYPE_UNIT_IN_REQUIREMENT);
            throw e;
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testBulkReadingTypeInRequirementAllowedForIrregular() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType bulkRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.1.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Bulk");
        assertThat(bulkRT).isNotNull();
        ReadingType regRT  = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.4.0.41.109.0.0.0.0.0.0.0.0.0.72.0", "Regular");
        assertThat(regRT).isNotNull();

        FullySpecifiedReadingTypeRequirement bulkRequirement = config.newReadingTypeRequirement("Bulk").withReadingType(bulkRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del3", regRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable readingTypeDeliverable = builder.build(builder.requirement(bulkRequirement));

        // Asserts
        assertThat(readingTypeDeliverable).isNotNull();
        assertThat(readingTypeDeliverable.getReadingType()).isEqualTo(regRT);
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testBulkReadingTypeInRequirementAllowedForRegularBulkDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        ReadingType bulkRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "Bulk");
        assertThat(bulkRT).isNotNull();

        FullySpecifiedReadingTypeRequirement bulkRequirement = config.newReadingTypeRequirement("Bulk").withReadingType(bulkRT);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del3", bulkRT, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable readingTypeDeliverable = builder.build(builder.requirement(bulkRequirement));

        // Asserts
        assertThat(readingTypeDeliverable).isNotNull();
        assertThat(readingTypeDeliverable.getReadingType()).isEqualTo(bulkRT);
    }

    @Test
    @Transactional
    // formula = max(10, 0) function call + constants
    public void testMinusUsingParser() {
        Formula.Mode myMode = Formula.Mode.AUTO;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerExpressionNode node =
                new ExpressionNodeParser(service.getThesaurus(), service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode)
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo("MINUTE10 values cannot be aggregated to MINUTE15 values.");
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
            assertThat(e.getConstraintViolations().iterator().next().getMessage()).isEqualTo("MINUTE5 values cannot be aggregated to MINUTE10 values.");
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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.7.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.7.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "sixtyMinTR"));

        ReadingType fifteenMinRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.4.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));

        ReadingType fifteenMinWhRT =
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "fifteenMinWhRT"));

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
                inMemoryBootstrapModule.getMeteringService().getReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "fifteenMinRT"));
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
    public void testUpdateReadingTypeOfDeliverableThatIsUsedInAnotherDeliverable2() {
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
            deliverable1.startUpdate().setReadingType(incompatibleRT).complete();
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo(
                    "The readingtype \"" + otherRT.getMRID() + " (" + otherRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable2.getName() + " = " + deliverable2.getFormula().getExpressionNode().toString()
                            + "\".");
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testUpdateReadingTypeOfDeliverableThatIsUsedInAnotherDeliverable3() {
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
            toUpdate.startUpdate().setReadingType(incompatibleRT).complete();
            fail("InvalidNodeException expected");
        } catch (ConstraintViolationException e) {
            assertThat(e.getConstraintViolations().iterator().next().getMessage())
                .isEqualTo(
                    "The readingtype \"" + otherRT.getMRID() + " (" + otherRT.getFullAliasName() +
                            ")\" is not compatible with the dimension of the formula of deliverable \"" +
                            deliverable2.getName() + " = " + deliverable2.getFormula().getExpressionNode().toString()
                            + "\".");
        }
    }

    @Test(expected = ReadingTypeAlreadyUsedOnMetrologyConfiguration.class)
    @Transactional
    // formula = Requirement
    public void testMultipleDeliverableWithSameReadingTypeOnSameMetrologyConfig() {
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

        ServerExpressionNode node = new ExpressionNodeParser(thesaurus, service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode).parse(
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
        assertThat(((ConstantNode) operationNode.getLeftOperand()).getValue()).isEqualTo(BigDecimal.TEN);
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

        ServerExpressionNode node = new ExpressionNodeParser(thesaurus, service, inMemoryBootstrapModule.getCustomPropertySetService(), config, myMode).parse(
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
        assertThat(((ConstantNode) operationNode.getLeftOperand()).getValue()).isEqualTo(BigDecimal.TEN);
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
        assertThat(((ConstantNode) operationNode.getLeftOperand()).getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(operationNode.getRightOperand()).isInstanceOf(ConstantNodeImpl.class);
        assertThat(((ConstantNode) operationNode.getRightOperand()).getValue()).isEqualTo(new BigDecimal(20));
        assertThat(operationNode.getChildren().get(2)).isInstanceOf(NullNodeImpl.class);
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void minimumAggregationOfRequirement() {
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
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
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
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
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
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
        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config11", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();

        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .orElseGet(() -> inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT"));
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

    @Test
    public void testQuality() {
        for (int i = 0; i < ReadingQuality.values().length; i++) {
            System.out.println(ReadingQuality.values()[i].toString());
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void customPropertySetNotConfiguredOnMetroglogyConfiguration() {
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = this.getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config12", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config).isNotNull();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("dummy");
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn("customPropertySetNotConfiguredOnMetroglogyConfiguration");
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        try {
            // Business method
            builder.property(customPropertySet, propertySpec);
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.CUSTOM_PROPERTY_SET_NOT_CONFIGURED_ON_METROLOGY_CONFIGURATION);
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void customPropertySetNoLongerActive() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("dummy");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PersistenceSupport persistenceSupport = mock(PersistenceSupport.class);
        when(persistenceSupport.componentName()).thenReturn("TST");
        when(persistenceSupport.addCustomPropertyPrimaryKeyColumnsTo(any(Table.class))).thenReturn(Collections.emptyList());
        when(persistenceSupport.tableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUD");
        when(persistenceSupport.journalTableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUDJRNL");
        when(persistenceSupport.domainColumnName()).thenReturn("usagepoint");
        when(persistenceSupport.domainFieldName()).thenReturn("usagePoint");
        when(persistenceSupport.domainForeignKeyName()).thenReturn("MTR_TST_FK_USAGEPOINT");
        when(persistenceSupport.module()).thenReturn(Optional.empty());
        when(persistenceSupport.persistenceClass()).thenReturn(UsagePointCPS.class);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn("customPropertySetNoLongerActive");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.getPersistenceSupport()).thenReturn(persistenceSupport);
        CustomPropertySetService customPropertySetService = inMemoryBootstrapModule.getCustomPropertySetService();
        customPropertySetService.addCustomPropertySet(customPropertySet);
        RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId()).get();

        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = this.getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config12", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        config.addCustomPropertySet(registeredCustomPropertySet);
        assertThat(config).isNotNull();
        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Now unregister the CustomPropertySet
        customPropertySetService.removeCustomPropertySet(customPropertySet);

        try {
            // Business method
            builder.property(customPropertySet, propertySpec);
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.CUSTOM_PROPERTY_SET_NO_LONGER_ACTIVE);
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void customPropertySetNotVersioned() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("dummy");
        PersistenceSupport persistenceSupport = mock(PersistenceSupport.class);
        when(persistenceSupport.componentName()).thenReturn("TST");
        when(persistenceSupport.addCustomPropertyPrimaryKeyColumnsTo(any(Table.class))).thenReturn(Collections.emptyList());
        when(persistenceSupport.tableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUD");
        when(persistenceSupport.journalTableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUDJRNL");
        when(persistenceSupport.domainColumnName()).thenReturn("usagepoint");
        when(persistenceSupport.domainFieldName()).thenReturn("usagePoint");
        when(persistenceSupport.domainForeignKeyName()).thenReturn("MTR_TST_FK_USAGEPOINT");
        when(persistenceSupport.module()).thenReturn(Optional.empty());
        when(persistenceSupport.persistenceClass()).thenReturn(UsagePointCPS.class);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn("customPropertySetNotVersioned");
        when(customPropertySet.isVersioned()).thenReturn(false);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.getPersistenceSupport()).thenReturn(persistenceSupport);
        CustomPropertySetService customPropertySetService = inMemoryBootstrapModule.getCustomPropertySetService();
        customPropertySetService.addCustomPropertySet(customPropertySet);
        RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId()).get();

        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = this.getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config12", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        config.addCustomPropertySet(registeredCustomPropertySet);
        assertThat(config).isNotNull();
        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        try {
            // Business method
            builder.property(customPropertySet, propertySpec);
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.CUSTOM_PROPERTY_SET_NOT_VERSIONED);
            throw e;
        }
    }

    @Test
    @Transactional
    public void customProperty() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("dummy");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        PersistenceSupport persistenceSupport = mock(PersistenceSupport.class);
        when(persistenceSupport.componentName()).thenReturn("TST");
        when(persistenceSupport.addCustomPropertyPrimaryKeyColumnsTo(any(Table.class))).thenReturn(Collections.emptyList());
        when(persistenceSupport.tableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUD");
        when(persistenceSupport.journalTableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUDJRNL");
        when(persistenceSupport.domainColumnName()).thenReturn("usagepoint");
        when(persistenceSupport.domainFieldName()).thenReturn("usagePoint");
        when(persistenceSupport.domainForeignKeyName()).thenReturn("MTR_TST_FK_USAGEPOINT");
        when(persistenceSupport.module()).thenReturn(Optional.empty());
        when(persistenceSupport.persistenceClass()).thenReturn(UsagePointCPS.class);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn("customProperty");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.getPersistenceSupport()).thenReturn(persistenceSupport);
        CustomPropertySetService customPropertySetService = inMemoryBootstrapModule.getCustomPropertySetService();
        customPropertySetService.addCustomPropertySet(customPropertySet);
        RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId()).get();

        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = this.getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config12", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        config.addCustomPropertySet(registeredCustomPropertySet);
        assertThat(config).isNotNull();
        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Business method
        FormulaBuilder property = builder.property(customPropertySet, propertySpec);
        ReadingTypeDeliverable deliverable = builder.build(property);

        // Asserts
        assertThat(property).isNotNull();
        assertThat(deliverable).isNotNull();
    }

    @Test
    @Transactional
    public void quantityCustomProperty() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("quantity");
        when(propertySpec.getValueFactory()).thenReturn(new QuantityValueFactory());
        PersistenceSupport persistenceSupport = mock(PersistenceSupport.class);
        when(persistenceSupport.componentName()).thenReturn("TST");
        when(persistenceSupport.addCustomPropertyPrimaryKeyColumnsTo(any(Table.class))).thenReturn(Collections.emptyList());
        when(persistenceSupport.tableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUD");
        when(persistenceSupport.journalTableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUDJRNL");
        when(persistenceSupport.domainColumnName()).thenReturn("usagepoint");
        when(persistenceSupport.domainFieldName()).thenReturn("usagePoint");
        when(persistenceSupport.domainForeignKeyName()).thenReturn("MTR_TST_FK_USAGEPOINT");
        when(persistenceSupport.module()).thenReturn(Optional.empty());
        when(persistenceSupport.persistenceClass()).thenReturn(UsagePointCPSWithStringProperty.class);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn("customPropertySetNoLongerActive");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.getPersistenceSupport()).thenReturn(persistenceSupport);
        CustomPropertySetService customPropertySetService = inMemoryBootstrapModule.getCustomPropertySetService();
        customPropertySetService.addCustomPropertySet(customPropertySet);
        RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId()).get();

        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = this.getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config12", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        config.addCustomPropertySet(registeredCustomPropertySet);
        assertThat(config).isNotNull();
        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        // Business method
        FormulaBuilder property = builder.property(customPropertySet, propertySpec);
        ReadingTypeDeliverable deliverable = builder.build(property);

        // Asserts
        assertThat(property).isNotNull();
        assertThat(deliverable).isNotNull();
    }

    @Test(expected = InvalidNodeException.class)
    @Transactional
    public void nonNumericalCustomProperty() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("dummy");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PersistenceSupport persistenceSupport = mock(PersistenceSupport.class);
        when(persistenceSupport.componentName()).thenReturn("TST");
        when(persistenceSupport.addCustomPropertyPrimaryKeyColumnsTo(any(Table.class))).thenReturn(Collections.emptyList());
        when(persistenceSupport.tableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUD");
        when(persistenceSupport.journalTableName()).thenReturn("MTR_TST_CPS_FORMULA_CRUDJRNL");
        when(persistenceSupport.domainColumnName()).thenReturn("usagepoint");
        when(persistenceSupport.domainFieldName()).thenReturn("usagePoint");
        when(persistenceSupport.domainForeignKeyName()).thenReturn("MTR_TST_FK_USAGEPOINT");
        when(persistenceSupport.module()).thenReturn(Optional.empty());
        when(persistenceSupport.persistenceClass()).thenReturn(UsagePointCPSWithStringProperty.class);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn("customPropertySetNoLongerActive");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.getPersistenceSupport()).thenReturn(persistenceSupport);
        CustomPropertySetService customPropertySetService = inMemoryBootstrapModule.getCustomPropertySetService();
        customPropertySetService.addCustomPropertySet(customPropertySet);
        RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId()).get();

        Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        ServerMetrologyConfigurationService service = this.getMetrologyConfigurationService();
        MetrologyConfigurationBuilder metrologyConfigurationBuilder = service.newMetrologyConfiguration("config12", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        config.addCustomPropertySet(registeredCustomPropertySet);
        assertThat(config).isNotNull();
        ReadingType AplusRT = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "AplusRT");
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", AplusRT, Formula.Mode.AUTO);

        try {
            // Business method
            FormulaBuilder property = builder.property(customPropertySet, propertySpec);
            ReadingTypeDeliverable deliverable = builder.build(property);
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.CUSTOM_PROPERTY_MUST_BE_NUMERICAL);
            throw e;
        }
    }

    private static class UsagePointCPS implements PersistentDomainExtension<UsagePoint> {
        @NotNull
        @SuppressWarnings("unused")
        private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = ValueReference.absent();
        @NotNull
        @SuppressWarnings("unused")
        private Reference<UsagePoint> usagePoint = ValueReference.absent();
        private Interval interval;
        private BigDecimal dummy;

        @Override
        public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        }

        @Override
        public void validateDelete() {
        }
    }

    private static class UsagePointCPSWithStringProperty extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
        @NotNull
        @SuppressWarnings("unused")
        private Reference<UsagePoint> usagePoint = ValueReference.absent();
        @Size(max = 125)
        private String dummy;

        @Override
        public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        }

        @Override
        public void validateDelete() {
        }
    }

}