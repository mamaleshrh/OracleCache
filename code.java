import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Enum representing device statuses
enum DeviceStatus {
    NEEDS_ATTENTION, WORKING_NORMALLY, UNREACHABLE, ENABLED, DISABLED
}

// Interface for cache eviction strategy
interface EvictionStrategy {
    void evictEntries(Map<String, LocalDateTime> timestamps, LinkedHashMap<String, String> accessOrderMap);
}

// LRU Eviction Strategy implementing EvictionStrategy
class LRUEvictionStrategy implements EvictionStrategy {
    private final int maxSize;

    public LRUEvictionStrategy(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void evictEntries(Map<String, LocalDateTime> timestamps, LinkedHashMap<String, String> accessOrderMap) {
        while (accessOrderMap.size() > maxSize) {
            String lruKey = accessOrderMap.keySet().iterator().next();
            accessOrderMap.remove(lruKey);
            timestamps.remove(lruKey);
        }
    }
}

// Singleton Device Cache class
class DeviceCache {
    private static final int TTL_SECONDS = 5;
    private static DeviceCache instance;
    private final EvictionStrategy evictionStrategy;

    private final Map<DeviceStatus, Set<String>> statusDeviceMap = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> deviceTimestamps = new ConcurrentHashMap<>();
    private final Map<String, DeviceStatus> deviceStatusMap = new ConcurrentHashMap<>();
    private final LinkedHashMap<String, String> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);

    private DeviceCache(EvictionStrategy evictionStrategy) {
        this.evictionStrategy = evictionStrategy;
        for (DeviceStatus status : DeviceStatus.values()) {
            statusDeviceMap.put(status, new TreeSet<>());
        }
    }

    public static synchronized DeviceCache getInstance(EvictionStrategy evictionStrategy) {
        if (instance == null) {
            instance = new DeviceCache(evictionStrategy);
        }
        return instance;
    }

    public synchronized void updateCache(String deviceId, DeviceStatus status) {
        statusDeviceMap.values().forEach(devices -> devices.remove(deviceId));
        statusDeviceMap.get(status).add(deviceId);
        deviceTimestamps.put(deviceId, LocalDateTime.now());
        deviceStatusMap.put(deviceId, status);
        accessOrderMap.put(deviceId, deviceId);
        evictionStrategy.evictEntries(deviceTimestamps, accessOrderMap);
    }

    public synchronized List<String> getDevicesByStatus(DeviceStatus status) {
        List<String> validDevices = new ArrayList<>();
        Set<String> devices = new TreeSet<>(statusDeviceMap.get(status));
        for (String deviceId : devices) {
            if (!isExpired(deviceId)) {
                validDevices.add(deviceId);
            } else {
                removeDevice(deviceId);
            }
        }
        return validDevices;
    }

    private boolean isExpired(String deviceId) {
        LocalDateTime lastUpdate = deviceTimestamps.get(deviceId);
        return lastUpdate != null && LocalDateTime.now().isAfter(lastUpdate.plusSeconds(TTL_SECONDS));
    }

    private void removeDevice(String deviceId) {
        DeviceStatus status = deviceStatusMap.remove(deviceId);
        if (status != null) {
            statusDeviceMap.get(status).remove(deviceId);
        }
        deviceTimestamps.remove(deviceId);
        accessOrderMap.remove(deviceId);
    }
}

// Unit Test class
class DeviceCacheTest {
    public static void main(String[] args) {
        EvictionStrategy evictionStrategy = new LRUEvictionStrategy(3);
        DeviceCache cache = DeviceCache.getInstance(evictionStrategy);

        cache.updateCache("Device1", DeviceStatus.WORKING_NORMALLY);
        cache.updateCache("Device2", DeviceStatus.NEEDS_ATTENTION);
        cache.updateCache("Device3", DeviceStatus.ENABLED);
        cache.updateCache("Device4", DeviceStatus.DISABLED);

        System.out.println("Devices in NEEDS_ATTENTION: " + cache.getDevicesByStatus(DeviceStatus.NEEDS_ATTENTION));
        System.out.println("Devices in ENABLED: " + cache.getDevicesByStatus(DeviceStatus.ENABLED));
    }
}
