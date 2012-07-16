package com.md_5.bot.mc;

import lombok.Data;

@Data
public class PingResponse {

    private final String motd;
    private final int onlinePlayers;
    private final int maxPlayers;
}
