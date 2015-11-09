package edu.gsu.hxue.fireSMC;

import edu.gsu.hxue.desFire.FireSystem;
import edu.gsu.hxue.desFire.exceptions.InvalidLogicException;
import edu.gsu.hxue.desFire.exceptions.NotSupportedFunctionException;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.smc.AbstractState;
import edu.gsu.hxue.smc.GlobalConstants;
import edu.gsu.hxue.utilities.SensorUtility;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.util.Random;
import java.util.Vector;

public class FireStateWithUniformWind extends AbstractState {
    private FireSystem fireSys; // a DES fire system
    private double stepLength; // the simulation length for one step
    private TemperatureSensorProfile[] sensorProfiles; // sensor locations, immutable
    private double windSpeed = 0;
    private double windDirection = 0;

    public FireStateWithUniformWind clone() {
        FireStateWithUniformWind c = null;
        try {
            // shallow copy
            c = (FireStateWithUniformWind) super.clone();
            // deep copy
            c.fireSys = (FireSystem) fireSys.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return c;
    }

    @Override
    public AbstractTransitionRandomComponent drawNextRandomComponentSample() {
        Random r = edu.gsu.hxue.smc.GlobalConstants.RAND;
        double speedMoveSigma = 1;
        double directionMoveSigma = 15;
        return new WindMove(r.nextGaussian() * speedMoveSigma, r.nextGaussian() * directionMoveSigma);
    }

    public FireStateWithUniformWind(double stepLength, FireSystem fireSys, TemperatureSensorProfile[] sensorProfiles, double windSpeed, double windDirection) {
        this.stepLength = stepLength;
        this.fireSys = fireSys;
        this.sensorProfiles = sensorProfiles;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
    }

    @Override
    public AbstractState transitionFunction() throws StateFunctionNotSupportedException {
        FireSystem newSys = (FireSystem) fireSys.clone();
        newSys.forceToUpdateWeather(this.windSpeed, this.windDirection); // use the parameters at this step as the ones for the next step
        try {
            newSys.run(stepLength);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new FireStateWithUniformWind(stepLength, newSys, this.sensorProfiles, this.windSpeed, this.windDirection);
    }

    @Override
    public AbstractState transitionModel(AbstractTransitionRandomComponent random) throws StateFunctionNotSupportedException {
        WindMove wind = (WindMove) random;
        FireSystem newSys = (FireSystem) fireSys.clone();
        double newWindSpeed = this.windSpeed + wind.windSpeedMove;
        double newWindDirection = this.windDirection + wind.windDirectionMove;
        newSys.forceToUpdateWeather(newWindSpeed, newWindDirection);
        try {
            newSys.run(stepLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FireStateWithUniformWind(stepLength, newSys, this.sensorProfiles, newWindSpeed, newWindDirection);
    }

    @Override
    public SensorReadingVector measurementFunction() throws StateFunctionNotSupportedException {
        SensorReadingVector measurement = this.new SensorReadingVector();
        try {
            measurement.setSensorReadings(fireSys.generateSensorReadings(sensorProfiles));
        } catch (WrongSensorNumberException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return measurement;
    }

    @Override
    public void setDescription(String des) {
        fireSys.setName(des + "_");
    }

    @Override
    public BigDecimal measurementPdf(AbstractMeasurement measurement) throws StateFunctionNotSupportedException {

        double[] sensorReadings = ((SensorReadingVector) measurement).getSensorReadings();
        double[] simTrueReadings = this.fireSys.generateSensorReadings(sensorProfiles);
        double sigma = 200;  //20 was good for most cases

		/*
         * double variance = sigma*sigma;
		 * 
		 * double[][] cov = new double[sensorProfiles.length][sensorProfiles.length]; for(int i=0; i<sensorProfiles.length; i++) for(int j=0; j<sensorProfiles.length; j++) { if(i==j) cov[i][j] =
		 * variance; else cov[i][j] =0; }
		 * 
		 * MultivariateNormalDistribution mn = new MultivariateNormalDistribution(simTrueReadings, cov); BigDecimal weight = BigDecimal.valueOf(mn.density(sensorReadings));
		 */

        NormalDistribution norm = new NormalDistribution(0, sigma);
        BigDecimal weight = BigDecimal.ONE;
        for (int i = 0; i < sensorProfiles.length; i++) {
            double normResult = norm.density(sensorReadings[i] - simTrueReadings[i]);

            double minNorm = 1E-300; // if not doing so, a small value will become 0, and mess up the weight
            if (normResult < minNorm)
                normResult = minNorm;

            weight = weight.multiply(BigDecimal.valueOf(normResult));
            // System.out.printf("Inter Weight-"+i+" : %5e%n", weight);
        }

        // System.out.printf("A likelihood weight: %5e%n", weight);

        return weight;
    }

    public FireSystem getFireSystem() {
        return fireSys;
    }

    @Override
    public AbstractState propose(AbstractMeasurement measurement) throws StateFunctionNotSupportedException, InvalidLogicException {
        double sim_confidence = 0.8;
        double sen_confidence = 0.8;

        // Prior sampling
        AbstractTransitionRandomComponent random = this.drawNextRandomComponentSample();
        WindMove wind = (WindMove) random;
        FireSystem newSys = (FireSystem) fireSys.clone();
        double newWindSpeed = this.windSpeed + wind.windSpeedMove;
        double newWindDirection = this.windDirection + wind.windDirectionMove;
        newSys.forceToUpdateWeather(newWindSpeed, newWindDirection);
        try {
            newSys.run(stepLength);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Sensor based editing

        // // Partition sensors
        SensorWithReading[] sensors = new SensorWithReading[sensorProfiles.length];
        double[] sensorReadings = ((SensorReadingVector) measurement).getSensorReadings();
        for (int i = 0; i < sensorProfiles.length; i++) {
            sensors[i] = new SensorWithReading();
            sensors[i].location = sensorProfiles[i].location;
            sensors[i].radius = sensorProfiles[i].radius;
            sensors[i].reading = sensorReadings[i];
        }
        Vector<Vector<TemperatureSensorProfile>> clusters = SensorUtility.partitionSensors(sensors);

        // //edit prior
        for (Vector<TemperatureSensorProfile> cluster : clusters) {
            double u = GlobalConstants.RAND.nextDouble() * (sim_confidence + sen_confidence);

            if (u > sim_confidence) // use sim info
            {
                double temperatureHighThreshold = 150;
                double temperatureLowThreshold = 20;

                // good turn off-on parameters
                // case 1: off(1, 9) on(2, 100) good;
                // case 2: off(1, 9) on(2, 100) good;
                // case 3: off(1, 9) on(2, 100) good;
                // case 4: off(1, 9) on(2, 100) good;
                // case 5:
                boolean turnOff = true;
                double turnOffSigma = 1;
                double turnOffRadiusMean = 9;
                double turnOffRadius = turnOffRadiusMean + GlobalConstants.RAND.nextGaussian() * turnOffSigma;// 80/15.0;

                double turnOnSigma = 2;
                double turnOnFactor = 100; // mean = temperature/turnOnFactor ;tried 50
                double maxTurnOnRadisus = 1000 / turnOnFactor;

                // turn on fires for high temperature sensors
                int xDim = newSys.getXDim();
                int yDim = newSys.getYDim();
                boolean[][] turnOnFlags = new boolean[xDim][yDim];
                for (int i = 0; i < xDim; i++)
                    for (int j = 0; j < yDim; j++)
                        turnOnFlags[i][j] = false;

                for (TemperatureSensorProfile t : cluster) {
                    SensorWithReading sensor = (SensorWithReading) t;
                    double turnOnRadius = sensor.reading / turnOnFactor + GlobalConstants.RAND.nextGaussian() * turnOnSigma;
                    if (turnOnRadius > maxTurnOnRadisus)
                        turnOnRadius = maxTurnOnRadisus;
                    if (sensor.reading >= temperatureHighThreshold) // set turn on candidates
                    {
                        int xL = (int) (sensor.location.x - turnOnRadius) - 1;
                        int xH = (int) (sensor.location.x + turnOnRadius) + 1;
                        int yL = (int) (sensor.location.y - turnOnRadius) - 1;
                        int yH = (int) (sensor.location.y + turnOnRadius) + 1;

                        // to see if the area is already burning
                        boolean alreadyOn = false;
                        for (int x = xL; x <= xH; x++)
                            for (int y = yL; y <= yH; y++) {
                                double dis = sensor.location.distance(x, y);
                                if (x < 0 || x >= xDim || y < 0 || y >= yDim)
                                    continue;
                                if (dis <= turnOnRadius && newSys.getFireCell(x, y).isBurning()) {
                                    alreadyOn = true;
                                    break;

                                }
                            }

                        // prepare to turn on all whole area if there is no burning cell
                        if (!alreadyOn)
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

                for (TemperatureSensorProfile t : cluster) {
                    SensorWithReading sensor = (SensorWithReading) t;

                    if (sensor.reading <= temperatureLowThreshold) // remove some candidates
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

                for (int i = 0; i < xDim; i++)
                    for (int j = 0; j < yDim; j++)
                        if (turnOnFlags[i][j])
                            newSys.forceToIgnite(i, j);

                // turn off fires for low temperature sensors
                if (turnOff) {
                    for (TemperatureSensorProfile t : cluster) {
                        SensorWithReading sensor = (SensorWithReading) t;

                        if (sensor.reading <= temperatureLowThreshold) // remove some candidates
                        {
                            int xL = (int) (sensor.location.x - turnOffRadius) - 1;
                            int xH = (int) (sensor.location.x + turnOffRadius) + 1;
                            int yL = (int) (sensor.location.y - turnOffRadius) - 1;
                            int yH = (int) (sensor.location.y + turnOffRadius) + 1;
                            for (int x = xL; x <= xH; x++)
                                for (int y = yL; y <= yH; y++) {
                                    if (x < 0 || x >= xDim || y < 0 || y >= yDim)
                                        continue;
                                    double dis = sensor.location.distance(x, y);
                                    if (dis < turnOffRadius) {
                                        if (!newSys.getFireCell(x, y).isBurning())
                                            continue; // unburned
                                        // if(newSys.getFireCell(x, y).isBurnedOut()) continue; // burned out
                                        newSys.forceToUnburn(x, y);
                                    }
                                }

                        }
                    }
                }
            }
        }

        // Create and return the state
        return new FireStateWithUniformWind(stepLength, newSys, this.sensorProfiles, newWindSpeed, newWindDirection);
    }

    // Not supported functions
    @Override
    public BigDecimal transitionPdf(AbstractState nextState) throws StateFunctionNotSupportedException {
        throw new AbstractState.StateFunctionNotSupportedException();
    }

    @Override
    public AbstractMeasurement measurementModel(AbstractMeasurementRandomComponent random) throws StateFunctionNotSupportedException {
        throw new AbstractState.StateFunctionNotSupportedException();
    }

    @Override
    public BigDecimal proposalPdf(AbstractMeasurement measurement) throws StateFunctionNotSupportedException {
        throw new AbstractState.StateFunctionNotSupportedException();
    }

    @Override
    public AbstractState generateNoisedState() throws StateFunctionNotSupportedException {
        throw new AbstractState.StateFunctionNotSupportedException();
    }

    // end of not-supported functions

    public static class WindMove extends AbstractTransitionRandomComponent {
        public double windSpeedMove;
        public double windDirectionMove;

        public WindMove(double speedMove, double directionMove) {
            this.windDirectionMove = directionMove;
            this.windSpeedMove = speedMove;
        }
    }

    public class SensorReadingVector extends AbstractMeasurement {
        private double[] sensorReadings;

        public double[] getSensorReadings() {
            return this.sensorReadings;
        }

        public void setSensorReadings(double[] readings) throws WrongSensorNumberException {
            if (readings.length != sensorProfiles.length) {
                throw new WrongSensorNumberException();
            }
            sensorReadings = readings;
        }
    }

    public static class WrongSensorNumberException extends Exception {
        private static final long serialVersionUID = -6947753831195262801L;
    }

    private static class SensorWithReading extends TemperatureSensorProfile {
        double reading;
    }

    @Override
    public double distance(AbstractState sample) {
        FireSystem another = ((FireStateWithUniformWind) sample).fireSys;
        try {
            return this.fireSys.mismatchedCellNumber(another);
        } catch (NotSupportedFunctionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }
}
