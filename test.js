importPackage(Packages.com.md_5.bot.mc);
importPackage(Packages.com.md_5.bot.mc.entity);
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
            var nearby = bot.getNearbyEntities(4);
            if (nearby.size() > 0){
                bot.hit(nearby.get(0));
            }
            Thread.sleep(500);
        }
    }).start();

} else {
    out.println("Failed to connect.");
}
