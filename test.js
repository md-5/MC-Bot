importPackage(Packages.com.md_5.bot.mc);
var connection = new Connection("127.0.0.1", 25565);
connection.setUsername("md_5");
connection.connect();

while (connection.isConnected()){
	var packet = connection.getPacket();
	out.println("Js got packet: " + PacketUtil.getId(packet));
}
