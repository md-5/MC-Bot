package com.md_5.bot.mc.entity;

import com.md_5.bot.mc.Location;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Entity {

    private final int id;
    private Location location;
}
