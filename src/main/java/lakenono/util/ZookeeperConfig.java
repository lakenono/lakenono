package lakenono.util;
/**
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.RetryNTimes;

public class ZookeeperConfig {

	private static final String ZK_ADDRESS = "localhost:2181";
	private static final String ZK_PATH = "/config";
	private static final String NAMESPACE = "project";

	private final String path;
	private final String address;
	
	private Map<String,Set<String>> maps = new HashMap<String, Set<String>>();
	
	
	CuratorFramework client;

	public ZookeeperConfig() {
		this(ZK_ADDRESS);
	}

	public ZookeeperConfig(String address) {
		this(address, ZK_PATH);
	}

	public ZookeeperConfig(String address, String path) {
		this.address = address;
		this.path = path;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception {
		client = CuratorFrameworkFactory.builder()
				.connectString(address)
				.sessionTimeoutMs(60000)
				.connectionTimeoutMs(5000)
				.canBeReadOnly(false)
				.namespace(NAMESPACE)
				.retryPolicy(new RetryNTimes(5, 5000))
				.build();
		client.start();
		
		checkData();
		
		final PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
		childrenCache.getListenable().addListener((cli, event) -> {
			watcherNode(event);
		});
		childrenCache.start(StartMode.BUILD_INITIAL_CACHE);
		
		Thread.sleep(Integer.MAX_VALUE);
	}

	private void checkData() throws Exception {
		if(client.checkExists().forPath(path)==null){
			return;
		}
		List<String> lists = client.getChildren().forPath(path);
		for(String s:lists){
			byte[] data = client.getData().forPath(path+"/"+s);
			Set<String> sets = dataParse(data);
			maps.put(s, sets);
		}
	}

	private void watcherNode(PathChildrenCacheEvent event) {
		ChildData data = event.getData();
		String key = data.getPath().substring(path.length()+1);
		System.out.println("Receive event: path=[" + data.getPath() + "]" + ", data=[" + new String(data.getData()) + "]" + ", stat=[" + data.getStat() + "]");
		Set<String> tmp;

		switch (event.getType()) {
		case CHILD_ADDED:
			tmp = dataParse(data.getData());
			maps.put(key, tmp);
			break;
		case CHILD_REMOVED:
			if (checkKey(key)) {
				maps.get(key).clear();
				maps.remove(key);
			}
			break;
		case CHILD_UPDATED:
			if (checkKey(key)) {
				tmp = dataParse(data.getData());
				maps.get(key).clear();
				maps.get(key).addAll(tmp);
			} else {
				tmp = dataParse(data.getData());
				maps.put(key, tmp);
			}
			break;
		default:
			break;
		}
		System.out.println(maps);
	}
	
	private Set<String> dataParse(byte[] data) {
		Set<String> sets = null;
		if (data != null && data.length > 0) {
			String value = new String(data);
			String[] valuse = value.split(",");
			sets = new HashSet<String>(valuse.length);
			for (String s : valuse) {
				sets.add(s);
			}
		} else {
			sets = new HashSet<String>();
		}
		return sets;
	}
	
	public boolean checkKey(String key){
		return maps.containsKey(key);
	}
	
	public Set<String> getSets(String key){
		return maps.get(key);
	}
	
	public CuratorFramework getClient(){
		return client;
	}
	
	public void close(){
		if(client!=null){
			client.close();
		}
	}

	public static void main(String[] args) {
		ZookeeperConfig config = new ZookeeperConfig();
	}
}
*/