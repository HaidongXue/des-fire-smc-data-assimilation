package edu.gsu.hxue.fireSMC;

import edu.gsu.hxue.desFire.FireSystemConfig;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.smc.AbstractParticleSystem;
import edu.gsu.hxue.smc.Particle;
import edu.gsu.hxue.smc.specialParticleSystems.SenSimFilter;
import edu.gsu.hxue.smc.weightUpdatingStrategies.KernelEstimationProposalWeight.KernelFunction;

import java.math.BigDecimal;
import java.util.Vector;

public class SenSimFireTwinExperiment extends FireIdenticalTwinExperiment {
    private KernelFunction kernel;
    private BigDecimal bandWidth;
    private int kernelParticleNumbers;


    public SenSimFireTwinExperiment(TemperatureSensorProfile[] sensorProfiles, int stepLength, FireSystemConfig realFireConfig, FireSystemConfig simFireConfig,
                                    int kernelParticleNumbers, KernelFunction kernel, BigDecimal bandWidth) {
        super(sensorProfiles, stepLength, realFireConfig, simFireConfig);

        this.kernel = kernel;
        this.bandWidth = bandWidth;
        this.kernelParticleNumbers = kernelParticleNumbers;
    }

    @Override
    protected AbstractParticleSystem createParticleSystem(Vector<Particle> particleSet) {
        return new SenSimFilter(particleSet, kernelParticleNumbers, this.kernel, this.bandWidth);
    }
}
