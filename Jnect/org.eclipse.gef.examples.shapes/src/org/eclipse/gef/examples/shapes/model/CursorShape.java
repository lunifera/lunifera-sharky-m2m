package org.eclipse.gef.examples.shapes.model;

import org.eclipse.swt.graphics.Image;
import org.jnect.bodymodel.PositionedElement;

public class CursorShape extends Shape {

	/** A 16x16 pictogram of a cursor shape. */
	private static final Image CURSOR_ICON = createImage("icons/ellipse16.gif");

	private static final long serialVersionUID = 1;

	private PositionedElement positionedElement;
	private boolean isEditing = false;

	public CursorShape(PositionedElement positionedElement) {
		this.positionedElement = positionedElement;
	}

	public Image getIcon() {
		return CURSOR_ICON;
	}

	public String toString() {
		return "Cursor " + hashCode();
	}

	public PositionedElement getPositionedElement() {
		return positionedElement;
	}

	public boolean switchGefEditingMode() {
		if (isEditing()) {
			isEditing = false;
			return false;
		} else {
			isEditing = true;
			return true;
		}
	}

	public boolean isEditing() {
		return isEditing;
	}

}
