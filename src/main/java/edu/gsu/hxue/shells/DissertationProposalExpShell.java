package edu.gsu.hxue.shells;

import edu.gsu.hxue.desFire.FireSystemConfig;
import edu.gsu.hxue.fireSMC.FireIdenticalTwinExperiment;
import edu.gsu.hxue.fireSMC.SenSimFireTwinExperiment;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.smc.GlobalConstants;
import edu.gsu.hxue.smc.weightUpdatingStrategies.KernelEstimationProposalWeight;
import edu.gsu.hxue.smc.weightUpdatingStrategies.KernelEstimationProposalWeight.KernelFunction;
import edu.gsu.hxue.utilities.SensorUtility;

import java.math.BigDecimal;

public class DissertationProposalExpShell {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: command [particleNumber] [realWindSpeed] [realWindDirection] [simWindSpeed] [simWindDirection] ");
        }

        int stepNumber = 9;
        int stepLength = 1200;
        int particleNumber = Integer.parseInt(args[0]);
        int sensorNumber = 1000;
        int sensorRadius = 100; //100, once in bootstrap

        int xDim = 200;
        int yDim = 200;

        double initialRealWindSpeed = Double.parseDouble(args[1]);
        double initialRealWindDirection = Double.parseDouble(args[2]);
        double initialSimWindSpeed = Double.parseDouble(args[3]);
        double initialSimWindDirection = Double.parseDouble(args[4]);

        TemperatureSensorProfile[] sensorProfiles = SensorUtility.generateSensorProfiles(GlobalConstants.RAND, xDim, yDim, sensorNumber, sensorRadius);

		
		/*Vector<Vector<TemperatureSensorProfile>> partition = SensorUtility.partitionSensors(sensorProfiles);
		System.out.println("sensor cluster number: " + partition.size());
		PresentationUtility.drawSensorPartitionMonitoredArea(xDim, yDim, partition);
		PresentationUtility.drawSensorPartition(xDim, yDim, partition);*/


        //FireIdenticalTwinExperiment exp = new BootstrapFireTwinExperiment(sensorProfiles, stepLength, getRealConfig(),  getSimConfig());
        FireIdenticalTwinExperiment exp = createSenSimExp(sensorProfiles, stepLength, getRealConfig(), getSimConfig());

        exp.SetRealWind(initialRealWindSpeed, initialRealWindDirection);
        exp.SetSimWind(initialSimWindSpeed, initialSimWindDirection);

        double time = System.currentTimeMillis();
        try {
            exp.runDataAssimilationExperiement(stepNumber, particleNumber);
            System.gc();
            System.out.println("=========================== All SMC done  ===========================");
            System.out.println("Memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000.0 + "MB");
            System.out.println("Execution time: " + (System.currentTimeMillis() - time) / 1000 + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static FireIdenticalTwinExperiment createSenSimExp(TemperatureSensorProfile[] sensorProfiles, int stepLength, FireSystemConfig realConfig, FireSystemConfig simConfig) {
        int kernelParticleNumbers = 5;

        KernelFunction kernel = new KernelEstimationProposalWeight.GaussianKernel();

        double bandwidth = 2000;
        BigDecimal bandWidth = BigDecimal.valueOf(bandwidth);

        return new SenSimFireTwinExperiment(sensorProfiles, stepLength, getRealConfig(), getSimConfig(),
                kernelParticleNumbers, kernel, bandWidth);
    }

    public static FireSystemConfig getRealConfig() {
        FireSystemConfig config = new FireSystemConfig();
        config.showPresentationLayer = false;
        config.useSystemMonitor = false;
        config.useHeatWriter = false;
        config.gisFileFolder = "GISData/";
        config.otherFileFolder = "OtherFireSettings/";
        config.slopeFileName = "GIS_slope_15.txt";
        config.aspectFileName = "GIS_aspect_15.txt";
        config.fuelFileName = "GIS_fuel_15.txt";
        config.initialWeatherFileName = "weather_artificial_unchangedWind_5_125.txt"; //useless since it will be reset
        config.initialIgnitionFile = "IgnitionPoints_SMC_real.txt";
        config.laterIgnitionFile = "IgnitionPoints_SMC_later_real.txt";
        config.initialContainedCells = "InitialContainedCells_empty.txt";
        config.heatUpdateInterval = 1800;
        return config;
    }

    public static FireSystemConfig getSimConfig() {
        FireSystemConfig config = new FireSystemConfig();
        config.showPresentationLayer = false;
        config.useSystemMonitor = false;
        config.useHeatWriter = false;
        config.gisFileFolder = "GISData/";
        config.otherFileFolder = "OtherFireSettings/";
        config.slopeFileName = "GIS_slope_15.txt";
        config.aspectFileName = "GIS_aspect_15.txt";
        config.fuelFileName = "GIS_fuel_15.txt";
        config.initialWeatherFileName = "weather_artificial_unchangedWind_6_105.txt";//useless since it will be reset
        config.initialIgnitionFile = "IgnitionPoints_SMC_sim.txt";
        config.laterIgnitionFile = "IgnitionPoints_SMC_later_sim.txt";
        config.initialContainedCells = "InitialContainedCells_empty.txt";
        config.heatUpdateInterval = 1800;

        return config;
    }

}
