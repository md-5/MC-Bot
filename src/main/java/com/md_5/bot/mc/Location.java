package com.md_5.bot.mc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
public class Location {

    @Setter(AccessLevel.NONE)
    private float yaw;
    private float pitch;
    private double x;
    private double y;
    private double z;
    private double stance;
    private boolean onGround;

    public Location() {
    }

    /**
     * Set the yaw. This helper method will convert it to degrees and cast to
     * float.
     *
     * @param yaw the yaw to set, will be converted to degrees.
     */
    public void setYaw(float yaw) {
        this.yaw = (float) Math.toDegrees(yaw);
    }
}
