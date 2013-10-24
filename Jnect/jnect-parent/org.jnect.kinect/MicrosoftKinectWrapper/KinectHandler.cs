using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Input;
using System.Xml;
using System.Threading;

using Microsoft.Kinect;


namespace MicrosoftKinectWrapper
{
    public interface IKinectHandler
    {
        string setUpAndRun();
        string testSkeletonTracking();
        string getSkeleton();
        string stop();
    }

    public class KinectHandler : IKinectHandler
    {
        private KinectSensor kinectSensor;
        private Skeleton[] skeletons;
        private List<string> skeletonQueue = new List<string>();
        private bool skeletonQueueBusy = false;

        //private ClientConnectionHandler cch;

        public KinectHandler()
        {
        }

        public string getSkeleton()
        {
            /* Busy waiting */
            while (skeletonQueueBusy)
            {
                Thread.Sleep(50);
            }

            skeletonQueueBusy = true;
            List<string> skeletonList = new List<string>();
            while (skeletonQueue.Count > 0)
            {
                skeletonList.Add(skeletonQueue.ElementAt(0));
                skeletonQueue.RemoveAt(0);
            }

            skeletonQueueBusy = false;

            string listToString = null;
            if (skeletonList.Count > 0)
            {
                listToString = skeletonList.ElementAt(0);
                for (int i = 1; i < skeletonList.Count; i++)
                {
                    listToString += "*" + skeletonList.ElementAt(i);
                }
            }
            
            return (null != listToString) ? listToString : null;
        }


        private void appendSkeletonToQueue(string skeleton)
        {
            /* Busy waiting */
            while (skeletonQueueBusy)
            {
                Thread.Sleep(50);
            }

            skeletonQueueBusy = true;
            skeletonQueue.Add(skeleton);
            skeletonQueueBusy = false;
        }


        private void nui_SkeletonFrameReady(object sender, SkeletonFrameReadyEventArgs e)
        {
            bool receivedData = false;

            using (SkeletonFrame skeletonFrame = e.OpenSkeletonFrame())
            {
                if (skeletonFrame != null)
                {
                    if (skeletons == null) //allocate the first time
                    {
                        skeletons = new Skeleton[skeletonFrame.SkeletonArrayLength];
                    }

                    receivedData = true;
                }
                else
                {
                    // apps processing of skeleton data took too long; it got more than 2 frames behind.
                    // the data is no longer avabilable.
                    Console.Error.WriteLine("Processing of skeleton data took too long; it got more than 2 frames behind. The data is no longer avabilable.");
                }

                if (receivedData)
                {
                    XmlDocument doc = new XmlDocument();

                    XmlNode xmlnode = doc.CreateNode(XmlNodeType.XmlDeclaration, "", "");
                    doc.AppendChild(xmlnode);

                    XmlNode root = doc.CreateElement("Skeleton");
                    doc.AppendChild(root);

                    XmlNode frameNode = doc.CreateElement("frameNumber");
                    frameNode.InnerText = skeletonFrame.FrameNumber.ToString();
                    root.AppendChild(frameNode);

                    XmlNode timeStampNode = doc.CreateElement("timeStamp");
                    timeStampNode.InnerText = skeletonFrame.Timestamp.ToString();
                    root.AppendChild(timeStampNode);

                    skeletonFrame.CopySkeletonDataTo(skeletons);
                    foreach (Skeleton skeleton in skeletons)
                    {

                        if (skeleton.TrackingState == SkeletonTrackingState.Tracked)
                        {
                            XmlNode skeletonData = doc.CreateElement("skeletonData");
                            root.AppendChild(skeletonData);

                            XmlNode trackingId = doc.CreateElement("trackingId");
                            trackingId.InnerText = skeleton.TrackingId.ToString();
                            skeletonData.AppendChild(trackingId);

                            //These lines remained from the beta sdk version of jenect. There seams to be no equivalence in API 1.0 anymore...
                            /*XmlNode userIndex = doc.CreateElement("userIndex");
                            userIndex.InnerText = skeleton.UserIndex.ToString();
                            skeletonData.AppendChild(userIndex);*/

                            JointCollection joints = skeleton.Joints;

                            foreach (Joint j in joints)
                            {
                                XmlNode joint = doc.CreateElement("joint");

                                XmlNode jPositionX = doc.CreateElement("positionX");
                                jPositionX.InnerText = j.Position.X.ToString();
                                joint.AppendChild(jPositionX);

                                XmlNode jPositionY = doc.CreateElement("positionY");
                                jPositionY.InnerText = j.Position.Y.ToString();
                                joint.AppendChild(jPositionY);

                                XmlNode jPositionZ = doc.CreateElement("positionZ");
                                jPositionZ.InnerText = j.Position.Z.ToString();
                                joint.AppendChild(jPositionZ);

                                XmlNode jointID = doc.CreateElement("jointId");
                                jointID.InnerText = j.JointType.ToString();
                                joint.AppendChild(jointID);

                                skeletonData.AppendChild(joint);
                            }

                            XmlNode positionX = doc.CreateElement("positionX");
                            positionX.InnerText = skeleton.Position.X.ToString();
                            skeletonData.AppendChild(positionX);

                            XmlNode positionY = doc.CreateElement("positionY");
                            positionY.InnerText = skeleton.Position.Y.ToString();
                            skeletonData.AppendChild(positionY);

                            XmlNode positionZ = doc.CreateElement("positionZ");
                            positionZ.InnerText = skeleton.Position.Z.ToString();
                            skeletonData.AppendChild(positionZ);

                        }

                        appendSkeletonToQueue(doc.OuterXml);
                        //cch.sendSkeleton(doc.OuterXml);
                    }
                }
            }
        }

        internal void startSkeletonTracking()
        {
        }

        public string setUpAndRun()
        {
            kinectSensor = KinectSensor.KinectSensors[0];

            try
            {
                kinectSensor.ColorStream.Enable();
                kinectSensor.DepthStream.Enable();
                kinectSensor.SkeletonStream.Enable();
                kinectSensor.Start();
            }
            catch (InvalidOperationException)
            {
                return "Runtime initialization failed. Please make sure Kinect device is plugged in.";
            }

            kinectSensor.SkeletonFrameReady += new EventHandler<SkeletonFrameReadyEventArgs>(nui_SkeletonFrameReady);

            return "Setup Done!";

        }

        public String stop()
        {
            this.kinectSensor.Stop();

            return "Kinect Stoped...";
        }

        public string testSkeletonTracking()
        {


            XmlDocument doc = new XmlDocument();

            XmlNode xmlnode = doc.CreateNode(XmlNodeType.XmlDeclaration, "", "");
            doc.AppendChild(xmlnode);

            XmlNode root = doc.CreateElement("Skeleton");
            doc.AppendChild(root);

            XmlNode frameNode = doc.CreateElement("frameNumber");
            frameNode.InnerText = "frameNumber";
            root.AppendChild(frameNode);

            XmlNode timeStampNode = doc.CreateElement("timeStamp");
            timeStampNode.InnerText = "timeStamp";
            root.AppendChild(timeStampNode);


            for (int i = 0; i < 2; i++)
            {

                if (true)
                {
                    XmlNode skeletonData = doc.CreateElement("skeletonData");
                    root.AppendChild(skeletonData);

                    XmlNode trackingId = doc.CreateElement("trackingId");
                    trackingId.InnerText = "trackingId" + i;
                    skeletonData.AppendChild(trackingId);

                    XmlNode userIndex = doc.CreateElement("userIndex");
                    userIndex.InnerText = "userIndex" + i;
                    skeletonData.AppendChild(userIndex);



                    for (int j = 0; j < 2; j++)
                    {
                        XmlNode joint = doc.CreateElement("joint");

                        XmlNode jPositionX = doc.CreateElement("positionX");
                        jPositionX.InnerText = "positionX" + i + j;
                        joint.AppendChild(jPositionX);

                        XmlNode jPositionY = doc.CreateElement("positionY");
                        jPositionY.InnerText = "positionY" + i + j;
                        joint.AppendChild(jPositionY);

                        XmlNode jPositionZ = doc.CreateElement("positionZ");
                        jPositionZ.InnerText = "positionZ" + i + j;
                        joint.AppendChild(jPositionZ);

                        XmlNode jointID = doc.CreateElement("jointId");
                        jointID.InnerText = "jointId " + i + j;
                        joint.AppendChild(jointID);
                    }

                    XmlNode positionX = doc.CreateElement("positionX");
                    positionX.InnerText = "positionX" + i;
                    skeletonData.AppendChild(positionX);

                    XmlNode positionY = doc.CreateElement("positionY");
                    positionY.InnerText = "positionY" + i;
                    skeletonData.AppendChild(positionY);

                    XmlNode positionZ = doc.CreateElement("positionZ");
                    positionZ.InnerText = "positionZ" + i;
                    skeletonData.AppendChild(positionZ);

                }
            }

            return "SKELETON: " + doc.OuterXml;

            //cch.sendSkeleton("SKELETON: " + doc.OuterXml);
        }
    }
}
