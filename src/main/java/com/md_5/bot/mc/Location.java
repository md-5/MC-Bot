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

    public Location() {
    }

    public Location(double x, double y, double z) {
        this(0, 0, x, y, z);
    }

    /**
     * Set the yaw. This helper method will convert it to degrees and cast to
     * float.
     *
     * @param yaw the yaw to set, will be converted to degrees.
     */
    public void setYawRadians(float yaw) {
        this.yaw = (float) Math.toDegrees(yaw);
    }

    /**
     * Set the pitch. This helper method will convert it to degrees and cast to
     * float.
     *
     * @param pitch the pitch to set, will be converted to degrees.
     */
    public void setPitchRadians(float pitch) {
        this.pitch = (float) Math.toDegrees(pitch);
    }

    /**
     * Adds the location by another.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return the same location
     */
    public Location add(double x, double y, double z, float yaw, float pitch) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.yaw += yaw;
        this.pitch += pitch;
        return this;
    }

    public Location add(double x, double y, double z) {
        return this.add(x, y, z, 0, 0);
    }

    /**
     * Gets the magnitude of the location, defined as sqrt(x^2+y^2+z^2). The
     * value of this method is not cached and uses a costly square-root
     * function, so do not repeatedly call this method to get the location's
     * magnitude. NaN will be returned if the inner result of the sqrt()
     * function overflows, which will be caused if the length is too long. Not
     * world-aware and orientation independent.
     *
     * @return the magnitude
     */
    public double length() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * Gets the magnitude of the location squared. Not world-aware and
     * orientation independent.
     *
     * @return the magnitude
     */
    public double lengthSquared() {
        return Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
    }

    /**
     * Get the distance between this location and another. The value of this
     * method is not cached and uses a costly square-root function, so do not
     * repeatedly call this method to get the location's magnitude. NaN will be
     * returned if the inner result of the sqrt() function overflows, which will
     * be caused if the distance is too long.
     *
     * @see Vector
     * @param o The other location
     * @return the distance
     * @throws IllegalArgumentException for differing worlds
     */
    public double distance(Location o) {
        return Math.sqrt(distanceSquared(o));
    }

    /**
     * Get the squared distance between this location and another.
     *
     * @param o The other location
     * @return the distance
     */
    public double distanceSquared(Location o) {
        return Math.pow(x - o.x, 2) + Math.pow(y - o.y, 2) + Math.pow(z - o.z, 2);
    }

    /**
     * Performs scalar multiplication, multiplying all components with a scalar.
     *
     * @param m The factor
     * @see Vector
     * @return the same location
     */
    public Location multiply(double m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    /**
     * Safely converts a double (location coordinate) to an int (block
     * coordinate)
     *
     * @param loc Precise coordinate
     * @return Block coordinate
     */
    public static int locToBlock(double loc) {
        return (int) Math.floor(loc);
    }
}
