/*******************************************************************************
 * Copyright (c) 2016 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG)  - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.common.beans.property;

import java.util.Map;

import org.eclipse.gef4.common.beans.binding.MapExpressionHelperEx;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;

/**
 * A replacement for {@link SimpleMapProperty} to fix the following JavaFX
 * issues:
 * <ul>
 * <li>https://bugs.openjdk.java.net/browse/JDK-8136465: fixed by keeping track
 * of all listeners and ensuring that remaining listeners are re-added when a
 * listener is removed.</li>
 * <li>https://bugs.openjdk.java.net/browse/JDK-8120138: fixed by overwriting
 * equals() and hashCode()</li>
 * </ul>
 *
 * @author anyssen
 *
 * @param <K>
 *            The key type of the wrapped {@link ObservableMap}.
 * @param <V>
 *            The value type of the wrapped {@link ObservableMap}.
 *
 */
public class SimpleMapPropertyEx<K, V> extends SimpleMapProperty<K, V> {

	private MapExpressionHelperEx<K, V> helper = null;

	/**
	 * Creates a new unnamed {@link SimpleMapPropertyEx}.
	 */
	public SimpleMapPropertyEx() {
		super();
	}

	/**
	 * Constructs a new {@link SimpleMapPropertyEx} for the given bean and with
	 * the given name.
	 *
	 * @param bean
	 *            The bean this property is related to.
	 * @param name
	 *            The name of the property.
	 */
	public SimpleMapPropertyEx(Object bean, String name) {
		super(bean, name);
	}

	/**
	 * Constructs a new {@link SimpleMapPropertyEx} for the given bean and with
	 * the given name and initial value.
	 *
	 * @param bean
	 *            The bean this property is related to.
	 * @param name
	 *            The name of the property.
	 * @param initialValue
	 *            The initial value of the property
	 */
	public SimpleMapPropertyEx(Object bean, String name,
			ObservableMap<K, V> initialValue) {
		super(bean, name, initialValue);
	}

	/**
	 * Constructs a new unnamed {@link SimpleMapPropertyEx} that is not related
	 * to a bean, with the given initial value.
	 *
	 * @param initialValue
	 *            The initial value of the property
	 */
	public SimpleMapPropertyEx(ObservableMap<K, V> initialValue) {
		super(initialValue);
	}

	@Override
	public void addListener(
			ChangeListener<? super ObservableMap<K, V>> listener) {
		if (helper == null) {
			helper = new MapExpressionHelperEx<>(this);
		}
		helper.addListener(listener);
	}

	@Override
	public void addListener(InvalidationListener listener) {
		if (helper == null) {
			helper = new MapExpressionHelperEx<>(this);
		}
		helper.addListener(listener);
	}

	@Override
	public void addListener(MapChangeListener<? super K, ? super V> listener) {
		if (helper == null) {
			helper = new MapExpressionHelperEx<>(this);
		}
		helper.addListener(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		// Overwritten here to compensate an inappropriate equals()
		// implementation on Java 7
		// (https://bugs.openjdk.java.net/browse/JDK-8120138)
		if (other == this) {
			return true;
		}

		if (other == null || !(other instanceof Map)) {
			return false;
		}

		try {
			Map<K, V> otherMap = (Map<K, V>) other;
			if (otherMap.size() != size()) {
				return false;
			}
			for (K key : keySet()) {
				if (get(key) == null) {
					if (otherMap.get(key) != null) {
						return false;
					}
				} else if (!get(key).equals(otherMap.get(key))) {
					return false;
				}
			}
		} catch (ClassCastException unused) {
			return false;
		}

		return true;
	}

	@Override
	protected void fireValueChangedEvent() {
		if (helper != null) {
			helper.fireValueChangedEvent();
		}
	}

	@Override
	protected void fireValueChangedEvent(
			Change<? extends K, ? extends V> change) {
		if (helper != null) {
			helper.fireValueChangedEvent(change);
		}
	}

	@Override
	public int hashCode() {
		// Overwritten here to compensate an inappropriate hashCode()
		// implementation on Java 7
		// (https://bugs.openjdk.java.net/browse/JDK-8120138)
		int h = 0;
		for (Entry<K, V> e : entrySet()) {
			h += e.hashCode();
		}
		return h;
	}

	@Override
	public void removeListener(
			ChangeListener<? super ObservableMap<K, V>> listener) {
		if (helper != null) {
			helper.removeListener(listener);
		}
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		if (helper != null) {
			helper.removeListener(listener);
		}
	}

	@Override
	public void removeListener(
			MapChangeListener<? super K, ? super V> listener) {
		if (helper != null) {
			helper.removeListener(listener);
		}
	}
}
