package org.eclipse.gef.examples.shapes.helper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.examples.shapes.ShapesEditor;
import org.eclipse.gef.examples.shapes.model.CursorShape;
import org.eclipse.gef.examples.shapes.model.Shape;
import org.eclipse.gef.examples.shapes.model.ShapesDiagram;
import org.eclipse.gef.examples.shapes.model.commands.ShapeCreateCommand;
import org.eclipse.gef.examples.shapes.model.commands.ShapeDeleteCommand;
import org.eclipse.gef.examples.shapes.model.commands.ShapeSetConstraintCommand;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.jnect.core.KinectManager;

public class GefEditingHelper {

	public static GefEditingHelper INSTANCE = new GefEditingHelper();

	private CursorShape cursor;
	private ShapesEditor editor;
	private ShapesDiagram diagram;
	private Shape movingShape;
	private Point oldLocation;
	private PropertyChangeListener propertyChangeListener;
	private boolean paused;

	private GefEditingHelper() {
		this.cursor = null;
		this.editor = null;
		this.diagram = null;
		this.movingShape = null;
		this.oldLocation = null;
		this.paused = false;
		this.propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String prop = evt.getPropertyName();
				if (Shape.LOCATION_PROP.equals(prop)) {
					if (movingShape != null) {
						movingShape.setLocation(cursor.getLocation());
					}
				}
			}
		};
	};

	public void startGefEditing() {
		if (cursor == null) {
			editor = (ShapesEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
			diagram = editor.getModel();
			cursor = new CursorShape(KinectManager.INSTANCE.getSkeletonModel().getLeftHand());
			cursor.setSize(new Dimension(10, 10));
			new ShapeCreateCommand(cursor, diagram, new Rectangle()).execute();
		}
	}

	public void switchGefEditingMode() {
		if (cursor == null)
			return;

		boolean isEditing = cursor.switchGefEditingMode();
		if (isEditing) {
			movingShape = findShapeToEdit();
			if (movingShape == null) {
				// no shape found, exit editing mode
				cursor.switchGefEditingMode();
			} else {
				// hook shape to cursor and save old position for undo
				oldLocation = movingShape.getLocation();
				cursor.addPropertyChangeListener(propertyChangeListener);
			}
		} else {
			if (movingShape != null) {
				// remove hook
				cursor.removePropertyChangeListener(propertyChangeListener);

				// execute as command so the location change can be undone
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						Rectangle newBounds = new Rectangle(movingShape.getLocation(), movingShape.getSize());
						movingShape.setLocation(oldLocation); // for undo
						editor.executeOnCommandStack(new ShapeSetConstraintCommand(movingShape,
							new ChangeBoundsRequest(RequestConstants.REQ_MOVE), newBounds));
					}
				});

				movingShape = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Shape findShapeToEdit() {
		Shape foundShape = null;
		Point cursorLocation = cursor.getLocation();
		List<Shape> shapes = diagram.getChildren();
		for (Shape s : shapes) {
			if (!s.equals(cursor)) {
				Point location = s.getLocation();
				int xMin = location.x;
				int xMax = xMin + s.getSize().width;
				int yMin = location.y;
				int yMax = yMin + s.getSize().height;
				// check if cursor position is lying inside the bounds of the current shape
				if ((xMin <= cursorLocation.x && cursorLocation.x <= xMax)
					&& (yMin <= cursorLocation.y && cursorLocation.y <= yMax)) {
					foundShape = s;
					break;
				}
			}
		}
		return foundShape;
	}

	public void stopGefEditing() {
		if (cursor != null) {
			new ShapeDeleteCommand(diagram, cursor).execute();
			editor = null;
			diagram = null;
			cursor = null;
			movingShape = null;
		}
	}

	public void pause() {
		if (cursor != null) { // cursor is active, so we can pause
			new ShapeDeleteCommand(diagram, cursor).execute();
			paused = true;
		}
	}

	public void unpause() {
		if (paused) {
			new ShapeCreateCommand(cursor, diagram, new Rectangle()).execute();
			paused = false;
		}
	}
}
