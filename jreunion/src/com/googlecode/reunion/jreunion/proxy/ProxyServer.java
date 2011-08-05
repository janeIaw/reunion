package com.googlecode.reunion.jreunion.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import com.googlecode.reunion.jreunion.network.NetworkThread;
import com.googlecode.reunion.jreunion.server.packets.Packet;

public class ProxyServer extends NetworkThread<ProxyConnection> {
	
	InetSocketAddress internal;
	InetSocketAddress external;
	
	private Map<Integer, ProxyConnection> sessions = new HashMap<Integer, ProxyConnection>();
	
	public ProxyServer(InetSocketAddress internal, InetSocketAddress external) throws IOException {
		super(internal);
		this.internal = internal;
		this.external = external;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws  
	 */
	public static void main(String[] args) throws Exception {
		
		Logger logger = Logger.getRootLogger();
		logger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m\r\n"){
			
			@Override
			public String format(LoggingEvent event) {

				String result = super.format(event);
				if(result.endsWith("\n\r\n")){
					
					result = result.substring(0, result.length()-2);
				}
				return result;
			}
			
		},ConsoleAppender.SYSTEM_OUT));
		
		InetSocketAddress internal = new InetSocketAddress(4005);
		InetSocketAddress external = new InetSocketAddress(InetAddress.getByName("192.168.1.199"), 4005);
		ProxyServer proxy = new ProxyServer(internal, external);
		proxy.start();
			
	}
	
	public void onPacketReceived(ProxyConnection connection, Packet packet){
		
		
	}
	
	@Override
	public ProxyConnection createConnection(SocketChannel socketChannel) {
		try {
			return new ProxyConnection(this, socketChannel);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private int sessionIter = 0;
	@Override
	public void onAccept(ProxyConnection connection) {
		System.out.println(connection.getSocketChannel().socket()+" accepted");
		
		synchronized(sessions){
			while(sessions.keySet().contains(sessionIter)){
				if(sessionIter<Integer.MAX_VALUE){
					sessionIter++;
				}else{
					sessionIter = 0;
				}
			}
			sessions.put(sessionIter, connection);
		}
	}

	@Override
	public void onDisconnect(ProxyConnection connection) {
		System.out.println(connection.getSocketChannel().socket()+" disconnected");
		synchronized(sessions){
			Iterator<Entry<Integer, ProxyConnection>> iterator = sessions.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<Integer, ProxyConnection> entry = iterator.next();
				if(entry.getValue()==connection){
					iterator.remove();
					// Handle disconnect
				}
			}
		}
	}
}