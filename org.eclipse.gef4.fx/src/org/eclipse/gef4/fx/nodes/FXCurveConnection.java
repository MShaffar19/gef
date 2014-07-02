/*******************************************************************************
 * Copyright (c) 2013 itemis AG and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.gef4.fx.nodes;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import org.eclipse.gef4.fx.anchors.AnchorKey;
import org.eclipse.gef4.fx.anchors.AnchorLink;
import org.eclipse.gef4.fx.anchors.FXChopBoxAnchor;
import org.eclipse.gef4.fx.anchors.IFXAnchor;
import org.eclipse.gef4.geometry.convert.fx.JavaFX2Geometry;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Polyline;

public class FXCurveConnection extends AbstractFXConnection<ICurve> {

	private MapChangeListener<? super AnchorKey, ? super Point> startPCL = new MapChangeListener<AnchorKey, Point>() {
		@Override
		public void onChanged(
				javafx.collections.MapChangeListener.Change<? extends AnchorKey, ? extends Point> change) {
			updateEndReferencePoint();
		}
	};

	private MapChangeListener<? super AnchorKey, ? super Point> endPCL = new MapChangeListener<AnchorKey, Point>() {
		@Override
		public void onChanged(
				javafx.collections.MapChangeListener.Change<? extends AnchorKey, ? extends Point> change) {
			updateStartReferencePoint();
		}
	};

	private ChangeListener<? super AnchorLink> onStartAnchorLinkChange = new ChangeListener<AnchorLink>() {
		@Override
		public void changed(ObservableValue<? extends AnchorLink> observable,
				AnchorLink oldLink, AnchorLink newLink) {
			if (newLink != null) {
				IFXAnchor anchor = newLink.getAnchor();
				if (anchor != null) {
					anchor.positionProperty().addListener(startPCL);
				}
				updateStartReferencePoint();
			}
		}
	};

	private ChangeListener<? super AnchorLink> onEndAnchorLinkChange = new ChangeListener<AnchorLink>() {
		@Override
		public void changed(ObservableValue<? extends AnchorLink> observable,
				AnchorLink oldLink, AnchorLink newLink) {
			if (newLink != null) {
				IFXAnchor anchor = newLink.getAnchor();
				if (anchor != null) {
					anchor.positionProperty().addListener(endPCL);
				}
				updateEndReferencePoint();
			}
		}
	};

	private ListChangeListener<? super Point> onWayPointChange = new ListChangeListener<Point>() {
		@Override
		public void onChanged(
				javafx.collections.ListChangeListener.Change<? extends Point> c) {
			updateStartReferencePoint();
			updateEndReferencePoint();
		}
	};

	public FXCurveConnection() {
		setOnStartAnchorLinkChange(onStartAnchorLinkChange);
		setOnEndAnchorLinkChange(onEndAnchorLinkChange);
		setOnWayPointChange(onWayPointChange);
	}

	@Override
	public ICurve computeGeometry(Point[] points) {
		return new Polyline(points);
	}

	/**
	 * Returns a {@link Point} array containing reference points for the start
	 * and end anchors.
	 * 
	 * @return
	 */
	public Point[] computeReferencePoints() {
		// find reference points
		Point startReference = null;
		Point endReference = null;
		List<Point> wayPoints = getWayPoints();

		// first uncontained way point is start reference
		Node startNode = startAnchorLinkProperty.get().getAnchor()
				.getAnchorageNode();
		if (startNode != null) {
			for (Point p : wayPoints) {
				Point2D local = startNode.sceneToLocal(localToScene(p.x, p.y));
				if (!startNode.contains(local)) {
					startReference = p;
					break;
				}
			}
		}

		// last uncontained way point is end reference
		Node endNode = endAnchorLinkProperty.get().getAnchor()
				.getAnchorageNode();
		if (endNode != null) {
			for (int i = wayPoints.size() - 1; i >= 0; i--) {
				Point p = wayPoints.get(i);
				Point2D local = endNode.sceneToLocal(localToScene(p.x, p.y));
				if (!endNode.contains(local)) {
					endReference = p;
					break;
				}
			}
		}

		// if we did not find a startReference yet, we have to use the end
		// anchorage position or end anchor position
		if (startReference == null) {
			if (isEndConnected()) {
				Node anchorageNode = getEndAnchorLink().getAnchor()
						.getAnchorageNode();
				if (anchorageNode != null) {
					startReference = getCenter(anchorageNode);
				}
			}
		}
		if (startReference == null) {
			startReference = getEndPoint();
		}
		if (startReference == null) {
			startReference = new Point();
		}

		// if we did not find an endReference yet, we have to use the start
		// anchorage position or start anchor position
		if (endReference == null) {
			if (isStartConnected()) {
				Node anchorageNode = getStartAnchorLink().getAnchor()
						.getAnchorageNode();
				if (anchorageNode != null) {
					endReference = getCenter(anchorageNode);
				}
			}
		}
		if (endReference == null) {
			endReference = getStartPoint();
		}
		if (endReference == null) {
			endReference = new Point();
		}

		return new Point[] { startReference, endReference };
	}

	private Point getCenter(Node anchorageNode) {
		Point center = JavaFX2Geometry.toRectangle(
				anchorageNode.localToScene(anchorageNode.getLayoutBounds()))
				.getCenter();
		if (Double.isNaN(center.x) || Double.isNaN(center.y)) {
			return null;
		}
		return center;
	}

	private void updateEndReferencePoint() {
		AnchorLink anchorLink = endAnchorLinkProperty.get();
		if (anchorLink == null) {
			return;
		}
		IFXAnchor endAnchor = anchorLink.getAnchor();
		if (endAnchor instanceof FXChopBoxAnchor) {
			FXChopBoxAnchor a = (FXChopBoxAnchor) endAnchor;
			Point[] refPoints = computeReferencePoints();
			AnchorKey key = endAnchorLinkProperty.get().getKey();
			Point oldRef = a.getReferencePoint(key);
			if (oldRef == null || !oldRef.equals(refPoints[1])) {
				a.setReferencePoint(key, refPoints[1]);
			}
		}
	}

	private void updateStartReferencePoint() {
		AnchorLink anchorLink = startAnchorLinkProperty.get();
		if (anchorLink == null) {
			return;
		}
		IFXAnchor startAnchor = anchorLink.getAnchor();
		if (startAnchor instanceof FXChopBoxAnchor) {
			FXChopBoxAnchor a = (FXChopBoxAnchor) startAnchor;
			Point[] refPoints = computeReferencePoints();
			AnchorKey key = startAnchorLinkProperty.get().getKey();
			Point oldRef = a.getReferencePoint(key);
			if (oldRef == null || !oldRef.equals(refPoints[0])) {
				a.setReferencePoint(key, refPoints[0]);
			}
		}
	}

}
