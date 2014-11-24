package io.leopard.data.pub;

import io.leopard.core.context.Context;
import io.leopard.data4j.memdb.MemdbRsyncQueue;
import io.leopard.data4j.memdb.MemdbRsyncService;
import io.leopard.data4j.memdb.MemdbRsyncServiceRedisImpl;
import io.leopard.data4j.redis.Redis;

public abstract class PubSubRsyncImpl implements IPubSub, MemdbRsyncQueue, Context {

	protected MemdbRsyncService memdbRsyncService;

	private String channel;
	private Redis redis;

	public PubSubRsyncImpl(Redis redis, String channel) {
		this.redis = redis;
		this.channel = channel;
	}

	@Override
	public void init() {
		memdbRsyncService = new MemdbRsyncServiceRedisImpl(redis, channel, this);
		memdbRsyncService.init();
	}

	@Override
	public void destroy() {
		memdbRsyncService.destroy();
	}

	// @Override
	public boolean publish(String message) {
		// System.err.println("pub:" + message);
		memdbRsyncService.add(MemdbRsyncService.TYPE_SET, "key", message, false);
		return true;
	}

	@Override
	public boolean add(String type, String key, String value, boolean isMySelf) {
		// System.err.println("isMySelf:" + isMySelf + " type:" + type + " key:"
		// + key + " value:" + value);
		this.subscribe(value, isMySelf);
		return true;
	}

}