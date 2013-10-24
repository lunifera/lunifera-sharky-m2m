package org.jnect.emfstore;

import java.security.AccessControlException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.emfstore.client.model.CompositeOperationHandle;
import org.eclipse.emf.emfstore.client.model.ModelFactory;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.exceptions.InvalidHandleException;
import org.eclipse.emf.emfstore.client.model.impl.WorkspaceImpl;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreClientUtil;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.common.model.ModelElementId;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AttributeOperation;
import org.jnect.bodymodel.Body;
import org.jnect.bodymodel.BodymodelFactory;
import org.jnect.bodymodel.CenterHip;
import org.jnect.bodymodel.CenterShoulder;
import org.jnect.bodymodel.Head;
import org.jnect.bodymodel.HumanLink;
import org.jnect.bodymodel.LeftAnkle;
import org.jnect.bodymodel.LeftElbow;
import org.jnect.bodymodel.LeftFoot;
import org.jnect.bodymodel.LeftHand;
import org.jnect.bodymodel.LeftHip;
import org.jnect.bodymodel.LeftKnee;
import org.jnect.bodymodel.LeftShoulder;
import org.jnect.bodymodel.LeftWrist;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.bodymodel.RightAnkle;
import org.jnect.bodymodel.RightElbow;
import org.jnect.bodymodel.RightFoot;
import org.jnect.bodymodel.RightHand;
import org.jnect.bodymodel.RightHip;
import org.jnect.bodymodel.RightKnee;
import org.jnect.bodymodel.RightShoulder;
import org.jnect.bodymodel.RightWrist;
import org.jnect.bodymodel.Spine;

/**
 * @author aleaum
 *         This class offers the backbone for recording changing body models e.g. for storing Kinect data.
 */
public class EMFStorage extends Observable implements ICommitter {

	private static EMFStorage INSTANCE;
	private static String PROJECT_NAME = "jnectEMFStorage";
	private int NEEDED_CHANGES;

	ProjectSpace projectSpace;
	Usersession usersession;

	private Body replayBody;
	private Body recordingBody;
	private Body outwardRecordingBody;

	private List<ChangePackage> changePackages;
	private boolean changePackagesUpdateNeeded;
	private int replayStatesCount = 0;
	private final int BODY_ELEMENTS_COUNT;
	private ReplayRunnable replayRunnable;

	/**
	 * Counts how many new bodies have been recorded since the last commit.
	 */
	private int recordedBodyCount = 0;

	private CompositeOperationHandle compOpHandle;

	private boolean isRecording = false;

	/**
	 * @return The singleton EMFStorage object. Tries to setup the connection to the EMFStore Server.
	 */
	public static EMFStorage getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new EMFStorage();
		}
		return INSTANCE;
	}

	protected EMFStorage() {
		this.changePackagesUpdateNeeded = true;
		replayBody = createAndFillBody();
		outwardRecordingBody = createAndFillBody();
		outwardRecordingBody.eAdapters().add(new BundleBodyChangesAdapter());
		BODY_ELEMENTS_COUNT = outwardRecordingBody.eContents().size();
		// 3 changes (x, y, z) in every body element
		NEEDED_CHANGES = BODY_ELEMENTS_COUNT * 3;
	}

	/**
	 * This method sets up a connection to the EMFStore to retrieve a stored body model, if one is present. If the
	 * connection is already accomplished the call to this method is still safe, nothing will happen.
	 * 
	 * @return true if the connection was successful, false otherwise
	 */
	private boolean connectToEMFStoreAndInit() {
		if (projectSpace == null) {

			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					try {
						// create a default Usersession and log in
						usersession = EMFStoreClientUtil.createUsersession();
						Workspace currentWorkspace = WorkspaceManager.getInstance().getCurrentWorkspace();
						currentWorkspace.getUsersessions().add(usersession);
						usersession.logIn();

						// search for existing storage project on server
						Iterator<ProjectInfo> projectInfos = currentWorkspace.getRemoteProjectList(usersession)
							.iterator();
						ProjectInfo projectInfo = null;
						while (projectInfos.hasNext()) {
							ProjectInfo currentProjectInfo = projectInfos.next();
							if (currentProjectInfo.getName().equals(PROJECT_NAME)) {
								projectInfo = currentProjectInfo;
								break;
							}
						}

						// if storage project is not existing on server create one, else retrieve it
						if (projectInfo == null) {
							projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
							projectSpace.setProject(org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
								.createProject());
							projectSpace.setProjectName(PROJECT_NAME);
							projectSpace.setProjectDescription("Project for jnect-storage");
							projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
							projectSpace.initResources(currentWorkspace.eResource().getResourceSet());
							((WorkspaceImpl) currentWorkspace).addProjectSpace(projectSpace);
							currentWorkspace.save();
							projectSpace.shareProject(usersession, new NullProgressMonitor());
						} else {
							// check if we already have a local copy, else checkout the project
							boolean found = false;
							for (ProjectSpace ps : currentWorkspace.getProjectSpaces()) {
								if (ps.getProjectInfo().getName().equals(PROJECT_NAME)) {
									projectSpace = ps;
									projectSpace.setUsersession(usersession);
									found = true;
									break;
								}
							}
							if (!found) {
								projectSpace = currentWorkspace.checkout(usersession, projectInfo);
							}
						}

						Project project = projectSpace.getProject();
						boolean found = false;
						for (EObject obj : project.getAllModelElements()) {
							if (obj instanceof Body) {
								recordingBody = (Body) obj;
								found = true;
								break;
							}
						}
						if (!found) {
							recordingBody = createAndFillBody();
							project.addModelElement(recordingBody);
						}
						projectSpace.commit(createLogMessage(usersession.getUsername(), "commit initial body"), null,
							new NullProgressMonitor());

						org.eclipse.emf.emfstore.client.model.Configuration.setAutoSave(false);

					} catch (AccessControlException e) {
						ModelUtil.logException(e);
						projectSpace = null;
						recordingBody = null;
					} catch (EmfStoreException e) {
						ModelUtil.logException(e);
						projectSpace = null;
						recordingBody = null;
					}
				}
			}.run(false);
		}

		if (projectSpace == null) {
			return false;
		} else {
			assert recordingBody != null;
			return true;
		}
	}

	public static Body createAndFillBody() {
		Body body = BodymodelFactory.eINSTANCE.createBody();
		BodymodelFactory factory = BodymodelFactory.eINSTANCE;
		// create Elements
		Head head = factory.createHead();
		CenterShoulder shoulderCenter = factory.createCenterShoulder();
		LeftShoulder shoulderLeft = factory.createLeftShoulder();
		RightShoulder shoulderRight = factory.createRightShoulder();
		LeftElbow elbowLeft = factory.createLeftElbow();
		RightElbow elbowRight = factory.createRightElbow();
		LeftWrist wristLeft = factory.createLeftWrist();
		RightWrist wristRight = factory.createRightWrist();
		LeftHand handLeft = factory.createLeftHand();
		RightHand handRight = factory.createRightHand();
		Spine spine = factory.createSpine();
		CenterHip hipCenter = factory.createCenterHip();
		LeftHip hipLeft = factory.createLeftHip();
		RightHip hipRight = factory.createRightHip();
		LeftKnee kneeLeft = factory.createLeftKnee();
		RightKnee kneeRight = factory.createRightKnee();
		LeftAnkle ankleLeft = factory.createLeftAnkle();
		RightAnkle ankleRight = factory.createRightAnkle();
		LeftFoot footLeft = factory.createLeftFoot();
		RightFoot footRight = factory.createRightFoot();

		// set color
		footLeft.setColor_g(255);
		footRight.setColor_g(255);
		handLeft.setColor_r(255);
		handLeft.setColor_g(0);
		handLeft.setColor_b(0);
		handRight.setColor_r(255);
		head.setColor_b(255);

		// add elements to body
		body.setHead(head);
		body.setLeftAnkle(ankleLeft);
		body.setRightAnkle(ankleRight);
		body.setLeftElbow(elbowLeft);
		body.setRightElbow(elbowRight);
		body.setLeftFoot(footLeft);
		body.setRightFoot(footRight);
		body.setLeftHand(handLeft);
		body.setRightHand(handRight);
		body.setCenterHip(hipCenter);
		body.setLeftHip(hipLeft);
		body.setRightHip(hipRight);
		body.setLeftKnee(kneeLeft);
		body.setRightKnee(kneeRight);
		body.setCenterShoulder(shoulderCenter);
		body.setLeftShoulder(shoulderLeft);
		body.setRightShoulder(shoulderRight);
		body.setSpine(spine);
		body.setLeftWrist(wristLeft);
		body.setRightWrist(wristRight);

		// create links
		createLink(head, shoulderCenter, body);
		createLink(shoulderCenter, shoulderLeft, body);
		createLink(shoulderCenter, shoulderRight, body);
		createLink(shoulderLeft, elbowLeft, body);
		createLink(shoulderRight, elbowRight, body);
		createLink(elbowLeft, wristLeft, body);
		createLink(elbowRight, wristRight, body);
		createLink(wristLeft, handLeft, body);
		createLink(wristRight, handRight, body);
		createLink(shoulderCenter, spine, body);
		createLink(spine, hipCenter, body);
		createLink(hipCenter, hipLeft, body);
		createLink(hipCenter, hipRight, body);
		createLink(hipLeft, kneeLeft, body);
		createLink(hipRight, kneeRight, body);
		createLink(kneeLeft, ankleLeft, body);
		createLink(kneeRight, ankleRight, body);
		createLink(ankleLeft, footLeft, body);
		createLink(ankleRight, footRight, body);
		return body;
	}

	private static void createLink(PositionedElement source, PositionedElement target, Body body) {
		HumanLink link = BodymodelFactory.eINSTANCE.createHumanLink();
		link.setSource(source);
		link.setTarget(target);

		source.getOutgoingLinks().add(link);
		target.getIncomingLinks().add(link);

		body.getLinks().add(link);
	}

	private LogMessage createLogMessage(String name, String message) {
		LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
		logMessage.setAuthor(name);
		logMessage.setDate(Calendar.getInstance().getTime());
		logMessage.setClientDate(Calendar.getInstance().getTime());
		logMessage.setMessage(message);
		return logMessage;
	}

	public Body getRecordingBody() {
		return outwardRecordingBody;
	}

	public Body getReplayingBody() {
		return replayBody;
	}

	public int getReplayStatesCount() {
		return replayStatesCount;
	}

	public void initReplay() {
		if (!connectToEMFStoreAndInit()) {
			return;
		}

		if (changePackagesUpdateNeeded) {
			PrimaryVersionSpec start = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
			start.setIdentifier(1);
			try {
				changePackages = projectSpace.getChanges(start, projectSpace.getBaseVersion());
				changePackagesUpdateNeeded = false;
				replayStatesCount = 0;
				for (ChangePackage cp : changePackages) {
					replayStatesCount += cp.getOperations().size();
				}
			} catch (EmfStoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Replays the body model from emfstore
	 * 
	 * @param initCommit
	 * @throws EmfStoreException
	 */
	public void replay(final int version) {
		if (!connectToEMFStoreAndInit())
			return;

		if (replayRunnable == null) {
			replayRunnable = new ReplayRunnable();
		}
		if (replayRunnable.isStopped()) {
			final CommitVersionAndOffset versAndOffset = getCommitVersionForReplayVersion(version);
			replayRunnable.prepare(versAndOffset, version);
			Thread replayThread = new Thread(replayRunnable);
			replayThread.start();
		}

	}

	protected void replayOneChange(List<AbstractOperation> operations, int bodyOffset) {
		int startPos = bodyOffset;
		assert operations.size() >= startPos - 1 : "Operations should contain the requested body index " + startPos
			+ " but had size " + operations.size() + ".";

		List<AbstractOperation> leafOperations = operations.get(bodyOffset).getLeafOperations();

		for (AbstractOperation o : leafOperations) {
			replayElement(replayBody, o);
		}

	}

	private CommitVersionAndOffset getCommitVersionForReplayVersion(int repVersion) {
		int countedBodies = 0;
		for (int i = 0; i < changePackages.size(); i++) {
			ChangePackage cp = changePackages.get(i);
			int currentCount = cp.getOperations().size();
			if (countedBodies + currentCount > repVersion)
				return new CommitVersionAndOffset(i, repVersion - countedBodies);
			countedBodies += currentCount;
		}
		assert false : "The last change package should at the very least contain the searched repVersion!";
		return new CommitVersionAndOffset(changePackages.size() - 1, 0);
	}

	private void replayElement(Body targetBody, AbstractOperation o) {
		if (o instanceof AttributeOperation) {
			AttributeOperation ao = (AttributeOperation) o;
			ModelElementId id = ao.getModelElementId();
			EObject element = projectSpace.getProject().getModelElement(id);
			Object newValue = ao.getNewValue();
			String attribute = ao.getFeatureName(); // gets attribute name

			if (element instanceof Head) {
				setValue(attribute, targetBody.getHead(), newValue);
			} else if (element instanceof CenterShoulder) {
				setValue(attribute, targetBody.getCenterShoulder(), newValue);
			} else if (element instanceof LeftShoulder) {
				setValue(attribute, targetBody.getLeftShoulder(), newValue);
			} else if (element instanceof RightShoulder) {
				setValue(attribute, targetBody.getRightShoulder(), newValue);
			} else if (element instanceof LeftElbow) {
				setValue(attribute, targetBody.getLeftElbow(), newValue);
			} else if (element instanceof RightElbow) {
				setValue(attribute, targetBody.getRightElbow(), newValue);
			} else if (element instanceof LeftWrist) {
				setValue(attribute, targetBody.getLeftWrist(), newValue);
			} else if (element instanceof RightWrist) {
				setValue(attribute, targetBody.getRightWrist(), newValue);
			} else if (element instanceof LeftHand) {
				setValue(attribute, targetBody.getLeftHand(), newValue);
			} else if (element instanceof RightHand) {
				setValue(attribute, targetBody.getRightHand(), newValue);
			} else if (element instanceof Spine) {
				setValue(attribute, targetBody.getSpine(), newValue);
			} else if (element instanceof CenterHip) {
				setValue(attribute, targetBody.getCenterHip(), newValue);
			} else if (element instanceof LeftHip) {
				setValue(attribute, targetBody.getLeftHip(), newValue);
			} else if (element instanceof RightHip) {
				setValue(attribute, targetBody.getRightHip(), newValue);
			} else if (element instanceof LeftKnee) {
				setValue(attribute, targetBody.getLeftKnee(), newValue);
			} else if (element instanceof RightKnee) {
				setValue(attribute, targetBody.getRightKnee(), newValue);
			} else if (element instanceof LeftAnkle) {
				setValue(attribute, targetBody.getLeftAnkle(), newValue);
			} else if (element instanceof RightAnkle) {
				setValue(attribute, targetBody.getRightAnkle(), newValue);
			} else if (element instanceof LeftFoot) {
				setValue(attribute, targetBody.getLeftFoot(), newValue);
			} else if (element instanceof RightFoot) {
				setValue(attribute, targetBody.getRightFoot(), newValue);
			}
		}
	}

	private void setValue(String attribute, PositionedElement element, Object value) {
		if (attribute.equalsIgnoreCase("x")) {
			element.setX((Float) value);
		} else if (attribute.equalsIgnoreCase("y")) {
			element.setY((Float) value);
		} else if (attribute.equalsIgnoreCase("z")) {
			element.setZ((Float) value);
		}
	}

	public void setReplayToState(int state) {
		stopReplay();
		if (!changePackages.isEmpty()) {
			CommitVersionAndOffset versAndOff = getCommitVersionForReplayVersion(state);
			ChangePackage cp = changePackages.get(versAndOff.version);
			// replay the desired state to show the correct shape
			replayOneChange(cp.getOperations(), versAndOff.offset);
		}
	}

	private void commitBodyChanges(IProgressMonitor monitor) {
		if (!connectToEMFStoreAndInit())
			return;

		// commit the pending changes of the project to the EMF Store
		try {
			// projectSpace.setDirty(true);
			// ((ProjectSpaceBase) projectSpace).save();
			projectSpace.commit(
				createLogMessage(usersession.getUsername(), "Commiting " + recordedBodyCount + " new body frames."),
				null, monitor);
			changePackagesUpdateNeeded = true;
			recordedBodyCount = 0;
		} catch (EmfStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void commit() {
		Job commitJob = new Job("Saving recorded data.") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				commitBodyChanges(monitor);
				return Status.OK_STATUS;
			}
		};
		commitJob.setUser(true); // show dialog
		commitJob.schedule();
	}

	private class CommitVersionAndOffset {
		public int version;
		public int offset;

		public CommitVersionAndOffset(int version, int offset) {
			this.version = version;
			this.offset = offset;
		}
	}

	public void startStopRecording(boolean on) {
		if (on && connectToEMFStoreAndInit()) {
			isRecording = true;
		} else {
			isRecording = false;
		}
	}

	private class ReplayRunnable implements Runnable {

		private CommitVersionAndOffset replayFrom;

		private int replayFromBodyNr;

		private boolean stop;

		public ReplayRunnable() {
			stop = true;
		}

		public void prepare(CommitVersionAndOffset replayFromCS, int replayFromBodyNr) {
			this.replayFrom = replayFromCS;
			this.replayFromBodyNr = replayFromBodyNr;
		}

		public synchronized void stop() {
			stop = true;
		}

		public synchronized boolean isStopped() {
			return stop;
		}

		@Override
		public void run() {
			stop = false;
			List<AbstractOperation> compositeBodyOps;

			int currentVersion = replayFromBodyNr;
			int innerOffset = replayFrom.offset;

			for (int i = replayFrom.version; i < changePackages.size() && !isStopped(); i++) {
				ChangePackage cp = changePackages.get(i);
				compositeBodyOps = cp.getOperations();
				for (int j = innerOffset; j < compositeBodyOps.size() && !isStopped(); j++) {
					replayOneChange(compositeBodyOps, j);
					currentVersion++;
					setChanged();
					notifyObservers(currentVersion);
					try {
						// pause for a moment to see changes
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				innerOffset = 0;

			}
			stop();
		}
	}

	protected void syncBodies(Body outwardBody, Body emfBody) {
		EList<EObject> bodyContents = outwardBody.eContents();
		EList<EObject> recBodyContents = emfBody.eContents();
		assert BODY_ELEMENTS_COUNT == bodyContents.size() && BODY_ELEMENTS_COUNT == recBodyContents.size() : "Unexpected amount of body elements. The being is not human ;-)";
		for (int i = 0; i < BODY_ELEMENTS_COUNT; i++) {
			PositionedElement outwardBodyEl = (PositionedElement) bodyContents.get(i);
			PositionedElement recBodyEl = (PositionedElement) recBodyContents.get(i);

			recBodyEl.setX(outwardBodyEl.getX());
			recBodyEl.setY(outwardBodyEl.getY());
			recBodyEl.setZ(outwardBodyEl.getZ());
		}
	}

	public void stopReplay() {
		if (replayRunnable != null)
			replayRunnable.stop();
	}

	private class BundleBodyChangesAdapter extends EContentAdapter {
		private int currChanges = 0;

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);

			if (++currChanges == NEEDED_CHANGES) {
				currChanges = 0;
				if (isRecording && projectSpace != null) {
					assert recordingBody != null;
					try {
						compOpHandle = projectSpace.beginCompositeOperation();
						syncBodies(outwardRecordingBody, recordingBody);

						compOpHandle.end("New Body frame", "Added new frame to store", projectSpace.getProject()
							.getModelElementId(recordingBody));
						projectSpace.setDirty(true);
						recordedBodyCount++;
					} catch (InvalidHandleException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public boolean isRecording() {
		return isRecording;
	}

}
