package net.k3rnel.server.network.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;

/**
 * Decodes messages received from a client
 * @author shadowkanji
 *
 */
public class K3RNELDecoder extends TextLineDecoder {
	
	/**
	 * Default constructor
	 */
	public K3RNELDecoder() {
		super(Charset.forName("US-ASCII"), LineDelimiter.AUTO);
	}
	
	/**
	 * Decodes a message
	 */
	public void decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		super.decode(session, in, out);
	}
}
