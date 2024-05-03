import java.io.IOException;

public class NetworkingServer 
{
	private GameServerUDP thisUDPServer;
	private GameServerTCP thisTCPServer;
	private NPCcontroller npcCtrl;

	public NetworkingServer(int serverPort, String protocol) 
	{	
		npcCtrl = new NPCcontroller();
		try 
		{	if(protocol.toUpperCase().compareTo("TCP") == 0)
			{	thisTCPServer = new GameServerTCP(serverPort);
			}
			else
			{	thisUDPServer = new GameServerUDP(serverPort, npcCtrl);
			}
		} 
		catch (IOException e) 
		{	e.printStackTrace();
		}
		npcCtrl.start(thisUDPServer);
	}

	public static void main(String[] args) 
	{	if(args.length > 1)
		{	NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		}
	}

}
