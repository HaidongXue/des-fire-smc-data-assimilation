package edu.gsu.hxue.fireSMC;

import edu.gsu.hxue.desFire.FireSystemConfig;
import edu.gsu.hxue.sensors.TemperatureSensorProfile;
import edu.gsu.hxue.smc.AbstractParticleSystem;
import edu.gsu.hxue.smc.Particle;
import edu.gsu.hxue.smc.specialParticleSystems.BootstrapFilter;

import java.util.Vector;

public class BootstrapFireTwinExperiment extends FireIdenticalTwinExperiment {

    public BootstrapFireTwinExperiment(TemperatureSensorProfile[] sensorProfiles, int stepLength, FireSystemConfig realFireConfig, FireSystemConfig simFireConfig) {
        super(sensorProfiles, stepLength, realFireConfig, simFireConfig);
    }

    @Override
    protected AbstractParticleSystem createParticleSystem(Vector<Particle> particleSet) {
        return new BootstrapFilter(particleSet);
    }
}
