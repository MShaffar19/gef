/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API & implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.zest.fx.parts;

import org.eclipse.gef4.zest.fx.policies.HideFirstAnchorageOnClickPolicy;

import javafx.scene.image.Image;

/**
 * The {@link HideHandlePart} is an {@link AbstractHidingHandlePart}
 * that displays a "collapse" image. By default, the
 * {@link HideFirstAnchorageOnClickPolicy} is installed for
 * {@link HideHandlePart}, so that the corresponding
 * {@link NodeContentPart} can be hidden by a click on this part.
 *
 * @author mwienand
 *
 */
public class HideHandlePart extends AbstractHidingHandlePart {

	/**
	 * The url to the image that is displayed when hovered this part.
	 */
	public static final String IMG_HIDE = "/collapseall.png";

	/**
	 * The url to the image that is displayed when not hovering this part.
	 */
	public static final String IMG_HIDE_DISABLED = "/collapseall_disabled.png";

	@Override
	protected Image getHoverImage() {
		return new Image(IMG_HIDE);
	}

	@Override
	protected Image getImage() {
		return new Image(IMG_HIDE_DISABLED);
	}

}