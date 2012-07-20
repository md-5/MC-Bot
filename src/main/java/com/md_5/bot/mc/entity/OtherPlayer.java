package com.md_5.bot.mc.entity;

import com.md_5.bot.mc.Location;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OtherPlayer extends Entity {

    private final String name;
    private int currentItem;

    public OtherPlayer(int id, String name, Location location, int currentItem) {
        super(id, location);
        this.name = name;
        this.currentItem = currentItem;
    }
}
