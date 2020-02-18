package io.github.ilmich.tempesta.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import io.github.ilmich.tempesta.io.connectors.ServerConnector;
import io.github.ilmich.tempesta.util.ExceptionUtils;
import io.github.ilmich.tempesta.web.http.HttpServerDescriptor;
import io.github.ilmich.tempesta.web.http.Request;
import io.github.ilmich.tempesta.web.http.Response;

public class PlainIOHandler implements IOHandler {

	private ExecutorService executor = Executors.newCachedThreadPool();
	// rework a better thread pools
	/*
	 * MoreExecutors.listeningDecorator(new ThreadPoolExecutor(
	 * HttpServerDescriptor.MIN_THREADS_PROCESSOR, // number of core threads
	 * HttpServerDescriptor.MAX_THREADS_PROCESSOR, // number of max threads
	 * HttpServerDescriptor.THREAD_PROCESSOR_IDLE_TIME, // thread idle time
	 * TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
	 */
	private final Logger logger = Logger.getLogger(PlainIOHandler.class.getName());

	private ServerConnector connector = null;

	private Protocol protocol = null;

	public PlainIOHandler() {
		super();
	}

	public PlainIOHandler(Protocol protocol) {
		super();
		this.protocol = protocol;
	}

	public void setExecutor(ExecutorService executor) {
		/*
		 * if (executor instanceof ListeningExecutorService) this.executor =
		 * (ListeningExecutorService) executor; else this.executor =
		 * MoreExecutors.listeningDecorator(executor);
		 */
		this.executor = executor;
	}

	@Override
	public void handleAccept(SelectionKey key) throws IOException {
		try {
			SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
			if (clientChannel.isOpen()) {
				clientChannel.configureBlocking(false);
				// register channel for reading
				connector.registerChannel(clientChannel, SelectionKey.OP_READ);
			}
		} catch (IOException ex) {
			logger.severe("Error accepting connection: " + ex.getMessage());
		}
	}

	@Override
	public void handleConnect(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleRead(SelectionKey key) throws IOException {
		final SocketChannel client = (SocketChannel) key.channel();
		final ByteBuffer readBuffer = ByteBuffer.allocate(HttpServerDescriptor.READ_BUFFER_SIZE);
		try {
			if (IOSocketHelper.readBuffer(readBuffer, client) < 0) { // client close connection
				throw new ClosedChannelException();
			}

			if (connector.hasKeepAliveTimeout(client)) { // prolong keep-alive timeout
				connector.prolongKeepAliveTimeout(client);
			}

			final Request req = protocol.onRead(readBuffer, client);
			if (req != null) { // response completed
				final CompletableFuture<Response> future = new CompletableFuture<Response>();

				executor.submit(new Runnable() {
				
					@Override
					public void run() {
						try {
							future.complete(protocol.processRequest(req));
						} catch (Throwable ex) {
							future.completeExceptionally(ex);
						}
					}
				});

				future.whenCompleteAsync(new BiConsumer<Response, Throwable>() {

					@Override
					public void accept(Response t, Throwable u) {
						if (t != null) {
							try {
								connector.registerChannel(client, SelectionKey.OP_WRITE, t);
							} catch (IOException ex) {
								logger.severe("Error when processing request: " + ExceptionUtils.getStackTrace(ex));
								logger.severe(req.toString());
								connector.removeKeepAliveTimeout(client);
								connector.closeChannel(client);
							}
						} else {
							logger.severe("Error when processing request: " + ExceptionUtils.getStackTrace(u));
							logger.severe(req.toString());
							connector.removeKeepAliveTimeout(client);
							connector.closeChannel(client);
						}

					}

				});
			}

		} catch (ClosedChannelException ex) {
			logger.fine("ClosedChannelException when reading: client disconnect");
			connector.removeKeepAliveTimeout(client);
			throw ex;
		} catch (IOException ex) {
			logger.severe("IOException when reading: " + ex.getMessage());
			connector.removeKeepAliveTimeout(client);
			throw ex;
		}
	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException {
		if (key.attachment() == null)
			return;

		boolean finished = false;
		SocketChannel client = (SocketChannel) key.channel();
		try {
			if (key.attachment() instanceof Response) {
				Response response = (Response) key.attachment();
				ByteBuffer writeBuffer = (ByteBuffer) response.getResponseData().getByteBuffer();

				IOSocketHelper.writeBuffer(writeBuffer, client);
				if (!writeBuffer.hasRemaining()) {
					if (!(finished = response.getFile() == null)) {
						FileChannel channel = (FileChannel) response.getFile();
						long bytesWritten = channel.transferTo(channel.position(), channel.size(), client);
						if (!(finished = bytesWritten < channel.size())) {
							channel.position(channel.position() + bytesWritten);
						} else {
							channel.close();
						}
					}
				}
				if (finished) {
					// connector.closeOrRegisterForRead(key, response.isKeepAlive());
					this.handleDisconnect(key);
				}
			}

		} catch (IOException ex) {
			logger.severe("Error writing on channel: " + ex.getMessage());
			connector.removeKeepAliveTimeout(client);
			throw ex;
		}
	}

	public void handleDisconnect(SelectionKey key) throws IOException {
		if (key.attachment() != null && key.attachment() instanceof Response) {
			Response response = (Response) key.attachment();
			connector.closeOrRegisterForRead(key, response.isKeepAlive());
		}
	}

	public ServerConnector getConnector() {
		return connector;
	}

	public void setConnector(ServerConnector connector) {
		this.connector = connector;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	@Override
	public void attachServerConnector(ServerConnector conn) {
		this.connector = conn;
	}
}
