

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SMTPserver {

	private static Charset messageCharset = null;
	/* dictionary to store sender information */
	private static Map<Integer, String> dictSender = null;
	/* dictionary to store receiver information */
	private static Map<Integer, String> dictRcpt = null;
	
	
 
	/* Main program loop */
																								public static void main(String[] args) throws IOException {

		// create a new selector
		Selector selector = Selector.open();

		// create a non-blocking server socket channel
		ServerSocketChannel servSock = ServerSocketChannel.open();
		servSock.configureBlocking(false);

		//binding
		servSock.socket().bind(new InetSocketAddress(6332));

		// register and accept server socket
		servSock.register(selector, SelectionKey.OP_ACCEPT);

		// All Strings will be converted to US-ASCII coded byte to write in the Bytebuffer
		messageCharset = setCharset("US-ASCII");
		// initialize sender and receiver dictionary
		dictSender = new HashMap<Integer, String>();
		dictRcpt = new HashMap<Integer, String>();


		while (true) {
			if(selector.select() == 0) /* blocking */
				continue;
			
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();

			//'iter' iterate through the key collection and get all the channels in Interest-set according to their key status
			while(iter.hasNext()) {
				SelectionKey key = iter.next();

				if(key.isAcceptable()) {
					//a connection is accepted by a ServerSocketChannel
					ServerSocketChannel sock = (ServerSocketChannel) key.channel();
					SocketChannel client = sock.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

					//Connection Opening
					ByteBuffer buf = ByteBuffer.allocate(64);
					sendMessage("220 E.MAIL Simple Mail Transfer Service Ready", buf, client);
				}

				else if(key.isReadable()) {
					//a channel is ready for reading
					ByteBuffer buf = ByteBuffer.allocate(1024);
					SocketChannel channel = (SocketChannel) key.channel();
					channel.read(buf);
					buf.flip();
					
					String s = decodeMessage(buf);
					System.out.println("Message received: " + s);
					//received HELP -> send status 214
					if (substringSafe(s, 0, 4).equals("HELP")) {
						sendMessage("214 What can I help you?", buf, channel);
					}
					//received HELO -> send status 250
					else if (substringSafe(s, 0, 4).equals("HELO")) {
						sendMessage("250 E.MAIL", buf, channel);
					}
					//received MAILFROM -> put sender info in dictionary -> send status 250
					else if (substringSafe(s, 0, 9).equals("MAIL FROM")) {
						String sender = substringSafe(s, 11).trim();
						dictSender.put(key.hashCode(), sender);
						// System.out.println(msg_id + sender);
						sendMessage("250 OK", buf, channel);
					}
					//received RCPT TO -> put receiver info in dictionary -> create directory with receiver name -> send status 250
					else if (substringSafe(s, 0, 7).equals("RCPT TO")) {
						String rcpt_name = substringSafe(s, 9).trim();//remove <CRLF> from dirname
						dictRcpt.put(key.hashCode(), rcpt_name);
						//System.out.println(msg_id + rcpt_name);
						createDir(rcpt_name);
						sendMessage("250 OK", buf, channel);
					}
					//received DATA
					else if (substringSafe(s, 0, 4).equals("DATA")) {
						//System.out.println(getHash(key));
						sendMessage("354 Start mail input", buf, channel);
					}
					//received actual data ended in <CRLF>.<CRLF> -> save data to file
					else if (substringRight(s, 5).equals("\r\n.\r\n")) {
						writeNewFile(key.hashCode(), substringLeft(s, s.length()-5));//exclude <CRLF>.<CRLF>
						sendMessage("250 OK", buf, channel);
					}
					//received QUIT -> close channel and send status 221
					else if (substringSafe(s, 0, 4).equals("QUIT")) {
						sendMessage("221 E.MAIL Service closing transmission channel", buf, channel);
						channel.close();
						System.out.println("Close channel.");
					}
				}
				//remove key from Ready-set
				iter.remove();
			}
		}
	}

	/* Set character set encoding */
	private static Charset setCharset(String cs) {
		Charset messageCharset = null;
		try {
			messageCharset = Charset.forName(cs);
		} catch (UnsupportedCharsetException uce) {
			System.out.println("ERR_UNSUPPORTED_CHARSET");
			uce.printStackTrace();
		}
		return messageCharset;
	}

	/* Read message from the Buffer and convert to a String */
	private static String decodeMessage(ByteBuffer buf) {
		String returnChar = null;
		CharsetDecoder decoder = messageCharset.newDecoder();
		try {
			CharBuffer charBuf = decoder.decode(buf);
			returnChar = charBuf.toString();
		} catch (CharacterCodingException e) {
			System.out.println("ERR_CHAR_DECODE");
			e.printStackTrace();
		}
		return returnChar;
	}

	/* Send message over channel, include <CRLF> */
	private static void sendMessage(String str, ByteBuffer buf, SocketChannel channel) {
		byte[] message = new String(str + "\r\n").getBytes(messageCharset);
		buf = ByteBuffer.wrap(message);
		try {
			channel.write(buf);
		} catch (IOException e) {
			System.out.println("ERR_WRITE_SOCK_CHANNEL");
			e.printStackTrace();
		}
		buf.clear();
	}


	private static String substringSafe (String s, int start, int len) {
		if (s.length()>=start+len) return s.substring(start, len);
		else return "ERR_STR_LEN";
	}

	private static String substringSafe (String s, int start) {
		if (s.length()>start) return s.substring(start);
		else return "ERR_STR_LEN";
	}
	private static String substringRight (String s, int last_n) {
		if (s.length()>=last_n) return s.substring(s.length()-last_n);
		else return "ERR_STR_LEN";
	}
	private static String substringLeft (String s, int first_n) {
		if (s.length()>=first_n) return s.substring(0, first_n);
		else return "ERR_STR_LEN";
	}

	/* Process received emails, adopt to the technical requirements */
	private static void writeNewFile(Integer id, String data) {
		String sender = dictSender.get(id);
		String rcpt = dictRcpt.get(id);
		int random_id = ThreadLocalRandom.current().nextInt(0, 10000);
		String filename = rcpt + "/" + sender + "_" + random_id;
		/* save string data to a file using JAVA NIO channel */
		try {
			FileOutputStream f = new FileOutputStream(filename);
			FileChannel ch = f.getChannel();
			ch.write(ByteBuffer.wrap(data.getBytes(messageCharset)));
			ch.close();
			f.close();
			System.out.println("Wrote to file " + filename);
		} catch (IOException e) {
			System.out.println("ERR_CREATE_FILE");
			e.printStackTrace();
		}
		dictSender.remove(id);
		dictRcpt.remove(id);
	}

	/* Create a folder / directory */
	private static void createDir(String dirname) {
		try {
			Files.createDirectories(Paths.get(dirname));
		} catch (IOException e) {
			System.out.println("ERR_CREATE_DIR");
			e.printStackTrace();
		}
	}
}
