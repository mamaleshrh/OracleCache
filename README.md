# OracleCache

# Oracle Internship Project - In-Memory Cache for Identity Access Management

## **Overview**
During my internship at **Oracle**, I worked on optimizing the **Oracle Identity Access Management** product by implementing an **in-memory cache** to improve the performance of the **admin page**. The admin page manages user devices, tracks their statuses, and provides real-time analytics.

## **Project Scope**
The primary challenge was reducing the performance overhead caused by frequent **SQL queries** to fetch user device data. The admin page displayed information such as:
- Device Registration Date
- Last Online Timestamp (via heartbeat signals)
- Device Status (**NEEDS_ATTENTION, WORKING_NORMALLY, UNREACHABLE, ENABLED, DISABLED**)

To enhance system efficiency, I designed and implemented an **LRU-based (Least Recently Used) in-memory cache** that reduced the number of direct database queries.

## **Key Features Implemented**
### ✅ **Concurrent In-Memory Caching**
- Used **`ConcurrentHashMap`** to store device details and allow concurrent read/write operations.
- Ensured **thread safety** without requiring explicit locking.

### ✅ **LRU (Least Recently Used) Eviction Mechanism**
- Implemented **LRU eviction** using a **LinkedHashMap** to maintain access order.
- When the cache exceeded the **MAX_CACHE_SIZE**, the least recently accessed entries were removed.

### ✅ **TTL (Time-To-Live) Expiry Mechanism**
- Implemented **automatic invalidation** of cache entries after a **fixed time interval (TTL_SECONDS)**.
- Used timestamps to track last update time and remove expired entries.

### ✅ **Efficient Device Status Management**
- Mapped devices to different statuses using **ConcurrentHashMap<DeviceStatus, Set<String>>**.
- Allowed efficient retrieval of devices based on their status.

## **Technologies Used**
- **Java** (Core language)
- **JUnit** (For Unit Testing)
- **ConcurrentHashMap & LinkedHashMap** (Data Structures for efficient cache implementation)
- **SOLID Design Principles** (For maintainable and scalable code design)

## **Class Diagram (UML Representation)**
```
+-------------------------+
| <<interface>> Cache<K,V>|
+-------------------------+
| + put(K key, V value): void    |
| + get(K key): V                |
| + remove(K key): void          |
| + getKeysByStatus(Object status): List<K> |
| + evict(): void                |
+-------------------------+

           ^
           |
+-------------------------+
| <<abstract>> BaseCache<K,V> |
+-------------------------+
| - cacheMap: Map<K, V>       |
| + put(K key, V value): void |
| + get(K key): V             |
| + remove(K key): void       |
+-------------------------+

           ^
           |
+-------------------------+
| DeviceCache             |
+-------------------------+
| - MAX_CACHE_SIZE: int         |
| - TTL_SECONDS: int            |
| - timestamps: Map<String, Long>|
| - statusMap: Map<DeviceStatus, Set<String>> |
| + put(String deviceId, DeviceStatus status): void |
| + getKeysByStatus(Object status): List<String> |
| + evict(): void |
| + remove(String deviceId): void |
+-------------------------+

            <>--------------+
            |               |
+-------------------------+
| <<enum>> DeviceStatus    |
+-------------------------+
| NEEDS_ATTENTION          |
| WORKING_NORMALLY         |
| UNREACHABLE              |
| ENABLED                  |
| DISABLED                 |
+-------------------------+
```

## **Challenges Faced & Solutions**
### 🔴 **Issue: Debugging the Cache Implementation**
- I encountered a **bug** in the existing code that prevented execution.
- **Solution**: Created a **local function** to test all functionalities independently before integrating it into the system.

### 🔴 **Issue: Efficient Cache Synchronization**
- While using **ConcurrentHashMap**, explicit **locking** was not needed for most operations.
- However, for **certain updates (e.g., moving a device between statuses)**, I used **synchronized blocks** where necessary.

### 🔴 **Issue: Managing Expired Entries**
- Keeping track of **expired entries** in a **multi-threaded environment** was tricky.
- **Solution**: Used **timestamps** and an **eviction method** that ran periodically to remove outdated entries.

## **Unit Testing (JUnit)**
To ensure reliability, I wrote **JUnit test cases** to validate:
- **Concurrent Read & Write operations**
- **Correct LRU eviction behavior**
- **TTL-based automatic removal**

```java
@Test
void testConcurrentReadsAndWrites() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    DeviceCache cache = DeviceCache.getInstance();
    
    Runnable writer = () -> {
        cache.put("Device1", DeviceStatus.WORKING_NORMALLY);
    };
    
    Runnable reader = () -> {
        assertNotNull(cache.get("Device1"));
    };
    
    for (int i = 0; i < 5; i++) {
        executor.submit(writer);
        executor.submit(reader);
    }
    
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
}
```

## **Final Outcome**
- The **admin page response time improved significantly** by **reducing database queries**.
- The **cache handled concurrent requests efficiently** without performance degradation.
- The **modular and SOLID-compliant codebase** made future extensions easier.

## **Conclusion**
This project was an **excellent learning experience** in **high-performance caching, concurrency management, and software design principles**. It strengthened my skills in **Java, system optimization, and scalable software design**, while also giving me hands-on experience with **real-world enterprise solutions at Oracle**.
