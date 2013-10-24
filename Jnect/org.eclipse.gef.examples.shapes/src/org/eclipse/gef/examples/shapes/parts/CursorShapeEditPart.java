package org.eclipse.gef.examples.shapes.parts;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.examples.shapes.model.CursorShape;
import org.eclipse.gef.examples.shapes.model.Shape;
import org.eclipse.swt.widgets.Display;
import org.jnect.bodymodel.PositionedElement;

public class CursorShapeEditPart extends AbstractGraphicalEditPart implements NodeEditPart {

	private ConnectionAnchor anchor;

	// adapter to connect model to jnect
	private Adapter positionChangeAdapter = new Adapter() {
		@Override
		public void notifyChanged(Notification notification) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					refreshVisuals();
				}
			});
		}

		@Override
		public Notifier getTarget() {
			return getPositionedElement();
		}

		@Override
		public void setTarget(Notifier newTarget) {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean isAdapterForType(Object type) {
			// TODO Auto-generated method stub
			return false;
		}
	};

	/**
	 * Upon activation, add the change adapter to the positioned element
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			getPositionedElement().eAdapters().add(positionChangeAdapter);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		// the cursor should not be removable or should have any connections
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		IFigure f = createFigureForModel();
		f.setOpaque(true); // non-transparent figure
		f.setBackgroundColor(ColorConstants.red);
		f.setSize(10, 10);
		return f;
	}

	/**
	 * Return a new Ellipse which will be used as a cursor
	 */
	private IFigure createFigureForModel() {
		return new Ellipse();
	}

	/**
	 * Upon deactivation, detach change adapter from positioned element
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getPositionedElement().eAdapters().remove(positionChangeAdapter);
		}
	}

	private Shape getCastedModel() {
		return (Shape) getModel();
	}

	private PositionedElement getPositionedElement() {
		return ((CursorShape) getModel()).getPositionedElement();
	}

	protected ConnectionAnchor getConnectionAnchor() {
		if (anchor == null) {
			anchor = new EllipseAnchor(getFigure());
		}
		return anchor;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections
	 * ()
	 */
	protected List getModelSourceConnections() {
		return getCastedModel().getSourceConnections();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections
	 * ()
	 */
	protected List getModelTargetConnections() {
		return getCastedModel().getTargetConnections();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	protected void refreshVisuals() {
		IFigure figure = getFigure();
		PositionedElement model = getPositionedElement();
		DiagramEditPart parent = (DiagramEditPart) this.getParent();
		CursorShape cursor = (CursorShape) getModel();

		if (getViewer() == null || getViewer().getControl() == null) {
			return;
		}

		if (cursor.isEditing()) {
			figure.setBackgroundColor(ColorConstants.orange);
		} else {
			figure.setBackgroundColor(ColorConstants.red);
		}

		org.eclipse.swt.graphics.Point size = getViewer().getControl().getSize();

		// scaling factors, so user can stay at fixed position
		float xScaling = (size.x) * 0.75f;
		float yScaling = (size.y) * 0.75f;
		float xOffset = (size.x) / 2;
		float yOffset = (size.y) / 2;

		// set new location of cursor
		getCastedModel().setLocation(
			new org.eclipse.draw2d.geometry.Point(Math.round(xOffset + model.getX() * xScaling), Math.round(yOffset
				- model.getY() * yScaling)));

		// inform parent of location change
		Rectangle layout = new Rectangle(getCastedModel().getLocation(), getCastedModel().getSize());
		parent.setLayoutConstraint(this, figure, layout);
	}
}
