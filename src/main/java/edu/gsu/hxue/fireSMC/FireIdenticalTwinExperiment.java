package edu.gsu.hxue.fireSMC;

import edu.gsu.hxue.desFire.FireSystem;
import edu.gsu.hxue.desFire.FireSystemConfig;
import edu.gsu.hxue.desFire.exceptions.NotSupportedFunctionException;
import edu.gsu.hxue.identicalTwinExperiments.AbstractIdenticalTwinExperiment;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.smc.AbstractState;
import edu.gsu.hxue.smc.Particle;
import edu.gsu.hxue.utilities.PresentationUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public abstract class FireIdenticalTwinExperiment extends AbstractIdenticalTwinExperiment {
    TemperatureSensorProfile[] sensorProfiles;
    int stepLength = 1200;
    FireSystemConfig realFireConfig;
    FireSystemConfig simFireConfig;

    double initialRealWindSpeed = 5;
    double initialRealWindDirection = 125;

    double initialSimWindSpeed = 6;
    double initialSimWindDirection = 105;

    Vector<smcFireComparison> numbericResults = new Vector<smcFireComparison>();

    private class smcFireComparison {
        public double time;
        public double simError;
        public double filteredError;

        public double realFrontLength;
        public double realIgnitedArea;

        public double simFrontLength;
        public double simIgnitedArea;

        public double filteredFrontLength;
        public double filteredIgnitedArea;


        public smcFireComparison(
                double time, double simError, double filteredError,
                double realFrontLength, double realIgnitedArea,
                double simFrontLength, double simIgnitedArea,
                double filteredFrontLength, double filteredIgnitedArea
        ) {
            this.time = time;
            this.simError = simError;
            this.filteredError = filteredError;
            this.realFrontLength = realFrontLength;
            this.realIgnitedArea = realIgnitedArea;
            this.simFrontLength = simFrontLength;
            this.simIgnitedArea = simIgnitedArea;
            this.filteredFrontLength = filteredFrontLength;
            this.filteredIgnitedArea = filteredIgnitedArea;
        }
    }

    public void SetRealWind(double speed, double direction) {
        this.initialRealWindDirection = direction;
        this.initialRealWindSpeed = speed;
    }

    public void SetSimWind(double speed, double direction) {
        this.initialSimWindDirection = direction;
        this.initialSimWindSpeed = speed;
    }

    public FireIdenticalTwinExperiment(TemperatureSensorProfile[] sensorProfiles, int stepLength, FireSystemConfig realFireConfig, FireSystemConfig simFireConfig) {
        this.sensorProfiles = sensorProfiles;
        this.stepLength = stepLength;

        this.realFireConfig = realFireConfig;
        this.simFireConfig = simFireConfig;
    }

    @Override
    protected AbstractState createRealSystem() {
        FireSystem sys = new FireSystem(this.realFireConfig);
        return new FireStateWithUniformWind(stepLength, sys, sensorProfiles, initialRealWindSpeed, initialRealWindDirection);
    }

    @Override
    protected AbstractState createSimulatedSystem() {
        FireSystem sys = new FireSystem(this.simFireConfig);
        return new FireStateWithUniformWind(stepLength, sys, sensorProfiles, initialSimWindSpeed, initialSimWindDirection);
    }

    @Override
    protected void reportOnStep(int step) throws NotSupportedFunctionException {
        double reportTime = 10000;
        boolean reportFigure = false;
        boolean reportError = true;
        boolean saveState = true;
        double displayScale = 1;
        int currentTime = stepLength * step;

        FireSystem realFire = ((FireStateWithUniformWind) this.realSystem).getFireSystem();
        FireSystem simFire = ((FireStateWithUniformWind) this.simulatedSystem).getFireSystem();

        Vector<Particle> particleSet = this.particleSystem.getParticleSet();
        FireSystem[] particleFires = new FireSystem[particleSet.size()];
        for (int i = 0; i < particleFires.length; i++)
            particleFires[i] = ((FireStateWithUniformWind) particleSet.elementAt(i).state).getFireSystem();

        FireSystem highestWeightFire = ((FireStateWithUniformWind) particleSystem.getHighestWeightParticle().state).getFireSystem();

        this.numbericResults.add(
                this.new smcFireComparison(
                        currentTime, realFire.mismatchedCellNumber(simFire), realFire.mismatchedCellNumber(highestWeightFire),
                        realFire.getFireFrontLength(), realFire.getIgnitedArea(),
                        simFire.getFireFrontLength(), simFire.getIgnitedArea(),
                        highestWeightFire.getFireFrontLength(), highestWeightFire.getIgnitedArea()
                )
        );

        System.out.print("SMC =============================== Step" + step + " done! Current time = " + currentTime + " Error: " + realFire.mismatchedCellNumber(highestWeightFire));
        System.out.println(" Memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000.0 + "MB");


        if (currentTime > reportTime) {
            if (reportFigure) {
                PresentationUtility.drawFireFronts(realFire, simFire, highestWeightFire, currentTime, displayScale);
                PresentationUtility.drawFireFrontsWithParticleCloud(realFire, simFire, particleFires, currentTime, displayScale);
            }

            if (saveState) {
                System.out.println("Saving fire states.");
                String resultFolder = "results";
                File folder = new File(resultFolder);
                if (!folder.exists()) folder.mkdir();

                String realFireFilePath = resultFolder + "/" + currentTime + "_RealFire.fir";
                realFire.serializeTo(realFireFilePath);

                String simFireFilePath = resultFolder + "/" + currentTime + "_simFire.fir";
                simFire.serializeTo(simFireFilePath);

                String highestWeightFireFilePath = resultFolder + "/" + currentTime + "_highestWeightFire.fir";
                highestWeightFire.serializeTo(highestWeightFireFilePath);

                for (int i = 0; i < particleFires.length; i++) {
                    String particleFireFilePath = resultFolder + "/" + currentTime + "_Par" + i + "_highestWeightFire.fir";
                    particleFires[i].serializeTo(particleFireFilePath);
                }
                System.out.println("Saved fire states.");
            }

            if (reportError) {
                System.out.println("Saving numeric results.");
                String resultFolder = "results";
                File folder = new File(resultFolder);
                if (!folder.exists()) folder.mkdir();
                String filePath = resultFolder + "/NumbericResults.txt";

                // Create file, if not existing
                File resultFile = new File(filePath);
                if (!resultFile.exists())
                    try {
                        resultFile.createNewFile();
                    } catch (IOException e) {
                        System.err.println("Failed to create file: " + filePath);
                        System.exit(1);
                    }

                // Create PrintWriter
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(resultFile);
                    writer.println("Time\tSimulated Fire Error\tFiltered Fire Error\t" +
                            "Real Fire Front Length\tReal Fire Ignited Area\t" +
                            "Simulated Fire Front Length\tSimulated Fire Ignited Area\t" +
                            "Filtered Fire Front Length\tFiltered Fire Ignited Area");
                    for (smcFireComparison c : this.numbericResults)
                        writer.println(c.time + "\t" + c.simError + "\t" + c.filteredError + "\t" +
                                c.realFrontLength + "\t" + c.realIgnitedArea + "\t" +
                                c.simFrontLength + "\t" + c.simIgnitedArea + "\t" +
                                c.filteredFrontLength + "\t" + c.filteredIgnitedArea);
                    writer.close();
                    System.out.println("Saved numeric results.");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
