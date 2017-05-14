/*******************************************************************************
 * Copyright (c) 2017 itemis AG and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *     Tamas Miklossy   (itemis AG) - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.gef.dot.internal.language.htmllabel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class contains helper methods used on different places (e.g. Xtext
 * validation, Xtext content assistant support) of the Dot Html-like Label
 * sub-grammar.
 */
public class DotHtmlLabelHelper {
	private static final String ROOT_TAG_KEY = "ROOT";
	private static final Set<String> allTags = new HashSet<>();
	private static final Set<String> selfClosingTags = new HashSet<>();
	private static final Set<String> nonSelfClosingTags = new HashSet<>();
	private static final Map<String, Set<String>> validTags = new HashMap<>();
	private static final Map<String, Set<String>> allowedParents = new HashMap<>();
	private static final Map<String, Set<String>> validAttributes = new HashMap<>();

	static {
		validTags(ROOT_TAG_KEY, // allowed top-level tags
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S", "TABLE");

		validTags("FONT", // allowed tags between <FONT> and </FONT>
				"TABLE", "BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("I", // allowed tags between <I> and </I>
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("B", // allowed tags between <B> and </B>
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("U", // allowed tags between <U> and </U>
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("O", // allowed tags between <O> and </O>
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("SUB", // allowed tags between <SUB> and </SUB>
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("SUP", // allowed tags between <SUP> and </SUP>
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("S", // allowed tags between <S> and </S>
				"BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S");

		validTags("TABLE", // allowed tags between <TABLE> and </TABLE>
				"HR", "TR");

		validTags("TR", // allowed tags between <TR> and </TR>
				"VR", "TD");

		validTags("TD", // allowed tags between <TD> and </TD>
				"IMG", "BR", "FONT", "I", "B", "U", "O", "SUB", "SUP", "S",
				"TABLE");

		// find all tags
		for (Set<String> ts : validTags.values()) {
			allTags.addAll(ts);
		}

		// compute allowed parents for each tag
		for (String tag : allTags) {
			allowedParents.put(tag, new HashSet<>());
		}
		for (String parent : validTags.keySet()) {
			for (String tag : validTags.get(parent)) {
				allowedParents.get(tag).add(parent);
			}
		}

		// specify tags that can have attributes
		for (String t : new String[] { "TABLE", "TD", "FONT", "BR", "IMG" }) {
			validAttributes.put(t, new HashSet<>());
		}
		// add allowed attributes
		validAttributes("TABLE", // allowed <TABLE> tag attributes
				"ALIGN", "BGCOLOR", "BORDER", "CELLBORDER", "CELLPADDING",
				"CELLSPACING", "COLOR", "COLUMNS", "FIXEDSIZE", "GRADIENTANGLE",
				"HEIGHT", "HREF", "ID", "PORT", "ROWS", "SIDES", "STYLE",
				"TARGET", "TITLE", "TOOLTIP", "VALIGN", "WIDTH");

		validAttributes("TD", // allowed <TD> tag attributes
				"ALIGN", "BALIGN", "BGCOLOR", "BORDER", "CELLPADDING",
				"CELLSPACING", "COLOR", "COLSPAN", "FIXEDSIZE", "GRADIENTANGLE",
				"HEIGHT", "HREF", "ID", "PORT", "ROWSPAN", "SIDES", "STYLE",
				"TARGET", "TITLE", "TOOLTIP", "VALIGN", "WIDTH");

		validAttributes("FONT", // allowed <FONT> tag attributes
				"COLOR", "FACE", "POINT-SIZE");

		validAttributes("BR", // allowed <BR> tag attributes
				"ALIGN");

		validAttributes("IMG", // allowed <IMG> tag attributes
				"SCALE", "SRC");

		// specify tags that can be self-closing
		selfClosingTags.addAll(
				Arrays.asList(new String[] { "BR", "HR", "VR", "IMG" }));

		// calculate tags that cannot be self-closing, the difference between
		// the allTags and the selfClosingTags sets.
		nonSelfClosingTags.addAll(allTags);
		nonSelfClosingTags.removeAll(selfClosingTags);
	}

	/**
	 * Specify the valid child tags of a certain html tag.
	 * 
	 * @param tag
	 *            the parent tag to which valid child tags should be specified.
	 * @param childTags
	 *            the list of child tags that are valid within the parent tag.
	 */
	private static void validTags(String tag, String... childTags) {
		validTags.put(tag, new HashSet<String>(Arrays.asList(childTags)));
	}

	/**
	 * Specify the valid attributes of a certain html tag.
	 * 
	 * @param tag
	 *            the tag to which valid attributes should be specified.
	 * @param attributes
	 *            the list of attributes that are valid within the tag.
	 */
	private static void validAttributes(String tag, String... attributes) {
		validAttributes.get(tag).addAll(Arrays.asList(attributes));
	}

	/**
	 * Returns the key representing the root tag.
	 * 
	 * @return The root tag key
	 */
	public static String getRootTagKey() {
		return ROOT_TAG_KEY;
	}

	/**
	 * Returns all allowed html tags.
	 * 
	 * @return The set of all allowed html tags.
	 */
	public static Set<String> getAllTags() {
		return allTags;
	}

	/**
	 * Returns all tags that can be self-closing.
	 * 
	 * @return The set of all tags that can be self-closing.
	 */
	public static Set<String> getSelfClosingTags() {
		return selfClosingTags;
	}

	/**
	 * Returns all tags that cannot be self-closing.
	 * 
	 * @return The set of all tags that cannot be self-closing.
	 */
	public static Set<String> getNonSelfClosingTags() {
		return nonSelfClosingTags;
	}

	/**
	 * Returns the valid tags that can be used as a child tag of a particular
	 * tag.
	 * 
	 * @return A map mapping a particular tag to a set of its allowed children
	 *         tags.
	 */
	public static Map<String, Set<String>> getValidTags() {
		return validTags;
	}

	/**
	 * Returns the allowed parent tags that can be used as a parent tag of a
	 * particular tag.
	 * 
	 * @return A map mapping a particular tag to a set of its allowed parent
	 *         tags.
	 */
	public static Map<String, Set<String>> getAllowedParents() {
		return allowedParents;
	}

	/**
	 * Returns the valid attributes that can be used within a particular tag.
	 * 
	 * @return A map mapping a particular tag to a set of its allowed
	 *         attributes.
	 */
	public static Map<String, Set<String>> getValidAttributes() {
		return validAttributes;
	}

}
