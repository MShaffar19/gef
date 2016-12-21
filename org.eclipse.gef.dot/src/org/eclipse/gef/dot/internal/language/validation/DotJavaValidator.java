/*******************************************************************************
 * Copyright (c) 2009, 2016 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg    - intial Xtext generation (see bug #277380)
 *     Alexander Nyßen - initial implementation
 *     Tamas Miklossy  - Add support for arrowType edge decorations (bug #477980)
 *                     - Add support for polygon-based node shapes (bug #441352)
 *                     - Add support for all dot attributes (bug #461506)
 *
 *******************************************************************************/

package org.eclipse.gef.dot.internal.language.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.dot.internal.DotAttributes;
import org.eclipse.gef.dot.internal.DotImport;
import org.eclipse.gef.dot.internal.DotLanguageSupport;
import org.eclipse.gef.dot.internal.DotLanguageSupport.Context;
import org.eclipse.gef.dot.internal.DotLanguageSupport.IAttributeValueParser;
import org.eclipse.gef.dot.internal.DotLanguageSupport.IAttributeValueValidator;
import org.eclipse.gef.dot.internal.language.dot.AttrList;
import org.eclipse.gef.dot.internal.language.dot.AttrStmt;
import org.eclipse.gef.dot.internal.language.dot.Attribute;
import org.eclipse.gef.dot.internal.language.dot.DotGraph;
import org.eclipse.gef.dot.internal.language.dot.DotPackage;
import org.eclipse.gef.dot.internal.language.dot.EdgeOp;
import org.eclipse.gef.dot.internal.language.dot.EdgeRhsNode;
import org.eclipse.gef.dot.internal.language.dot.EdgeRhsSubgraph;
import org.eclipse.gef.dot.internal.language.dot.GraphType;
import org.eclipse.gef.dot.internal.language.dot.NodeStmt;
import org.eclipse.gef.dot.internal.language.shape.PolygonBasedNodeShape;
import org.eclipse.gef.dot.internal.language.style.NodeStyle;
import org.eclipse.gef.dot.internal.language.terminals.ID;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

/**
 * Provides DOT-specific validation rules.
 * 
 * @author anyssen
 *
 */
public class DotJavaValidator extends AbstractDotJavaValidator {

	/**
	 * Checks that within an {@link Attribute} only valid attribute values are
	 * used (dependent on context, in which the attribute is specified).
	 * 
	 * @param attribute
	 *            The {@link Attribute} to validate.
	 */
	@Check
	public void checkValidAttributeValue(final Attribute attribute) {
		List<Diagnostic> diagnostics = validateAttributeValue(
				DotLanguageSupport.getContext(attribute),
				attribute.getName().toValue(), attribute.getValue().toValue());
		for (Diagnostic d : diagnostics) {
			if (d.getSeverity() == Diagnostic.ERROR) {
				getMessageAcceptor().acceptError(d.getMessage(), attribute,
						DotPackage.Literals.ATTRIBUTE__VALUE,
						INSIGNIFICANT_INDEX, attribute.getName().toValue(),
						attribute.getValue().toValue());
			} else if (d.getSeverity() == Diagnostic.WARNING) {
				getMessageAcceptor().acceptWarning(d.getMessage(), attribute,
						DotPackage.Literals.ATTRIBUTE__VALUE,
						INSIGNIFICANT_INDEX, attribute.getName().toValue(),
						attribute.getValue().toValue());
			} else if (d.getSeverity() == Diagnostic.INFO) {
				getMessageAcceptor().acceptInfo(d.getMessage(), attribute,
						DotPackage.Literals.ATTRIBUTE__VALUE,
						INSIGNIFICANT_INDEX, attribute.getName().toValue(),
						attribute.getValue().toValue());
			}
		}
	}

	/**
	 * Validate the attribute determined via name and value syntactically and
	 * semantically.
	 * 
	 * @param context
	 *            The context element the attribute is related to.
	 * @param name
	 *            The name of the attribute.
	 * @param value
	 *            The value of the attribute.
	 * @return A list of {@link Diagnostic} objects representing the identified
	 *         issues, or an empty list if no issues were found.
	 */
	public List<Diagnostic> validateAttributeValue(final Context context,
			final String name, final String value) {
		// use parser (and validator) for respective attribute type
		if (DotAttributes.FORCELABELS__G.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.BOOL_PARSER, null,
					DotAttributes.FORCELABELS__G, value, "bool");
		} else if (DotAttributes.FIXEDSIZE__N.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.BOOL_PARSER, null,
					DotAttributes.FIXEDSIZE__N, value, "bool");
		} else if (DotAttributes.CLUSTERRANK__G.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.CLUSTERMODE_PARSER, null, name, value,
					"clusterMode");
		} else if (DotAttributes.OUTPUTORDER__G.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.OUTPUTMODE_PARSER, null, name, value,
					"outputMode");
		} else if (DotAttributes.PAGEDIR__G.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.PAGEDIR_PARSER, null, name, value,
					"pagedir");
		} else if (DotAttributes.RANKDIR__G.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.RANKDIR_PARSER, null, name, value,
					"rankdir");
		} else if (DotAttributes.SPLINES__G.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.SPLINES_PARSER, null, name, value,
					"splines");
		} else if (DotAttributes.LAYOUT__G.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.LAYOUT_PARSER, null, name, value,
					"layout");
		} else if (DotAttributes.DIR__E.equals(name)) {
			// dirType enum
			return validateAttributeValue(context,
					DotLanguageSupport.DIRTYPE_PARSER, null, name, value,
					"dirType");
		} else if (DotAttributes.ARROWHEAD__E.equals(name)
				|| DotAttributes.ARROWTAIL__E.equals(name)) {
			// validate arrowtype using delegate parser and validator
			return validateAttributeValue(context,
					DotLanguageSupport.ARROWTYPE_PARSER,
					DotLanguageSupport.ARROWTYPE_VALIDATOR, name, value,
					"arrowType");
		} else if (DotAttributes.ARROWSIZE__E.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.DOUBLE_PARSER,
					DotLanguageSupport.ARROWSIZE_VALIDATOR, name, value,
					"double");
		} else if (DotAttributes.POS__NE.equals(name)) {
			// validate point (node) or splinetype (edge) using delegate parser
			// and validator
			if (Context.NODE.equals(context)) {
				return validateAttributeValue(context,
						DotLanguageSupport.POINT_PARSER,
						DotLanguageSupport.POINT_VALIDATOR, name, value,
						"point");
			} else if (Context.EDGE.equals(context)) {
				return validateAttributeValue(context,
						DotLanguageSupport.SPLINETYPE_PARSER,
						DotLanguageSupport.SPLINETYPE_VALIDATOR, name, value,
						"splineType");
			}
		} else if (DotAttributes.SHAPE__N.equals(name)) {
			// validate shape using delegate parser and validator
			return validateAttributeValue(context,
					DotLanguageSupport.SHAPE_PARSER,
					DotLanguageSupport.SHAPE_VALIDATOR, name, value, "shape");
		} else if (DotAttributes.SIDES__N.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.INT_PARSER,
					DotLanguageSupport.SIDES_VALIDATOR, name, value, "int");
		} else if (DotAttributes.SKEW__N.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.DOUBLE_PARSER,
					DotLanguageSupport.SKEW_VALIDATOR, name, value, "double");
		} else if (DotAttributes.DISTORTION__N.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.DOUBLE_PARSER,
					DotLanguageSupport.DISTORTION_VALIDATOR, name, value,
					"double");
		} else if (DotAttributes.WIDTH__N.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.DOUBLE_PARSER,
					DotLanguageSupport.WIDTH_VALIDATOR, name, value, "double");
		} else if (DotAttributes.HEIGHT__N.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.DOUBLE_PARSER,
					DotLanguageSupport.HEIGHT_VALIDATOR, name, value, "double");
		} else if (DotAttributes.STYLE__GNE.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.STYLE_PARSER,
					DotLanguageSupport.STYLE_VALIDATOR, name, value, "style");
		} else if (DotAttributes.HEAD_LP__E.equals(name)
				|| DotAttributes.LP__GE.equals(name)
				|| DotAttributes.TAIL_LP__E.equals(name)
				|| DotAttributes.XLP__NE.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.POINT_PARSER,
					DotLanguageSupport.POINT_VALIDATOR, name, value, "point");
		} else if (DotAttributes.BGCOLOR__G.equals(name)
				|| DotAttributes.COLOR__NE.equals(name)
				|| DotAttributes.FILLCOLOR__NE.equals(name)
				|| DotAttributes.FONTCOLOR__GNE.equals(name)
				|| DotAttributes.LABELFONTCOLOR__E.equals(name)) {
			return validateAttributeValue(context,
					DotLanguageSupport.COLOR_PARSER,
					DotLanguageSupport.COLOR_VALIDATOR, name, value, "color");
		} else if (DotAttributes.COLORSCHEME__GNE.equals(name)) {
			return validateAttributeValue(context, null,
					DotLanguageSupport.COLORSCHEME_VALIDATOR, name, value,
					"colorscheme");
		}
		return Collections.emptyList();
	}

	/**
	 * Ensures that the 'striped' node style is used only for
	 * rectangularly-shaped nodes ('box', 'rect', 'rectangle' and 'square').
	 * 
	 * @param attribute
	 *            The node style attribute to validate.
	 */
	@Check
	public void checkValidCombinationOfNodeShapeAndStyle(Attribute attribute) {
		if (DotLanguageSupport.getContext(attribute) == Context.NODE
				&& attribute.getName().toValue()
						.equals(DotAttributes.STYLE__GNE)
				&& attribute.getValue().toValue()
						.equals(NodeStyle.STRIPED.toString())) {
			EList<AttrList> attributeList = null;
			NodeStmt node = EcoreUtil2.getContainerOfType(attribute,
					NodeStmt.class);
			if (node != null) {
				attributeList = node.getAttrLists();
			} else {
				AttrStmt attrStmt = EcoreUtil2.getContainerOfType(attribute,
						AttrStmt.class);
				if (attrStmt != null) {
					attributeList = attrStmt.getAttrLists();
				}
			}

			if (attributeList != null) {
				// TODO: DotImport should not be referenced here
				ID shapeValue = DotImport.getAttributeValue(attributeList,
						DotAttributes.SHAPE__N);
				// if the shape value is not explicitly set, use the default
				// shape value for evaluation
				if (shapeValue == null) {
					shapeValue = ID.fromString(
							PolygonBasedNodeShape.ELLIPSE.toString());
				}
				switch (PolygonBasedNodeShape.get(shapeValue.toValue())) {
				case BOX:
				case RECT:
				case RECTANGLE:
				case SQUARE:
					break;
				default:
					error("The style 'striped' is only supported with clusters and rectangularly-shaped nodes, such as 'box', 'rect', 'rectangle', 'square'.",
							DotPackage.eINSTANCE.getAttribute_Value());
				}
			}
		}
	}

	/**
	 * Ensures that within {@link EdgeRhsNode}, '-&gt;' is used in directed
	 * graphs, while '--' is used in undirected graphs.
	 * 
	 * @param edgeRhsNode
	 *            The EdgeRhsNode to validate.
	 */
	@Check
	public void checkEdgeOpCorrespondsToGraphType(EdgeRhsNode edgeRhsNode) {
		checkEdgeOpCorrespondsToGraphType(edgeRhsNode.getOp(), EcoreUtil2
				.getContainerOfType(edgeRhsNode, DotGraph.class).getType());
	}

	/**
	 * Ensures that within {@link EdgeRhsSubgraph} '-&gt;' is used in directed
	 * graphs, while '--' is used in undirected graphs.
	 * 
	 * @param edgeRhsSubgraph
	 *            The EdgeRhsSubgraph to validate.
	 */
	@Check
	public void checkEdgeOpCorrespondsToGraphType(
			EdgeRhsSubgraph edgeRhsSubgraph) {
		checkEdgeOpCorrespondsToGraphType(edgeRhsSubgraph.getOp(), EcoreUtil2
				.getContainerOfType(edgeRhsSubgraph, DotGraph.class).getType());
	}

	private void checkEdgeOpCorrespondsToGraphType(EdgeOp edgeOp,
			GraphType graphType) {
		boolean edgeDirected = edgeOp.equals(EdgeOp.DIRECTED);
		boolean graphDirected = graphType.equals(GraphType.DIGRAPH);
		if (graphDirected && !edgeDirected) {
			error("EdgeOp '--' may only be used in undirected graphs.",
					DotPackage.eINSTANCE.getEdgeRhs_Op());

		} else if (!graphDirected && edgeDirected) {
			error("EdgeOp '->' may only be used in directed graphs.",
					DotPackage.eINSTANCE.getEdgeRhs_Op());
		}
	}

	private static Diagnostic createSyntacticAttributeValueProblem(
			String attributeValue, String attributeTypeName,
			String parserMessage, String issueCode) {
		return new FeatureBasedDiagnostic(Diagnostic.ERROR,
				"The value '" + attributeValue
						+ "' is not a syntactically correct "
						+ attributeTypeName + ": " + parserMessage,
				null /* current object */, DotPackage.Literals.ATTRIBUTE__VALUE,
				ValidationMessageAcceptor.INSIGNIFICANT_INDEX, CheckType.NORMAL,
				issueCode, attributeValue);
	}

	private String getFormattedSyntaxErrorMessages(
			IAttributeValueParser.IParseResult<?> parseResult) {
		StringBuilder sb = new StringBuilder();
		for (Diagnostic d : parseResult.getSyntaxErrors()) {
			String message = d.getMessage();
			if (!message.isEmpty()) {
				if (sb.length() != 0) {
					sb.append(" ");
				}
				sb.append(message.substring(0, 1).toUpperCase()
						+ message.substring(1)
						+ (message.endsWith(".") ? "" : "."));
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private <T> List<Diagnostic> validateAttributeValue(
			Context attributeContext, final IAttributeValueParser<T> parser,
			final IAttributeValueValidator<T> validator,
			final String attributeName, final String attributeValue,
			final String attributeTypeName) {
		// parse value first (if a parser is given); otherwise take the (String)
		// value
		T parsedValue = null;
		if (parser != null) {
			IAttributeValueParser.IParseResult<T> parseResult = parser
					.parse(attributeValue);
			if (parseResult.hasSyntaxErrors()) {
				// handle syntactical problems
				return Collections.<Diagnostic> singletonList(
						createSyntacticAttributeValueProblem(attributeValue,
								attributeTypeName,
								getFormattedSyntaxErrorMessages(parseResult),
								attributeName));
			}
			parsedValue = parseResult.getParsedValue();
		} else {
			// for string values there is no parser
			parsedValue = (T) attributeValue;
		}

		// handle semantical problems
		List<Diagnostic> diagnostics = new ArrayList<>();
		if (validator != null) {
			final List<Diagnostic> validationResults = validator
					.validate(attributeContext, parsedValue);
			for (Diagnostic r : validationResults) {
				diagnostics.add(createSemanticAttributeValueProblem(
						r.getSeverity(), attributeValue, attributeTypeName,
						r.getMessage(), attributeName));
			}
		}
		return diagnostics;
	}

	private static Diagnostic createSemanticAttributeValueProblem(int severity,
			String attributeValue, String attributeTypeName,
			String validatorMessage, String issueCode) {
		return new FeatureBasedDiagnostic(severity,
				"The " + attributeTypeName + " value '" + attributeValue
						+ "' is not semantically correct: " + validatorMessage,
				null /* current object */, DotPackage.Literals.ATTRIBUTE__VALUE,
				ValidationMessageAcceptor.INSIGNIFICANT_INDEX, CheckType.NORMAL,
				issueCode, attributeValue);
	}
}
