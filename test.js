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
            moveCloser();
            attackNearby(); 
        }
    }).start();

} else {
    out.println("Failed to connect.");
}
/**
 * Helper method to look and move to the closest entity within 16 blocks.
 */
function moveCloser(){
    var nearby = bot.getNearbyEntities(16);
    if (nearby.size() > 0){
        bot.look(nearby.first());
    }
}
/**
 * Punch the closest entity at a vanilla compliant speed.
 */
var hits = 0;
var lastAttack = 0;
function attackNearby(){
    var nearby = bot.getNearbyEntities(3.75);
    if (nearby.size() > 0){
        var currentTime = System.currentTimeMillis();
        if (currentTime - lastAttack > 250){
            bot.attack(nearby.first());
            hits++;
            
            if (hits % 15 == 0){
                lastAttack = currentTime + 1000;
            } else {
                lastAttack = currentTime;
            }
        }
    }
}
