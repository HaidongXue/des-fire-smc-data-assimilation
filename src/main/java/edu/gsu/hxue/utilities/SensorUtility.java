package edu.gsu.hxue.utilities;

import edu.gsu.hxue.sensors.TemperatureSensorProfile;

import java.awt.*;
import java.util.Random;
import java.util.Vector;

public class SensorUtility {

    public static TemperatureSensorProfile[] generateSensorProfiles(int xDim, int yDim, int sensorNumber, double sensorRadius) {
        return generateSensorProfiles(new Random(), xDim, yDim, sensorNumber, sensorRadius);
    }

    public static TemperatureSensorProfile[] generateSensorProfiles(Random r, int xDim, int yDim, int sensorNumber, double sensorRadius) {
        TemperatureSensorProfile[] sensorProfiles = new TemperatureSensorProfile[sensorNumber];
        for (int i = 0; i < sensorNumber; i++) {
            sensorProfiles[i] = new TemperatureSensorProfile();
            sensorProfiles[i].radius = sensorRadius;
            sensorProfiles[i].location = new Point((int) Math.round(r.nextDouble() * xDim), (int) Math.round(r.nextDouble() * yDim));
        }
        return sensorProfiles;
    }

    public static TemperatureSensorProfile[] generateSensorProfilesInRectangle(Random r, int xS, int yS, int xE, int yE, int sensorNumber, double sensorRadius) {
        TemperatureSensorProfile[] sensorProfiles = new TemperatureSensorProfile[sensorNumber];
        for (int i = 0; i < sensorNumber; i++) {
            sensorProfiles[i] = new TemperatureSensorProfile();
            sensorProfiles[i].radius = sensorRadius;
            sensorProfiles[i].location = new Point((int) Math.round(r.nextDouble() * (xE - xS)) + xS, (int) Math.round(r.nextDouble() * (yE - yS)) + yS);
            System.out.println(sensorProfiles[i].location);
        }
        return sensorProfiles;
    }

    public static Vector<Vector<TemperatureSensorProfile>> partitionSensors(TemperatureSensorProfile[] sensorProfiles) {
        Vector<Vector<TemperatureSensorProfile>> sensorPartition = new Vector<Vector<TemperatureSensorProfile>>();

        for (int i = 0; i < sensorProfiles.length; i++) {
            TemperatureSensorProfile sensor = sensorProfiles[i];

            Vector<Vector<TemperatureSensorProfile>> reachedClusters = searchSensorToCluster(sensor, sensorPartition);
            if (reachedClusters.size() == 0) {
                Vector<TemperatureSensorProfile> newCluster = new Vector<TemperatureSensorProfile>();
                newCluster.add(sensor);
                sensorPartition.add(newCluster);
            } else if (reachedClusters.size() == 1) {
                reachedClusters.elementAt(0).add(sensor);
            } else {
                //Merge and join in those clusters
                Vector<TemperatureSensorProfile> mergedCluster = new Vector<TemperatureSensorProfile>();
                for (Vector<TemperatureSensorProfile> cluster : reachedClusters) {
                    for (TemperatureSensorProfile p : cluster)
                        mergedCluster.add(p);
                    sensorPartition.remove(cluster); // remove old ones from the partition
                }
                mergedCluster.add(sensor);

                // add the new one
                sensorPartition.add(mergedCluster);
            }
        }
        return sensorPartition;
    }

    private static Vector<Vector<TemperatureSensorProfile>> searchSensorToCluster(TemperatureSensorProfile sensor, Vector<Vector<TemperatureSensorProfile>> sensorPartition) {
        Vector<Vector<TemperatureSensorProfile>> reachedClusters = new Vector<Vector<TemperatureSensorProfile>>();

        for (Vector<TemperatureSensorProfile> cluser : sensorPartition) {
            boolean correlated = false;
            for (TemperatureSensorProfile p : cluser) {
                double dis = p.location.distance(sensor.location);
                if (dis <= (p.radius + sensor.radius)) // found a correlated sensor
                {
                    correlated = true;
                    break;
                }
            }

            if (correlated) {
                reachedClusters.add(cluser);
            }
        }
        return reachedClusters;
    }
}
