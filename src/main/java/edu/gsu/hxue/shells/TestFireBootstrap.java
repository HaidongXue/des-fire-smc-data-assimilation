package edu.gsu.hxue.shells;

import edu.gsu.hxue.desFire.FireSystemConfig;
import edu.gsu.hxue.fireSMC.BootstrapFireTwinExperiment;
import edu.gsu.hxue.fireSMC.FireIdenticalTwinExperiment;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.smc.GlobalConstants;
import edu.gsu.hxue.utilities.SensorUtility;

public class TestFireBootstrap {
    public static void main(String[] args) {

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        int stepNumber = 9;
        int stepLength = 1200;
        int particleNumber = 5;
        int sensorNumber = 200;
        int sensorRadius = 100;

        TemperatureSensorProfile[] sensorProfiles = SensorUtility.generateSensorProfiles(GlobalConstants.RAND, 200, 200, sensorNumber, sensorRadius);
        FireIdenticalTwinExperiment exp = new BootstrapFireTwinExperiment(sensorProfiles, stepLength, getRealConfig(), getSimConfig());

        double time = System.currentTimeMillis();
        try {
            exp.runDataAssimilationExperiement(stepNumber, particleNumber);
            System.gc();
            System.out.println("Memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000.0 + "MB");
            System.out.println("Execution time: " + (System.currentTimeMillis() - time) / 1000 + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FireSystemConfig getRealConfig() {
        FireSystemConfig config = new FireSystemConfig();
        config.showPresentationLayer = false;
        config.useSystemMonitor = false;
        config.useHeatWriter = false;
        config.gisFileFolder = "C:\\Users\\Haydon\\hdGit\\des-fire-smc\\GISData/";
        config.otherFileFolder = "C:\\Users\\Haydon\\hdGit\\des-fire-smc\\OtherFireSettings/";
        config.slopeFileName = "GIS_slope_15.txt";
        config.aspectFileName = "GIS_aspect_15.txt";
        config.fuelFileName = "GIS_fuel_15.txt";
        config.initialWeatherFileName = "weather_artificial_unchangedWind_5_125.txt"; //useless since it will be reset
        config.initialIgnitionFile = "IgnitionPoints_SMC.txt";
        config.laterIgnitionFile = "IgnitionPoints_SMC_later_empty.txt";
        config.initialContainedCells = "InitialContainedCells_empty.txt";
        config.heatUpdateInterval = 1800;
        return config;
    }

    private static FireSystemConfig getSimConfig() {
        FireSystemConfig config = new FireSystemConfig();
        config.showPresentationLayer = false;
        config.useSystemMonitor = false;
        config.useHeatWriter = false;
        config.gisFileFolder = "C:\\Users\\Haydon\\hdGit\\des-fire-smc\\GISData/";
        config.otherFileFolder = "C:\\Users\\Haydon\\hdGit\\des-fire-smc\\OtherFireSettings/";
        config.slopeFileName = "GIS_slope_15.txt";
        config.aspectFileName = "GIS_aspect_15.txt";
        config.fuelFileName = "GIS_fuel_15.txt";
        config.initialWeatherFileName = "weather_artificial_unchangedWind_6_105.txt";//useless since it will be reset
        config.initialIgnitionFile = "IgnitionPoints_SMC.txt";
        config.laterIgnitionFile = "IgnitionPoints_SMC_later_empty.txt";
        config.initialContainedCells = "InitialContainedCells_empty.txt";
        config.heatUpdateInterval = 1800;

        return config;
    }

}
