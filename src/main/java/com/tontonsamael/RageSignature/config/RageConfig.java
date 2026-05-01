package com.tontonsamael.RageSignature.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class RageConfig {
    public static final BuilderCodec<RageConfig> CODEC = BuilderCodec.builder(RageConfig.class, RageConfig::new)
            .append(new KeyedCodec<Integer>("DecayDelay", Codec.INTEGER),
                    RageConfig::setDelay,
                    RageConfig::getDelay
            ).add()
            .append(new KeyedCodec<Integer>("DecayPercent", Codec.INTEGER),
                    RageConfig::setDecay,
                    RageConfig::getDecay
            ).add()
            .append(new KeyedCodec<Float>("HitRatio", Codec.FLOAT),
                    RageConfig::setRatio,
                    RageConfig::getRatio
            ).add()
            .build();

    private int delay = 15;
    private int decay = 10;
    private float ratio = 1f;

    public RageConfig() {}

    public int getDelay() {
        return delay;
    }

    public void setDelay(int seconds) {
        this.delay = seconds;
    }

    public int getDecay() {
        return decay;
    }

    public void setDecay(int percent) {
        this.decay = Math.clamp(percent, 0, 100);
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = Math.clamp(ratio, 0.01f, Math.abs(ratio));
    }

    public int hashCode() {
        return Float.hashCode(ratio * 100000) + Integer.hashCode(delay) * 100 + Integer.hashCode(decay);
    }

    public boolean equals(RageConfig other) {
        return this.hashCode() == other.hashCode();
    }
}
