package com.md_5.bot.mc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {

    private float yaw;
    private float pitch;
    private double x;
    private double y;
    private double z;
    private double stance;
    private boolean onGround;

    public Location() {
    }
}
