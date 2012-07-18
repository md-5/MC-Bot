importPackage(Packages.com.md_5.bot.mc);
importPackage(Packages.java.lang);

var connection = new Connection("127.0.0.1", 25565);
connection.setUsername("md_5");

if (connection.connect()){

    // read thread
    new Thread(function run(){
        while (connection.isConnected()){
            
        }
    }).start();

    // write thread
    new Thread(function run(){
        while (connection.isConnected()){
            
        }
    }).start();

} else {
    out.println("Failed to connect.");
}
