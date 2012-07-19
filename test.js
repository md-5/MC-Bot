importPackage(Packages.com.md_5.bot.mc);
importPackage(Packages.java.lang);

var bot = new Connection("127.0.0.1", 25565);
bot.setUsername("sha_1");

if (bot.connect()){

    // read thread
    new Thread(function run(){
        while (bot.isConnected()){
            
        }
    }).start();

    // write thread
    new Thread(function run(){
        while (bot.isConnected()){
            var first = true;
            if (bot.getLocation() != null){
                if (first){
                    bot.getLocation().setYaw(0);
                    first = false;
                }
                bot.moveRelative(0.20,0);
                Thread.sleep(75);
            // 2.5 blocks a second
            }
        }
    }).start();

} else {
    out.println("Failed to connect.");
}
