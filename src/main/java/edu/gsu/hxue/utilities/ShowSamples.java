package edu.gsu.hxue.utilities;

import edu.gsu.hxue.desFire.FireSystem;
import edu.gsu.hxue.fireSMC.FireStateWithUniformWind;
import edu.gsu.hxue.fireSMC.FireStateWithUniformWind.SensorReadingVector;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.shells.DissertationProposalExpShell;
import edu.gsu.hxue.smc.AbstractState.StateFunctionNotSupportedException;
import edu.gsu.hxue.smc.GlobalConstants;
import edu.gsu.hxue.smc.samplingStrategies.ProposalSampling;

public class ShowSamples {

    public static void main(String[] args) {
        int stepLength = 1200;
        int sensorNumber = 50;
        int sensorRadius = 100; //100, once in bootstrap

        int xDim = 200;
        int yDim = 200;

        double realSpeed = 5;
        double realDirection = 125;

        double simSpeed = 6;//4;//6
        double simDirection = 105;//305;//105

        double initialLength = stepLength * 8;

        try {

            FireSystem real = new FireSystem(DissertationProposalExpShell.getRealConfig());
            real.forceToUpdateWeather(realSpeed, realDirection);
            real.run(initialLength);
            PresentationUtility.drawFire(real);
            //PresentationUtility.drawFireFront(real, Color.blue);

            FireSystem sim = new FireSystem(DissertationProposalExpShell.getSimConfig());
            sim.forceToUpdateWeather(simSpeed, simDirection);
            sim.run(initialLength);
            //PresentationUtility.drawFireFront(sim, Color.red);


            //TemperatureSensorProfile[] sensorProfiles = SensorUtility.generateSensorProfiles(GlobalConstants.RAND, xDim, yDim, sensorNumber, sensorRadius);
            TemperatureSensorProfile[] sensorProfiles = SensorUtility.generateSensorProfilesInRectangle(GlobalConstants.RAND, 50, 100, 120, 170, sensorNumber, sensorRadius);
            PresentationUtility.drawSensorPartition(xDim, yDim, SensorUtility.partitionSensors(sensorProfiles));

            FireStateWithUniformWind realState = new FireStateWithUniformWind(stepLength, real, sensorProfiles, realSpeed, realDirection);
            realState = (FireStateWithUniformWind) realState.transitionFunction();
            //PresentationUtility.drawFire(realState.getFireSystem());


            SensorReadingVector measurement = realState.measurementFunction();
            System.out.println("sensorNumber number: " + sensorNumber);
            System.out.println("measuremnt number: " + measurement.getSensorReadings().length);
            PresentationUtility.drawSensorTemperature(xDim, yDim, measurement, sensorProfiles);

            FireStateWithUniformWind simState = new FireStateWithUniformWind(stepLength, sim, sensorProfiles, simSpeed, simDirection);
            //PresentationUtility.drawSampleedFireFronts(10, simState, realState, new ProposalSampling(), measurement);
            PresentationUtility.drawSampleedFireFrontsWithSensorReadings(10, simState, realState, new ProposalSampling(), measurement, sensorProfiles);
        } catch (StateFunctionNotSupportedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
