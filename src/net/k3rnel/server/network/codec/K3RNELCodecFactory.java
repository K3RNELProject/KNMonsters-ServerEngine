package net.k3rnel.server.network.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Provides a custom implementation of a codec factory allowing optimal networking
 * @author shadowkanji
 *
 */
public class K3RNELCodecFactory implements ProtocolCodecFactory {
	private final K3RNELEncoder m_encoder;
	private final K3RNELDecoder m_decoder;
	
	/**
	 * Default constructor.
	 */
	public K3RNELCodecFactory() {
		m_encoder = new K3RNELEncoder();
		m_decoder = new K3RNELDecoder();
	}
	
	/**
	 * Returns the decoder
	 */
	public ProtocolDecoder getDecoder() throws Exception {
		return m_decoder;
	}

	/**
	 * Returns the encoder
	 */
	public ProtocolEncoder getEncoder() throws Exception {
		return m_encoder;
	}

	/**
	 * Return the default decoder
	 */
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return m_decoder;
	}

	/**
	 * Returns the default encoder
	 */
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return m_encoder;
	}

}
