package edu.gsu.hxue.utilities;

import edu.gsu.hxue.CellularAutomataPresentation;
import edu.gsu.hxue.desFire.FirePresentation;
import edu.gsu.hxue.desFire.FireSystem;
import edu.gsu.hxue.fireSMC.FireStateWithUniformWind;
import edu.gsu.hxue.fireSMC.FireStateWithUniformWind.SensorReadingVector;
import edu.gsu.hxue.gis.GISData;
import edu.gsu.hxue.gis.fuel.StandardCustomizedFuelEnum;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.smc.GlobalConstants;
import edu.gsu.hxue.smc.SamplingStrategy;
import edu.gsu.hxue.smc.samplingStrategies.ProposalSampling;

import java.awt.*;
import java.util.Vector;

public class PresentationUtility {
    public static void drawResultsFromSerializedResults(int time, int particleNumber, String resultFolder) {
        double displayScale = 4;

        String realFireFilePath = resultFolder + "/" + time + "_RealFire.fir";
        FireSystem realFire = FireSystem.deserializeFrom(realFireFilePath);

        String simFireFilePath = resultFolder + "/" + time + "_simFire.fir";
        FireSystem simFire = FireSystem.deserializeFrom(simFireFilePath);

        String highestWeightFireFilePath = resultFolder + "/" + time + "_highestWeightFire.fir";
        FireSystem highestWeightFire = FireSystem.deserializeFrom(highestWeightFireFilePath);

        PresentationUtility.drawFireFronts(realFire, simFire, highestWeightFire, time, displayScale);

        // draw fire cloud
        GISData gisMap = realFire.getGISMap();
        CellularAutomataPresentation p = new CellularAutomataPresentation(gisMap.xDim(), gisMap.yDim(), displayScale);
        p.setTitle("Paticle cloud at " + time);
        drawGISMap(gisMap, p);

        for (int i = 0; i < particleNumber; i++) {
            String particleFireFilePath = resultFolder + "/" + time + "_Par" + i + "_highestWeightFire.fir";
            FireSystem sys = FireSystem.deserializeFrom(particleFireFilePath);
            drawFireFront(sys, p, Color.black);
        }

        drawFireFront(realFire, p, Color.blue);
        drawFireFront(simFire, p, Color.red);

    }

    public static void drawResultFrontsFromSerializedResults(int time, int particleNumber, String resultFolder) {
        double displayScale = 4;

        String realFireFilePath = resultFolder + "/" + time + "_RealFire.fir";
        FireSystem realFire = FireSystem.deserializeFrom(realFireFilePath);

        String simFireFilePath = resultFolder + "/" + time + "_simFire.fir";
        FireSystem simFire = FireSystem.deserializeFrom(simFireFilePath);

        String highestWeightFireFilePath = resultFolder + "/" + time + "_highestWeightFire.fir";
        FireSystem highestWeightFire = FireSystem.deserializeFrom(highestWeightFireFilePath);

        PresentationUtility.drawFireFronts(realFire, simFire, highestWeightFire, time, displayScale);

    }

    public static void drawFireFronts(FireSystem realFire, FireSystem simFire, FireSystem highestWeightFire, int currentTime, double displayScale) {
        GISData gisMap = realFire.getGISMap();
        CellularAutomataPresentation p = new CellularAutomataPresentation(gisMap.xDim(), gisMap.yDim(), displayScale);
        p.setTitle("Fire Fronts at " + currentTime);
        drawGISMap(gisMap, p);

        drawFireFront(realFire, p, Color.blue);
        drawFireFront(simFire, p, Color.red);
        drawFireFront(highestWeightFire, p, Color.yellow);
    }

    public static void drawFireFrontsWithParticleCloud(FireSystem realFire, FireSystem simFire, FireSystem[] particleFires, int currentTime, double displayScale) {
        GISData gisMap = realFire.getGISMap();
        CellularAutomataPresentation p = new CellularAutomataPresentation(gisMap.xDim(), gisMap.yDim(), displayScale);
        p.setTitle("Paticle cloud at " + currentTime);
        drawGISMap(gisMap, p);

        for (FireSystem f : particleFires)
            drawFireFront(f, p, Color.black);

        drawFireFront(realFire, p, Color.blue);
        drawFireFront(simFire, p, Color.red);
    }

    public static void drawGISMap(GISData gisMap, CellularAutomataPresentation p) {
        int xDim = gisMap.xDim();
        int yDim = gisMap.yDim();
        for (int i = 0; i < xDim; i++)
            for (int j = 0; j < yDim; j++) {
                p.setCellColor(i, j, StandardCustomizedFuelEnum.modelIndexToFuelType(gisMap.fuelModelIndexAt(i, j)).getColor());
            }
        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();
    }

    public static void drawFireFront(FireSystem fire, CellularAutomataPresentation p, Color c) {
        int xDim = fire.getXDim();
        int yDim = fire.getYDim();

        for (int x = 0; x < xDim; x++)
            for (int y = 0; y < yDim; y++)
                if (fire.isOnFireFront(x, y)) p.setCellColor(x, y, c);

        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();
    }

    public static void drawFireFront(FireSystem fire, Color c) {

        int xDim = fire.getXDim();
        int yDim = fire.getYDim();

        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, 4);

        for (int x = 0; x < xDim; x++)
            for (int y = 0; y < yDim; y++)
                if (fire.isOnFireFront(x, y)) p.setCellColor(x, y, c);

        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();
    }

    public static void drawSensorPartition(int xDim, int yDim, Vector<Vector<TemperatureSensorProfile>> sensorPartition) {
        int displayScale = 4;
        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);
        p.setTitle("A sensor partition");

        float h = 0;
        float s = (float) 1;
        float b = (float) 1;
        float step = (float) .7 / sensorPartition.size();
        for (Vector<TemperatureSensorProfile> cluster : sensorPartition) {
            h += step;
            Color c = Color.getHSBColor(h, s, b);

            drawSensors(cluster.toArray(new TemperatureSensorProfile[1]), p, c);
        }
    }

    public static void drawSensorTemperature(int xDim, int yDim, SensorReadingVector sensorSensorReadings, TemperatureSensorProfile[] sensorProfiles) {
        int displayScale = 4;
        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);

        p.setTitle("Sensor Temperature");
        double maxTemperature = 500;

        double[] temperatures = sensorSensorReadings.getSensorReadings();
        for (int i = 0; i < sensorProfiles.length; i++) {
            int x = sensorProfiles[i].location.x;
            int y = sensorProfiles[i].location.y;
            double t = temperatures[i];
            //Color c = Color.GRAY;
            //if(t>30)
            Color c = FirePresentation.temperatureToColor(t, maxTemperature);
            p.setCellColor(x, y, c);
            p.setCellText(x, y, Double.toString(t));
        }


        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();
    }

    public static void drawSensorTemperature(CellularAutomataPresentation p, SensorReadingVector sensorSensorReadings, TemperatureSensorProfile[] sensorProfiles) {
        double maxTemperature = 500;

        double[] temperatures = sensorSensorReadings.getSensorReadings();
        for (int i = 0; i < sensorProfiles.length; i++) {
            int x = sensorProfiles[i].location.x;
            int y = sensorProfiles[i].location.y;
            double t = temperatures[i];
            Color c = null;// Color.GRAY;
            if (t > 30)
                c = FirePresentation.temperatureToColor(t, maxTemperature);
            p.setCellColor(x, y, c);
            p.setCellText(x, y, Double.toString(t));
        }


        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();
    }

    public static void drawSensorPartitionMonitoredArea(int xDim, int yDim, Vector<Vector<TemperatureSensorProfile>> sensorPartition) {
        int displayScale = 4;
        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);
        p.setTitle("A sensor partition");

        float h = 0;
        float s = (float) 1;
        float b = (float) 1;
        float step = (float) 0.7 / sensorPartition.size();
        for (Vector<TemperatureSensorProfile> cluster : sensorPartition) {
            //h = (float)Math.random();
            h += step;
            Color c = Color.getHSBColor(h, s, b);

            drawSensorsMonitoredArea(cluster.toArray(new TemperatureSensorProfile[1]), p, c);
        }
    }

    public static void drawSensors(TemperatureSensorProfile[] sensorProfiles, CellularAutomataPresentation p, Color c) {
        for (TemperatureSensorProfile sen : sensorProfiles)
            p.setCellColor(sen.location.x, sen.location.y, c);
        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();
    }

    public static void drawSensorsMonitoredArea(TemperatureSensorProfile[] sensorProfiles, CellularAutomataPresentation p, Color c) {
        for (TemperatureSensorProfile sen : sensorProfiles)
            drawSensorMonitoredArea(sen, p, c);
    }

    public static void drawSensorMonitoredArea(TemperatureSensorProfile sensor, CellularAutomataPresentation p, Color c) {
        int xL = (int) (sensor.location.x - sensor.radius) - 1;
        int xH = (int) (sensor.location.x + sensor.radius) + 1;
        int yL = (int) (sensor.location.y - sensor.radius) - 1;
        int yH = (int) (sensor.location.y + sensor.radius) + 1;
        for (int x = xL; x <= xH; x++)
            for (int y = yL; y <= yH; y++) {
                double dis = sensor.location.distance(x, y);
                if (dis <= sensor.radius) p.setCellColor(x, y, c);
            }
        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();
    }


    public static void main(String[] args) {
        System.out.println("Performing utility...");
        //drawResultsFromSerializedResults( 10800, 50, "C:/Users/haydon/Desktop/SMCResults/Case5Bootstrap" );
        drawResultsFromSerializedResults(10800, 5, "results");

		/*int xDim=200;
		int yDim=200;
		int sensorNumber = 200;
		double sensorRadius = 140/15;
		
		TemperatureSensorProfile[] sensors = SensorUtility.generateSensorProfiles(GlobalConstants.RAND, xDim, yDim, sensorNumber, sensorRadius);
		System.out.println("sensor number: " + sensors.length);
		
		Vector<Vector<TemperatureSensorProfile>> partition = SensorUtility.partitionSensors(sensors);
		System.out.println("sensor cluster number: " + partition.size());
		drawSensorPartitionMonitoredArea(xDim, yDim, partition);
		drawSensorPartition(xDim, yDim, partition);*/

        //drawSensorPartitionMonitoredAreaForEachCluster(xDim, yDim, partition);

        System.out.println("Done!");
		
		/*int displayScale = 4;
		int xDim =200;
		int yDim =200;
		CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);
		p.setTitle("A sensor partition");
		
		TemperatureSensorProfile s = new TemperatureSensorProfile();
		s.radius =30;
		s.location =new Point(100, 100);
		drawSensorMonitoredArea(s, p, Color.blue  );*/

    }

    public static void drawSensorPartitionMonitoredAreaForEachCluster(int xDim, int yDim, Vector<Vector<TemperatureSensorProfile>> partition) {
        if (partition.size() > 12) {
            System.out.println("too many clusters to draw");
        }

        int c = 0;
        float h = 0;
        float s = 1;
        float b = 1;

        for (Vector<TemperatureSensorProfile> cluster : partition) {
            int displayScale = 4;
            CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);
            p.setTitle("Cluster-" + c++);

            drawSensorsMonitoredArea(cluster.toArray(new TemperatureSensorProfile[0]), p, Color.getHSBColor(h, s, b));
            h += (float) 0.08;
        }
    }

    public static void drawSampleedFireFronts(int n, FireStateWithUniformWind currentState, FireStateWithUniformWind realState, SamplingStrategy sampler, SensorReadingVector sensorReadings) {
        int displayScale = 4;
        for (int i = 0; i < n; i++) {
            FireSystem current = currentState.getFireSystem();
            FireSystem real = realState.getFireSystem();

            CellularAutomataPresentation p = new CellularAutomataPresentation(current.getXDim(), current.getYDim(), displayScale);
            p.setTitle(sampler.getClass().toString() + ". (Real in blue; Current in red; Sampled in orange)");

            PresentationUtility.drawFireFront(real, p, Color.blue);
            PresentationUtility.drawFireFront(current, p, Color.red);

            FireStateWithUniformWind sample = (FireStateWithUniformWind) sampler.sampling(currentState, sensorReadings);
            FireSystem sysSampled = sample.getFireSystem();
            PresentationUtility.drawFireFront(sysSampled, p, Color.orange);
        }


    }

    public static void drawFire(FireSystem fireSystem) {
        int displayScale = 4;
        int xDim = fireSystem.getXDim();
        int yDim = fireSystem.getYDim();
        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);
        drawGISMap(fireSystem.getGISMap(), p);

        for (int i = 0; i < xDim; i++)
            for (int j = 0; j < yDim; j++) {
                if (fireSystem.getFireCell(i, j).isBurning()) p.setCellColor(i, j, Color.black);
                if (fireSystem.isOnFireFront(i, j)) p.setCellColor(i, j, Color.red);
            }


    }


    public static void drawSampleedFireFrontsWithSensorReadings(int n, FireStateWithUniformWind currentState, FireStateWithUniformWind realState, ProposalSampling sampler,
                                                                SensorReadingVector sensorReadings, TemperatureSensorProfile[] sensorProfiles) {
        int displayScale = 4;
        for (int i = 0; i < n; i++) {
            FireSystem current = currentState.getFireSystem();
            FireSystem real = realState.getFireSystem();

            CellularAutomataPresentation p = new CellularAutomataPresentation(current.getXDim(), current.getYDim(), displayScale);
            p.setTitle(sampler.getClass().toString() + ". (Real in blue; Current in black; Sampled in orange)");

            PresentationUtility.drawFireFront(real, p, Color.blue);
            PresentationUtility.drawFireFront(current, p, Color.black);

            FireStateWithUniformWind sample = (FireStateWithUniformWind) sampler.sampling(currentState, sensorReadings);
            FireSystem sysSampled = sample.getFireSystem();
            PresentationUtility.drawFireFront(sysSampled, p, Color.orange);

            PresentationUtility.drawSensorTemperature(p, sensorReadings, sensorProfiles);
        }

    }

    public static void drawHotArea(int xDim, int yDim, SensorReadingVector measurement, TemperatureSensorProfile[] sensorProfiles) {
        int displayScale = 4;
        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);

        p.setTitle("Hot area");

        double temperatureHighThreshold = 150;
        double turnOnSigma = 0;
        double turnOnFactor = 100; // mean = temperature/turnOnFactor ;tried 50
        double maxTurnOnRadisus = 1000 / turnOnFactor;

        for (int i = 0; i < sensorProfiles.length; i++) {
            double[] readings = measurement.getSensorReadings();
            double turnOnRadius = readings[i] / turnOnFactor + GlobalConstants.RAND.nextGaussian() * turnOnSigma;
            if (turnOnRadius > maxTurnOnRadisus)
                turnOnRadius = maxTurnOnRadisus;
            if (readings[i] >= temperatureHighThreshold) // set turn on candidates
            {
                int xL = (int) (sensorProfiles[i].location.x - turnOnRadius) - 1;
                int xH = (int) (sensorProfiles[i].location.x + turnOnRadius) + 1;
                int yL = (int) (sensorProfiles[i].location.y - turnOnRadius) - 1;
                int yH = (int) (sensorProfiles[i].location.y + turnOnRadius) + 1;

                boolean alreadyOn = false;


                for (int x = xL; x <= xH; x++)
                    for (int y = yL; y <= yH; y++) {
                        double dis = sensorProfiles[i].location.distance(x, y);
                        if (x < 0 || x >= xDim || y < 0 || y >= yDim)
                            continue;
                        if (dis <= turnOnRadius)
                            p.setCellColor(x, y, Color.pink);
                    }
            }
        }


        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();

    }

    public static void drawCoolArea(int xDim, int yDim, SensorReadingVector measurement, TemperatureSensorProfile[] sensorProfiles) {
        int displayScale = 4;
        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);

        p.setTitle("Cool area");

        double temperatureLowThreshold = 20;

        double turnOffSigma = 0;
        double turnOffRadiusMean = 9;
        double turnOffRadius = turnOffRadiusMean + GlobalConstants.RAND.nextGaussian() * turnOffSigma;// 80/15.0;


        for (int i = 0; i < sensorProfiles.length; i++) {
            double[] readings = measurement.getSensorReadings();

            if (readings[i] <= temperatureLowThreshold) // set turn on candidates
            {
                int xL = (int) (sensorProfiles[i].location.x - turnOffRadius) - 1;
                int xH = (int) (sensorProfiles[i].location.x + turnOffRadius) + 1;
                int yL = (int) (sensorProfiles[i].location.y - turnOffRadius) - 1;
                int yH = (int) (sensorProfiles[i].location.y + turnOffRadius) + 1;

                for (int x = xL; x <= xH; x++)
                    for (int y = yL; y <= yH; y++) {
                        double dis = sensorProfiles[i].location.distance(x, y);
                        if (x < 0 || x >= xDim || y < 0 || y >= yDim)
                            continue;
                        if (dis <= turnOffRadius)
                            p.setCellColor(x, y, Color.gray);
                    }
            }
        }


        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();

    }

    public static void drawPossibleFireArea(int xDim, int yDim, SensorReadingVector measurement, TemperatureSensorProfile[] sensorProfiles) {
        int displayScale = 4;
        CellularAutomataPresentation p = new CellularAutomataPresentation(xDim, yDim, displayScale);

        p.setTitle("Possible fire area");

        double temperatureHighThreshold = 150;
        double temperatureLowThreshold = 20;

        boolean turnOff = true;
        double turnOffSigma = 0;
        double turnOffRadiusMean = 9;
        double turnOffRadius = turnOffRadiusMean + GlobalConstants.RAND.nextGaussian() * turnOffSigma;// 80/15.0;

        double turnOnSigma = 0;
        double turnOnFactor = 100; // mean = temperature/turnOnFactor ;tried 50
        double maxTurnOnRadisus = 1000 / turnOnFactor;


        boolean[][] turnOnFlags = new boolean[xDim][yDim];
        for (int i = 0; i < xDim; i++)
            for (int j = 0; j < yDim; j++)
                turnOnFlags[i][j] = false;

        double[] readings = measurement.getSensorReadings();

        for (int i = 0; i < sensorProfiles.length; i++) {
            TemperatureSensorProfile sensor = sensorProfiles[i];
            double turnOnRadius = readings[i] / turnOnFactor + GlobalConstants.RAND.nextGaussian() * turnOnSigma;
            if (turnOnRadius > maxTurnOnRadisus)
                turnOnRadius = maxTurnOnRadisus;
            if (readings[i] >= temperatureHighThreshold) // set turn on candidates
            {
                int xL = (int) (sensor.location.x - turnOnRadius) - 1;
                int xH = (int) (sensor.location.x + turnOnRadius) + 1;
                int yL = (int) (sensor.location.y - turnOnRadius) - 1;
                int yH = (int) (sensor.location.y + turnOnRadius) + 1;

                for (int x = xL; x <= xH; x++)
                    for (int y = yL; y <= yH; y++) {
                        double dis = sensor.location.distance(x, y);
                        if (x < 0 || x >= xDim || y < 0 || y >= yDim)
                            continue;
                        if (dis <= turnOnRadius)
                            turnOnFlags[x][y] = true;
                    }
            }
        }

        for (int i = 0; i < sensorProfiles.length; i++) {
            TemperatureSensorProfile sensor = sensorProfiles[i];
            if (readings[i] <= temperatureLowThreshold) // remove some candidates
            {
                int xL = (int) (sensor.location.x - turnOffRadius) - 1;
                int xH = (int) (sensor.location.x + turnOffRadius) + 1;
                int yL = (int) (sensor.location.y - turnOffRadius) - 1;
                int yH = (int) (sensor.location.y + turnOffRadius) + 1;
                for (int x = xL; x <= xH; x++)
                    for (int y = yL; y <= yH; y++) {
                        double dis = sensor.location.distance(x, y);
                        if (x < 0 || x >= xDim || y < 0 || y >= yDim)
                            continue;
                        if (dis <= turnOffRadius)
                            turnOnFlags[x][y] = false;
                    }
            }
        }

        for (int x = 0; x < xDim; x++)
            for (int y = 0; y < yDim; y++) {
                if (turnOnFlags[x][y]) p.setCellColor(x, y, Color.red);
            }


        p.drawDirtyCellsInBuffer();
        p.showBufferOnScreen();

    }

}
